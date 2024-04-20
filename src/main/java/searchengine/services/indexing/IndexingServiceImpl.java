package searchengine.services.indexing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.model.EntityCreator;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.indexing.parser.HttpParserJsoup;
import searchengine.services.indexing.parser.PagesExtractorAction;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final SitesList sitesList;
    private final HttpParserJsoup httpParserJsoup;
    private final List<ForkJoinPool> forkJoinPools;
    private final EntityCreator entityCreator;


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


    private void addAllSites() {
        forkJoinPools.clear();
        log.info("Adding site");
        sitesRepository.truncateTableSite();
        pagesRepository.truncateTablePage();

        sitesList.getSites().forEach(site -> {
            ForkJoinPool pool = new ForkJoinPool();
            System.err.println(pool.isTerminated() + " " + pool.getActiveThreadCount() + " " + pool.getPoolSize());

            pool.execute(new PagesExtractorAction(site, httpParserJsoup,
                    pagesRepository, sitesRepository, pool, entityCreator));
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