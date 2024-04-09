package searchengine.dto.indexind;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
//@AllArgsConstructor
public class IndexingResponse {
private boolean result;
private String error;
}
