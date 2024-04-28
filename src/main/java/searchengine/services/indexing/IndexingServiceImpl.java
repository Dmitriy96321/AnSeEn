package searchengine.services.indexing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.model.EntityCreator;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.parser.LemmaParser;
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
    private final LemmaParser lemmaParser;


    @Override
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
        SiteEntity siteEntity = sitesRepository.findBySiteUrl(urlPage.substring(0, urlPage.indexOf("/",8)));
        if (siteEntity == null) {
            return IndexingResponse.builder().result(false).error("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n").build();
        }
        PageEntity newPageEntity = entityCreator.createPageEntity(urlPage, siteEntity );
        PageEntity pageEntity = pagesRepository.findByPageUrl(newPageEntity.getPath());
        if ( pageEntity != null){
            pageEntity.setContent(newPageEntity.getContent());
            pageEntity.setCode(newPageEntity.getCode());
        }else {
            pagesRepository.save(entityCreator.createPageEntity(urlPage, siteEntity));
        }
        return IndexingResponse.builder().result(true).build();
    }

    @Override
    public void someMethod(Long id) {
        System.out.println(id);
        pagesRepository.deleteDuplicatePages();
//        PageEntity pageEntity = pagesRepository.findById(id).orElseThrow();
//        entityCreator.createLemmaForPage(pageEntity).forEach(System.out::println);
//        lemmaParser.getLemmasForPage(pageEntity).entrySet().forEach(System.out::println);
    }


    private void addAllSites() {
        forkJoinPools.clear();
        log.info("Adding site");
        sitesRepository.truncateTableSite();
        pagesRepository.truncateTablePage();
        lemmasRepository.truncateTableLemma();
        indexRepository.truncateTableIndexes();

        sitesList.getSites().forEach(site -> {
            ForkJoinPool pool = new ForkJoinPool(4);
//            System.err.println(pool.isTerminated() + " " + pool.getActiveThreadCount() + " " + pool.getPoolSize());

            pool.execute(new PagesExtractorAction(site, httpParserJsoup,
                    pagesRepository, sitesRepository,
                    lemmasRepository, indexRepository,
                    pool, entityCreator));
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


}