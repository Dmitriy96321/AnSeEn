package searchengine.dto.indexind;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IndexingResponse {
private boolean result;
private String error;
}
