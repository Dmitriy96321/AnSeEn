package searchengine.services.indexing;


import searchengine.dto.indexind.IndexingResponse;

public interface IndexingService  {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexPage(String urlPage);
    void someMethod(Long id);
}
