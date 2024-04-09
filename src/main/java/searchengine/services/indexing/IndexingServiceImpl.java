package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final SitesList list;



    @Override
    public IndexingResponse startIndexing() {
        System.out.println("Starting indexing");
        return IndexingResponse.builder().result(true).build();
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }


    public void IndexingSite(Site site) {
    }
}
