package searchengine.services.indexing;


import org.springframework.web.bind.annotation.PathVariable;
import searchengine.dto.indexind.IndexingResponse;

public interface IndexingService  {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexPage(String urlPage);
    void someMethod(Long id);
}
