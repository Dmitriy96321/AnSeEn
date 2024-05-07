package searchengine.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private boolean result;
    private String error;
    private int count;
    private List<SearchResult> data;
}
