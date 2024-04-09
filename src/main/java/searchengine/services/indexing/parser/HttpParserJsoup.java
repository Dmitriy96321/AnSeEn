package searchengine.services.indexing.parser;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.ClientConfig;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class HttpParserJsoup implements HttpParser{
    private final ClientConfig config;
    @Override
    public List<String> extractLinks(String url) throws IOException {
        try {
            Thread.sleep(150);
            return Jsoup.connect(url)
                    .userAgent(config.getUserAgent())
                    .referrer(config.getReferer())
                    .get()
                    .select("a[href*=/]").stream()
                        .map(link -> link.attr("href"))
                        .filter(href -> href.contains(url.replaceAll("^(.*?\\/\\/[^\\/]+\\/).*", "$1")))
                        .collect(Collectors.toSet())
                        .stream()
                        .sorted()
                        .toList();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
