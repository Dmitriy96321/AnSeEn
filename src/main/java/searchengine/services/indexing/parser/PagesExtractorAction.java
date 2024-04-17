package searchengine.services.indexing.parser;


import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

@Slf4j
public class PagesExtractorAction extends RecursiveAction {
    private Integer sackCounter;
    private SiteEntity site;
    private String url;
    private HttpParserJsoup httpParserJsoup;
    private final PagesRepository pagesRepository;
    private final SitesRepository sitesRepository;

    public PagesExtractorAction(SiteEntity site, HttpParserJsoup httpParserJsoup,
                                PagesRepository pagesRepository, SitesRepository sitesRepository) {
        this.sackCounter = 0;
        this.site = site;
        this.url = site.getUrl();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
    }

    public PagesExtractorAction(SiteEntity site, String url, Integer sackCounter,
                                HttpParserJsoup httpParserJsoup, PagesRepository pagesRepository,
                                SitesRepository sitesRepository) {
        this.sackCounter = sackCounter;
        this.site = sitesRepository.findById(site.getId()).orElse(null);
        this.url = url;
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
    }

    @Override
    protected void compute() {
        sackCounter++;
        Set<PagesExtractorAction> taskList = new HashSet<>();
            List<String> links = httpParserJsoup.extractLinks(url);
        for (String link : links) {
            synchronized (pagesRepository) {
                synchronized (sitesRepository) {
                    if (!isIndexing(site)) {
                        return;
                    }
                    if (!pagesRepository.existsByPath(url)) {
                        pagesRepository.save(createPageEntity(url));
                    }
                    if (!pagesRepository.existsByPath(link)) {
                        pagesRepository.saveAndFlush(createPageEntity(link));
                        PagesExtractorAction task = new PagesExtractorAction(site, link, sackCounter,
                                httpParserJsoup, pagesRepository, sitesRepository);
                        task.fork();
                        taskList.add(task);
                        site.setStatusTime(LocalDateTime.now());
                        if (isIndexing(site)) {
                            sitesRepository.save(site);
                        } else {
                            return;
                        }
                    }
                }
            }
        }


        for (PagesExtractorAction task : taskList) {
            task.join();
        }
        sackCounter--;
        if (sackCounter == 0 && isIndexing(site)) {
            System.err.println(sackCounter + " pages extracted successfully");
            site.setStatus(StatusType.INDEXED);
            sitesRepository.save(site);
        }
    }
    private boolean isIndexing(SiteEntity site){
        synchronized (sitesRepository) {
            return sitesRepository.findById(site.getId()).orElseThrow().getStatus().equals(StatusType.INDEXING);
        }
    }





    private PageEntity createPageEntity(String link) {
        String errorMessage;
        PageEntity pageEntity = new PageEntity();

        try {

            Connection.Response response = httpParserJsoup.getConnect(link).execute();
            pageEntity.setPath(link);
            pageEntity.setSiteId(site);
            pageEntity.setCode(response.statusCode());
            pageEntity.setContent(response.parse().toString());
            return pageEntity;
        } catch (IOException e) {
            errorMessage = e.getMessage();
            log.error(e + e.getMessage() + " " + link + " createPageEntity ");
            pageEntity.setContent(errorMessage);
        }
        return pageEntity;
    }
}
