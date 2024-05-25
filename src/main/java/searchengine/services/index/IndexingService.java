package searchengine.services.index;


import searchengine.dto.index.IndexingResponse;

public interface IndexingService  {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexPage(String urlPage);
}
