package searchengine.services.indexing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import searchengine.parser.HttpParserJsoup;
import searchengine.parser.PagesExtractorAction;

import java.util.List;
import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final LemmaRepository lemmasRepository;
    private final IndexesRepository indexRepository;
    private final SitesList sitesList;
    private final HttpParserJsoup httpParserJsoup;
    private final List<ForkJoinPool> forkJoinPools;
    private final EntityCreator entityCreator;
    private final Jedis jedis;


    @Override
    @Transactional
    public IndexingResponse startIndexing() {
        log.info("Starting indexing");
        if (!indexingIsStarted()) {
            sitesList.getSites().forEach(site -> site.setIndexingIsStopped(false));
            addAllSites();
            return IndexingResponse.builder().result(true).build();
        }
        return IndexingResponse.builder().result(false).error("Indexing is started").build();
    }

    @Override
    @Transactional
    public IndexingResponse stopIndexing() {
        log.info("Stopping indexing ");
        if (!forkJoinPools.isEmpty()) {
            sitesList.getSites().forEach(site -> site.setIndexingIsStopped(true));
            forkJoinPools.forEach(ForkJoinPool::shutdownNow);
            sitesRepository.findAll().forEach(siteEntity -> {
                if (siteEntity.getStatus().equals(StatusType.INDEXING)) {
                    siteEntity.setStatus(StatusType.FAILED);
                    siteEntity.setLastError("Индексация остановлена пользователем");
                }
            });
            return IndexingResponse.builder().result(true).build();
        }
        return IndexingResponse.builder().result(false).error("Нет сайтов на индексации.").build();
    }

    @Override
    @Transactional
    public IndexingResponse indexPage(String urlPage) {
        log.info("Indexing page {}", urlPage);
        SiteEntity siteEntity = sitesRepository.findBySiteUrl(urlPage.substring(0, urlPage.indexOf("/", 8)));
        if (siteEntity == null) {
            return IndexingResponse.builder().result(false).error("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n").build();
        }

        PageEntity newPageEntity = entityCreator.createPageEntity(urlPage, siteEntity);
        PageEntity pageEntity = pagesRepository.findByPagePath(newPageEntity.getPath(),siteEntity.getId());

        if (pageEntity != null) {
            log.info(pageEntity.toString());
            lemmasRepository.getLemmasFromPage(pageEntity.getId()).forEach(lemma -> {
                lemma.setFrequency(lemma.getFrequency() - 1);
            });
            indexRepository.deleteIndexesByPageId(pageEntity.getId());

            pageEntity.setContent(newPageEntity.getContent());
            pageEntity.setCode(newPageEntity.getCode());

            createLemmaForPage(pageEntity);

        } else {

            pagesRepository.save(newPageEntity);
            log.info(newPageEntity.toString());
            createLemmaForPage(newPageEntity);
        }
        return IndexingResponse.builder().result(true).build();
    }

    @Override
    @Transactional
    public void someMethod(Long id) {
        PageEntity pageEntity = pagesRepository.findById(id).orElseThrow();
        lemmasRepository.getLemmasFromPage(pageEntity.getId()).forEach(lemma -> {
            lemma.setFrequency(lemma.getFrequency() - 1);
        });
        indexRepository.deleteIndexesByPageId(pageEntity.getId());
        pagesRepository.delete(pageEntity);
    }


    private void addAllSites() {
        clearBase();
        sitesList.getSites().forEach(site -> {
            ForkJoinPool pool = new ForkJoinPool();

            pool.execute(new PagesExtractorAction(site, httpParserJsoup,
                    pagesRepository, sitesRepository,
                    lemmasRepository, indexRepository,
                    pool, entityCreator)
            );
            forkJoinPools.add(pool);
        });
    }


    private boolean indexingIsStarted() {
        if (forkJoinPools.isEmpty()) {
            return false;
        }
        for (ForkJoinPool pool : forkJoinPools) {
            if (pool.getActiveThreadCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private void clearBase() {
        forkJoinPools.clear();
        log.info("Adding site");
        sitesRepository.truncateTableSite();
        pagesRepository.truncateTablePage();
        lemmasRepository.truncateTableLemma();
        indexRepository.truncateTableIndexes();
        jedis.flushAll();
    }

    private void createLemmaForPage(PageEntity pageEntity) {
        entityCreator.getLemmaForPage(pageEntity).forEach((lemma, frequency) -> {
            LemmaEntity lemmaEntity = lemmasRepository.findByLemma(lemma);
            log.info( lemma + " Adding lemma {}", lemmaEntity);
            if (lemmaEntity != null) {
                lemmaEntity.setFrequency(frequency + 1);
                indexRepository.save(entityCreator.createIndexEntity(pageEntity, lemmaEntity, frequency));
            } else {
                lemmaEntity = entityCreator.createLemmaForPage(
                        sitesRepository.findById(pageEntity.getSiteId().getId()).orElseThrow(), lemma
                );
                lemmasRepository.save(lemmaEntity);
                indexRepository.save(entityCreator.createIndexEntity(pageEntity, lemmaEntity, frequency));
            }
        });
    }

}