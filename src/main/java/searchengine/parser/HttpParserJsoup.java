package searchengine.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.ClientConfig;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class HttpParserJsoup implements HttpParser {

    private final ClientConfig config;

    public synchronized Connection getConnect(String url) {
        return Jsoup.connect(url)
                .userAgent(config.getUserAgent())
                .referrer(config.getReferer())
                .timeout(config.getTimeout())
                .ignoreContentType(true)
                .ignoreHttpErrors(true);
    }

    @Override
    public Set<String> extractLinks(String url) {
        Set<String> links = null;
//        System.out.println(url);
        try {

            links = getConnect(url).get()
                    .select("a[href*=/]")
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(href -> (href.contains(url.replaceAll("^(.*?\\/\\/[^\\/]+\\/).*", "$1"))
                                || (href.startsWith("/") && href.length() > 4)) && (!href.contains(".jpg")))
                    .collect(Collectors.toSet())
                    .stream()
                    .map(href -> {
                        if (href.startsWith("/")) {
                            if (url.length() - url.replaceAll("/","").length()>2){
                                return url.substring(0, url.indexOf("/", 8)) + href;
                            }
                            return url + href;
                        }
                        return href;
                    })
                    .collect(Collectors.toSet());
            links.add(url);
            return links;
        } catch (IOException e) {
            log.error(e + " - extractLinks");
        }
        log.info(links + " - extractLinks");
        return links;
    }

}
