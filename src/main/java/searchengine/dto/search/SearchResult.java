package searchengine.dto.search;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SearchResult {
    private String site;
    private String siteName;
    private String url;
    private String title;
    private String snippet;
    private float relevance;
}
