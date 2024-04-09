package searchengine.services.indexing.parser;

import java.io.IOException;
import java.util.List;

public interface HttpParser {
    List<String> extractLinks(String url) throws IOException;
}
