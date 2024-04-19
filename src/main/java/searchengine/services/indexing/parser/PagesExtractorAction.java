package searchengine.services.indexing.parser;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.io.IOException;
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

    private SiteEntity site;
    private String url;
    private HttpParserJsoup httpParserJsoup;
    private final PagesRepository pagesRepository;
    private final SitesRepository sitesRepository;
    private final Set<String> PAGES_CASH;
    private final ForkJoinPool thisPool;

    public PagesExtractorAction(SiteEntity site, HttpParserJsoup httpParserJsoup,
                                PagesRepository pagesRepository, SitesRepository sitesRepository,ForkJoinPool thisPool) {

        this.site = site;
        this.url = site.getUrl();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.PAGES_CASH = ConcurrentHashMap.newKeySet();
        this.thisPool = thisPool;
    }

    public PagesExtractorAction(SiteEntity site, String url,
                                HttpParserJsoup httpParserJsoup, PagesRepository pagesRepository,
                                SitesRepository sitesRepository, Set<String> PAGES_CASH, ForkJoinPool thisPool) {

        this.site = site;
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
            if (site.getStatus().equals(StatusType.FAILED)){
                return;
            }
            if (PAGES_CASH.add(url)){
                listPage.add(createPageEntity(url));
            }
            if (PAGES_CASH.add(link)) {
                listPage.add(createPageEntity(link));
                PagesExtractorAction task = new PagesExtractorAction(site, link,
                        httpParserJsoup, pagesRepository, sitesRepository, PAGES_CASH, thisPool);
                task.fork();
                taskList.add(task);
            }
        }
        pagesRepository.saveAll(listPage);
        LocalDateTime time = LocalDateTime.now();
        site.setStatusTime(time);

        for (PagesExtractorAction task : taskList) {
            task.join();
        }
//        System.err.println(thisPool.getQueuedTaskCount() + " tasks extracted");


        if (thisPool.getQueuedTaskCount() == 0
//                && isIndexing(site)
        ) {
//            System.err.println(thisPool.getPoolSize() + " PoolSize pages extracted");
            System.err.println(thisPool.getQueuedTaskCount() + " tasks extracted");
            site.setStatus(StatusType.INDEXED);
            sitesRepository.save(site);
        }
    }

    private boolean isIndexing(SiteEntity site) {
        return sitesRepository.findById(site.getId()).orElseThrow().getStatus().equals(StatusType.INDEXING);
    }





    private PageEntity createPageEntity(String link) {
        int responseCode;
        PageEntity pageEntity = new PageEntity();
        Connection.Response response = null;
        pageEntity.setPath(link);
        pageEntity.setSiteId(site);

        try {
            response = httpParserJsoup.getConnect(link).execute();
            responseCode = response.statusCode();
            pageEntity.setCode(responseCode);
            pageEntity.setContent((responseCode == 200) ? response.parse().toString() :
                    response.statusMessage());

        } catch (IOException e) {
            log.error(e + e.getMessage() + " " + link + " createPageEntity ");
            return pageEntity;
        }

        return pageEntity;
    }
}
