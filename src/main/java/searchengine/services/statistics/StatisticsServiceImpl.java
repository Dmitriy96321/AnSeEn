package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.repositories.JpaLemmaRepository;
import searchengine.repositories.JpaPagesRepository;
import searchengine.repositories.JpaSitesRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final JpaSitesRepository sitesRepository;
    private final JpaPagesRepository pagesRepository;
    private final JpaLemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site value : sitesList) {
            SiteEntity site = sitesRepository.findBySiteUrl(value.getUrl());
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            if (site != null) {
                item.setName(site.getName());
                item.setUrl(site.getUrl());
                item.setPages(pagesRepository.countBySiteId(site));
                item.setLemmas(lemmaRepository.countBySiteId(site));
                item.setStatus(site.getStatus().toString());
                item.setError(site.getLastError());
                item.setStatusTime(site.getStatusTime().toInstant(ZoneOffset.UTC).toEpochMilli());
                total.setPages(total.getPages() + pagesRepository.countBySiteId(site));
                total.setLemmas(total.getLemmas() + lemmaRepository.countBySiteId(site));
            } else {
                item.setName(value.getName());
                item.setUrl(value.getUrl());
                item.setPages(0);
                item.setLemmas(0);
                item.setStatus("FAILED");
                item.setError("NOT_INDEXING");
                item.setStatusTime(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
                total.setPages(total.getPages());
                total.setLemmas(total.getLemmas());
            }
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
