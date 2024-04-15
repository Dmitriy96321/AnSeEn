package searchengine.services.indexing;


import searchengine.dto.indexind.IndexingResponse;

public interface IndexingService  {
    void deleteAllSite();
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
}
