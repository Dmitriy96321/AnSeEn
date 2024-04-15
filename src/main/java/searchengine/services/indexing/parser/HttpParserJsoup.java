package searchengine.services.indexing.parser;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.ClientConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class HttpParserJsoup implements HttpParser{

    private final ClientConfig config;
    @Override
    public List<String> extractLinks(String url)  {
//        log.info(config.getReferer());
        try {

//            log.info("parser: "+config.getReferer() + ", " + config.getUserAgent());

            List<String> links = Jsoup.connect(url)
                    .userAgent(config.getUserAgent())
                    .referrer(config.getReferer())
                    .timeout(10 * 1000).ignoreContentType(true).ignoreHttpErrors(true)
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
            log.error(e.getMessage());
        }
        return List.of();
    }
}
