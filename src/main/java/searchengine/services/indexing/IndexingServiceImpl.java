package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.indexing.parser.HttpParserJsoup;
import searchengine.services.indexing.parser.PagesExtractorAction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final SitesList sitesList;
    private final HttpParserJsoup httpParserJsoup;
    private ForkJoinPool forkJoinPool;

    @Override
    public IndexingResponse startIndexing() {
        log.info("Starting indexing");
        if (sitesRepository.existsByStatus(StatusType.INDEXING)) {
            return IndexingResponse.builder().result(false).error("Indexing is started").build();
        }
        System.err.println("********* Starting indexing");
        addAllSites();
        return IndexingResponse.builder().result(true).build();
    }

    @Override
    public IndexingResponse stopIndexing() {
        log.info("Stopping indexing " + forkJoinPool.isTerminated());
        if (!forkJoinPool.isTerminated()) {
            forkJoinPool.shutdownNow();
            synchronized (sitesRepository) {
                sitesRepository.findAll().forEach(siteEntity -> {
                    if (siteEntity.getStatus().equals(StatusType.INDEXING)) {
                        siteEntity.setStatus(StatusType.FAILED);
                        sitesRepository.save(siteEntity);
                        System.err.println();
                    }
                });
            }
            return IndexingResponse.builder().result(true).build();
        }
        return IndexingResponse.builder().result(false).error("Нет сайтов на индексации.").build();
    }

    private void addAllSites() {
        log.info("Adding site");
        if (!sitesRepository.findAll().isEmpty()){
            sitesRepository.deleteAll();
        }
        if (!pagesRepository.findAll().isEmpty()) {
            pagesRepository.deleteAll();
        }
        forkJoinPool = new ForkJoinPool();
        sitesList.getSites().forEach(site -> {
            SiteEntity siteEntity = getSiteEntity(site);
            sitesRepository.save(siteEntity);
            forkJoinPool.execute(new PagesExtractorAction(siteEntity,
                    httpParserJsoup, pagesRepository, sitesRepository));
        });

    }

    @Override
    public void deleteAllSite() {
        log.info("Delete all sites SERVICE");
        sitesRepository.deleteAll();
        pagesRepository.deleteAll();
    }

    private SiteEntity getSiteEntity(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatusTime(LocalDateTime.now());
        try {
            httpParserJsoup.getConnect(site.getUrl()).execute().statusCode();
            siteEntity.setStatus(StatusType.INDEXING);
        } catch ( IOException e) {
            log.error(e.getMessage());
            siteEntity.setStatus(StatusType.FAILED);
            siteEntity.setLastError(e.getClass() + e.getMessage());
            System.out.println(siteEntity.getStatus());
        }
        System.out.println(siteEntity.getStatus());


        return siteEntity;
    }

}