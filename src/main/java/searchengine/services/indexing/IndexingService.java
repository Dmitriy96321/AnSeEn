package searchengine.services.indexing;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexind.IndexingResponse;

public interface IndexingService  {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
}
