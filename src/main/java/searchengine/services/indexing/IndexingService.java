package searchengine.services.indexing;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexind.IndexingResponse;

public interface IndexingService  {
    void deleteAllSite();
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
}
