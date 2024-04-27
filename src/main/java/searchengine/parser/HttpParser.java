package searchengine.parser;

import java.io.IOException;
import java.util.Set;

public interface HttpParser {
    Set<String> extractLinks(String url) throws IOException;
}
