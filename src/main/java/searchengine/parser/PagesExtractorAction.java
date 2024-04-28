package searchengine.parser;


import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Slf4j
public class PagesExtractorAction extends RecursiveAction {

    private Site site;
    private SiteEntity siteEntity;
    private String url;
    private final HttpParserJsoup httpParserJsoup;
    private final PagesRepository pagesRepository;
    private final SitesRepository sitesRepository;
    private final LemmaRepository lemmasRepository;
    private final IndexesRepository indexRepository;
    private final Set<String> PAGES_CASH;
    private final Set<String> LEMMAS_CASH;
    private final ForkJoinPool thisPool;
    private final EntityCreator entityCreator;



    public PagesExtractorAction(Site site, HttpParserJsoup httpParserJsoup,
                                PagesRepository pagesRepository, SitesRepository sitesRepository,
                                LemmaRepository lemmasRepository, IndexesRepository indexRepository,
                                ForkJoinPool thisPool, EntityCreator entityCreator) {
        this.LEMMAS_CASH = ConcurrentHashMap.newKeySet();
        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = entityCreator.createSiteEntity(site);
        this.url = site.getUrl();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.PAGES_CASH = ConcurrentHashMap.newKeySet();
        this.thisPool = thisPool;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
        sitesRepository.save(siteEntity);
    }


    public PagesExtractorAction(SiteEntity siteEntity, String url, Site site,
                                HttpParserJsoup httpParserJsoup, PagesRepository pagesRepository,
                                LemmaRepository lemmasRepository, IndexesRepository indexRepository,
                                SitesRepository sitesRepository, Set<String> PAGES_CASH, Set<String> lemmasCash, ForkJoinPool thisPool,
                                EntityCreator entityCreator) {
        LEMMAS_CASH = lemmasCash;

        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = siteEntity;
        this.url = url;
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.PAGES_CASH = PAGES_CASH;
        this.thisPool = thisPool;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    protected void compute() {

        Set<PagesExtractorAction> taskList = new HashSet<>();
        Set<String> links = httpParserJsoup.extractLinks(url);
        for (String link : links) {
            if (site.isIndexingIsStopped()) {
                return;
            }
            synchronized (pagesRepository){
                if (!pagesRepository.existsByPageUrl(link.substring(siteEntity.getUrl().length()))) {
                    savePage(link);

                    PagesExtractorAction task = new PagesExtractorAction(siteEntity, link, site,
                            httpParserJsoup, pagesRepository,
                            lemmasRepository, indexRepository,
                            sitesRepository, PAGES_CASH, LEMMAS_CASH, thisPool, entityCreator);
                    task.fork();
                    taskList.add(task);

                }
            }
        }
        for (PagesExtractorAction task : taskList) {
            task.join();
        }

        if (!site.isIndexingIsStopped()) {
            siteEntity.setStatus(StatusType.INDEXED);
            sitesRepository.setStatusBySite(StatusType.INDEXED, siteEntity.getId());
        }
    }
    private void savePage(String link){
        PageEntity pageEntity = entityCreator.createPageEntity(link, siteEntity);
        pagesRepository.save(pageEntity);
        LocalDateTime time = LocalDateTime.now();
        siteEntity.setStatusTime(time);
        sitesRepository.setStatusTime(time, siteEntity.getId());
    }
}
