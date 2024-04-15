package searchengine.services.indexing.parser;


import lombok.extern.slf4j.Slf4j;
import searchengine.config.ClientConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
@Slf4j
public class LinksExtractorFJP extends RecursiveTask<Set<String>> {
    private String url;
    private HttpParserJsoup httpParserJsoup;

    private static Set<String> linksAll = ConcurrentHashMap.newKeySet();
    public LinksExtractorFJP(String url, HttpParserJsoup httpParserJsoup) {
        this.url = url;
        this.httpParserJsoup = httpParserJsoup;
    }
    @Override
    protected Set<String> compute() {
//        log.info(url + " - fjp");
        Set<LinksExtractorFJP> taskList = new HashSet<>();
        Set<String> setUrl = new HashSet<>();
        List<String> links = httpParserJsoup.extractLinks(url);
//        System.out.println(links +" poiii");
        for (String link : links) {
            if (linksAll.add(link)) {
                linksAll.add(url);
                setUrl.add(link);
                LinksExtractorFJP task = new LinksExtractorFJP(link, httpParserJsoup);
                task.fork();
                taskList.add(task);
            }
        }
        taskList.forEach(f -> setUrl.addAll(f.join()));
        return setUrl;
    }
}