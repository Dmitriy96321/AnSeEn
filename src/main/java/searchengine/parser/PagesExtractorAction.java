package searchengine.parser;


import lombok.extern.slf4j.Slf4j;
import searchengine.config.LettuceConcurrentSet;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

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
    private final ForkJoinPool thisPool;
    private final EntityCreator entityCreator;
    private LettuceConcurrentSet concurrentSet;


    public PagesExtractorAction(Site site, HttpParserJsoup httpParserJsoup,
                                PagesRepository pagesRepository, SitesRepository sitesRepository,
                                LemmaRepository lemmasRepository, IndexesRepository indexRepository,
                                ForkJoinPool thisPool, EntityCreator entityCreator) {
        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = entityCreator.createSiteEntity(site);
        this.url = site.getUrl();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.thisPool = thisPool;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
        sitesRepository.save(siteEntity);
        this.concurrentSet = new LettuceConcurrentSet(siteEntity.getId().toString());
    }


    public PagesExtractorAction(SiteEntity siteEntity, String url, Site site,
                                HttpParserJsoup httpParserJsoup, PagesRepository pagesRepository,
                                LemmaRepository lemmasRepository, IndexesRepository indexRepository,
                                SitesRepository sitesRepository, ForkJoinPool thisPool,
                                EntityCreator entityCreator, LettuceConcurrentSet concurrentSet) {


        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = siteEntity;
        this.url = url;
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.thisPool = thisPool;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
        this.concurrentSet = concurrentSet;
    }

    @Override
    protected void compute() {

        Set<PagesExtractorAction> taskList = new HashSet<>();
        Set<String> links = httpParserJsoup.extractLinks(url);

        for (String link : links) {
            if (site.isIndexingIsStopped()) {
                return;
            }
                if (concurrentSet.add(link)) {
                    PageEntity pageEntity = entityCreator.createPageEntity(link,siteEntity);
                    pagesRepository.save(pageEntity);
                    saveTime(link);
                    PagesExtractorAction task = new PagesExtractorAction(siteEntity, link, site,
                            httpParserJsoup, pagesRepository,
                            lemmasRepository, indexRepository,
                            sitesRepository, thisPool, entityCreator, concurrentSet);
                    task.fork();
                    taskList.add(task);

                }

        }
        for (PagesExtractorAction task : taskList) {
            task.join();
        }

        if (!site.isIndexingIsStopped()) {
            siteEntity.setStatus(StatusType.INDEXED);
            sitesRepository.setStatusBySite(StatusType.INDEXED, siteEntity.getId());
//            concurrentSet.close();
        }
    }
    private void saveTime(String link){
//        PageEntity pageEntity = pagesRepository.findByPageUrl(link.substring(siteEntity.getUrl().length()));
//        entityCreator.getLemmaForPage(pageEntity);
        LocalDateTime time = LocalDateTime.now();
        siteEntity.setStatusTime(time);
        sitesRepository.setStatusTime(time, siteEntity.getId());
    }
}
