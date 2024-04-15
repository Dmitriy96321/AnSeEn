package searchengine.services.indexing.parser;


import org.jsoup.Connection;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

public class LinksExtractorAction extends RecursiveAction {
    private SiteEntity site;
    private String url;
    private Set<String> linksAll;
    private HttpParserJsoup httpParserJsoup;
    private PagesRepository pagesRepository;
    private SitesRepository sitesRepository;

    public LinksExtractorAction(SiteEntity site, HttpParserJsoup httpParserJsoup,
                                PagesRepository pagesRepository, SitesRepository sitesRepository) {
        this.site = site;
        this.url = site.getUrl();
        this.linksAll = ConcurrentHashMap.newKeySet();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
    }

    public LinksExtractorAction(SiteEntity site, String url, Set<String> linksAll,
                                HttpParserJsoup httpParserJsoup, PagesRepository pagesRepository,
                                SitesRepository sitesRepository) {
        this.site = site;
        this.url = url;
        this.linksAll = linksAll;
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
    }

    @Override
    protected void compute() {
        Set<LinksExtractorAction> taskList = new HashSet<>();
        List<String> links = httpParserJsoup.extractLinks(url);
        for (String link : links) {
            if (linksAll.add(url) || linksAll.add(link)) {
                pagesRepository.save(createPageEntity(link));
                LinksExtractorAction task = new LinksExtractorAction(site,link,linksAll,
                        httpParserJsoup,pagesRepository,sitesRepository);
                task.fork();
                taskList.add(task);
                site.setStatusTime(LocalDateTime.now());
                sitesRepository.save(site);
            }
        }
        for(LinksExtractorAction task : taskList){
            task.join();
        }
        site.setStatus(StatusType.INDEXED);
        sitesRepository.save(site);
    }

    private PageEntity createPageEntity(String link) {
//        Connection connection = httpParserJsoup.getConnect(link);
        PageEntity pageEntity = new PageEntity();
        pageEntity.setPath(link);
        pageEntity.setSiteId(site);
        pageEntity.setCode(httpParserJsoup.responseCode(link));
        pageEntity.setContent(httpParserJsoup.extractContent(link));
        return pageEntity;
    }
}
