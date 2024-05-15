package searchengine.dto.search;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SearchResult {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;

    @Override
    public String toString() {
        return "SearchResult{" +
                "site='" + site + '\'' +
                ", siteName='" + siteName + '\'' +
                ", uri='" + uri + '\'' +
                ", title='" + title + '\'' +
                ", snippet='" + snippet + '\'' +
                ", relevance=" + relevance +
                '}';
    }
}
