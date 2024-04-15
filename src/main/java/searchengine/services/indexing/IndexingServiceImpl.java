package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.ClientConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.indexing.parser.HttpParserJsoup;
import searchengine.services.indexing.parser.LinksExtractorAction;
import searchengine.services.indexing.parser.LinksExtractorFJP;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService{
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final SitesList sitesList;
    private final ClientConfig config;
    private final HttpParserJsoup httpParserJsoup;





    @Override
    public IndexingResponse startIndexing() {
        log.info("Starting indexing");
        if (sitesRepository.existsByStatus(StatusType.INDEXING)){
            return IndexingResponse.builder().result(false).error("Indexing is started").build();
        }
        if (!sitesRepository.findAll().isEmpty() || !pagesRepository.findAll().isEmpty()) {
            sitesRepository.deleteAll();
            pagesRepository.deleteAll();
        }
        addSite();
        addPages();
        return IndexingResponse.builder().result(true).build();
    }

    @Override
    public IndexingResponse stopIndexing() {

        return null;
    }


    private void addSite() {
        log.info("Adding site");
        sitesRepository.saveAll(sitesList.getSites().stream()
                .map(site -> {
                    SiteEntity siteEntity = new SiteEntity();
                    siteEntity.setName(site.getName());
                    siteEntity.setUrl(site.getUrl());
                    siteEntity.setStatus(StatusType.INDEXING);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    return siteEntity;
                }).toList());
    }
    @Override
    public void deleteAllSite(){
        log.info("Delete all sites SERVICE");
        sitesRepository.deleteAll();
        pagesRepository.deleteAll();
    }
    private void addPages(){
        log.info("Adding pages");

        List<SiteEntity> list = sitesRepository.findAll();
        ForkJoinPool forkJoinPool = new ForkJoinPool(list.size());
        list.forEach(siteEntity -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                log.error(e.getMessage() + " sleep interrupted");
            }
            forkJoinPool.invoke(new LinksExtractorAction(siteEntity,
                    httpParserJsoup, pagesRepository, sitesRepository));
        }
    );

//        forkJoinPool.invoke(new LinksExtractorAction(list.get(2),httpParserJsoup,pagesRepository,sitesRepository));

    }

    private void deleteIndexingSite(Site site) {}
    private boolean indexingSiteIsStarted() {
        return sitesRepository.existsByStatus(StatusType.INDEXING);
    }
}
