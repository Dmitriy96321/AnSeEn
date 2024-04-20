package searchengine.services.indexing.parser;


import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.model.EntityCreator;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

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
    private final Set<String> PAGES_CASH;
    private final ForkJoinPool thisPool;
    private final EntityCreator entityCreator;


    public PagesExtractorAction(Site site, HttpParserJsoup httpParserJsoup,
                                PagesRepository pagesRepository, SitesRepository sitesRepository,
                                ForkJoinPool thisPool, EntityCreator entityCreator) {
        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = entityCreator.createSiteEntity(site);
        this.url = site.getUrl();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.PAGES_CASH = ConcurrentHashMap.newKeySet();
        this.thisPool = thisPool;
        sitesRepository.save(siteEntity);
    }


    public PagesExtractorAction(SiteEntity siteEntity, String url, Site site,
                                HttpParserJsoup httpParserJsoup, PagesRepository pagesRepository,
                                SitesRepository sitesRepository, Set<String> PAGES_CASH, ForkJoinPool thisPool,
                                EntityCreator entityCreator) {

        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = siteEntity;
        this.url = url;
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.PAGES_CASH = PAGES_CASH;
        this.thisPool = thisPool;
    }

    @Override
    protected void compute() {
        Set<PagesExtractorAction> taskList = new HashSet<>();
        Set<String> links = httpParserJsoup.extractLinks(url);
        List<PageEntity> listPage = new ArrayList<>();
        for (String link : links) {
            if (site.isIndexingIsStopped()) {
                return;
            }
            if (PAGES_CASH.add(url)) {
                listPage.add(entityCreator.createPageEntity(url, siteEntity));
            }
            if (PAGES_CASH.add(link)) {
                listPage.add(entityCreator.createPageEntity(link, siteEntity));
                PagesExtractorAction task = new PagesExtractorAction(siteEntity, link, site,
                        httpParserJsoup, pagesRepository,
                        sitesRepository, PAGES_CASH, thisPool , entityCreator);
                task.fork();
                taskList.add(task);
            }
        }
        pagesRepository.saveAll(listPage);
        LocalDateTime time = LocalDateTime.now();
        siteEntity.setStatusTime(time);
        sitesRepository.setStatusTime(time, siteEntity.getId());

        for (PagesExtractorAction task : taskList) {
            task.join();
        }

        if (!site.isIndexingIsStopped() && thisPool.getQueuedTaskCount() == 0) {
            siteEntity.setStatus(StatusType.INDEXED);
            sitesRepository.setStatusBySite(StatusType.INDEXED, siteEntity.getId());
        }
    }


}
