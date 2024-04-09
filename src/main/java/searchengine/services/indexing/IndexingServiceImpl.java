package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService{
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final SitesList sitesList;




    @Override
    public IndexingResponse startIndexing() {
        log.info("Starting indexing");
        if (sitesRepository.existsByStatus(StatusType.INDEXING)){
            return IndexingResponse.builder().result(false).error("Indexing is started").build();
        }
        sitesList.getSites().forEach(this::addSite);
        return IndexingResponse.builder().result(true).build();
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }


    public void addSite(Site site) {
        log.info("Add site: " + site.getName());
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(StatusType.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        sitesRepository.save(siteEntity);
    }
    @Override
    public void deleteAllSite(){
        log.info("Delete all sites SERVICE");
        sitesRepository.deleteAll();
    }
    private void deleteIndexingSite(Site site) {}
    private boolean indexingSiteIsStarted() {
        return sitesRepository.existsByStatus(StatusType.INDEXING);
    }
}
