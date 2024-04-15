package searchengine.services.indexing.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.ClientConfig;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class HttpParserJsoup implements HttpParser{

    private final ClientConfig config;

    public Connection getConnect(String url) {
        return  Jsoup.connect(url)
                .userAgent(config.getUserAgent())
                .referrer(config.getReferer())
                .timeout(config.getTimeout())
                .ignoreContentType(true)
                .ignoreHttpErrors(true);
    }
    @Override
    public List<String> extractLinks(String url)  {
//        log.info(config.getReferer());
        try {
            //            log.info("parser: "+config.getReferer() + ", " + config.getUserAgent());
            List<String> links = getConnect(url)
                    .get()
                    .select("a[href*=/]")
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(href -> (href.contains(url.replaceAll("^(.*?\\/\\/[^\\/]+\\/).*", "$1"))
                            || (href.startsWith("/") && href.length() > 4)) && (!href.contains(".jpg")))
                    .collect(Collectors.toSet())
                    .stream()
                    .map(href -> {
                        if (href.startsWith("/")) {
                            return url.substring(0, url.indexOf("/", 8)) + href;
                        }
                        return href;
                    })
                    .sorted()
                    .toList();
//            System.out.println(links + "pars");

            return links;
        } catch (IOException e) {
            log.error(e + e.getMessage() + " - extractLinks in " + url);
        }
        return List.of();
    }

    public String extractContent(String url) {
        try {
            return getConnect(url).get().toString();
        } catch (IOException e) {
            log.error(e + e.getMessage() + " - extractContent in " + url);
        }
        return "";
    }
    public int responseCode(String url) {
        try {
            return getConnect(url).execute().statusCode();
        } catch (IOException e) {
            log.error(e + e.getMessage() + " - responseCode in " + url);
        }
        return 0;
    }
}
