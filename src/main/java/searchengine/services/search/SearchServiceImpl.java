package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.parser.LemmaParser;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaParser lemmaParser;
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexesRepository indexesRepository;




    @Override
    public SearchResponse search(String site,String query) {
        if (query == null || query.isEmpty()) {
            log.error("query is null or empty");
            return SearchResponse.builder().result(false).error("Задан пустой поисковый запрос").build();
        }


//        System.out.println(lemmaRepository.findByLemmaAndSiteId(query, sitesRepository.findBySiteUrl(site)).getLemma() + "  " + lemmaRepository.findByLemmaAndSiteId(query, sitesRepository.findBySiteUrl(site)).getSiteId().getId());

        System.out.println(getLemmaEntityList(query,site));
        List<SearchResult> list = new ArrayList<>();
        list.add(SearchResult.builder().url("htttt").title("zagolovok").site("httt").siteName("kakoito").snippet(lemmaParser.getLemmasFromQuery(query).toString()).relevance(0.6F).build());
//        list.add(SearchResult.builder().url("htttg").title("zagolok").site("httu").siteName("kakoit").snippet("dgffffdf").relevance(0.9F).build());
        return SearchResponse.builder().result(true).count(list.size()).data(list).build();
    }

    private Map<SiteEntity, List<LemmaEntity>> getLemmaEntityList(String query, String site) {
        Map<SiteEntity, List<LemmaEntity>> listLemmasEntityFromSiteMap = new HashMap<>();

        if (site == null || site.isEmpty()) {
            List<SiteEntity> siteEntities = sitesRepository.findAll();
            siteEntities.forEach(siteEntity ->
                listLemmasEntityFromSiteMap.put(siteEntity, getLemmaEntityListFromSite(query, siteEntity))
            );
            return listLemmasEntityFromSiteMap;
        }
        SiteEntity siteEntity = sitesRepository.findBySiteUrl(site);
        listLemmasEntityFromSiteMap.put(siteEntity, getLemmaEntityListFromSite(query, siteEntity));
        return listLemmasEntityFromSiteMap;
    }
    private List<LemmaEntity> getLemmaEntityListFromSite(String query, SiteEntity site) {
        List<LemmaEntity> list = new ArrayList<>();
        int rarity = (pagesRepository.countBySiteId(site) * 75) / 100;
        lemmaParser.getLemmasFromQuery(query).forEach(lemma -> {
            if (lemma != null || !lemma.isEmpty()) {
                LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSiteId(lemma, site);
                if (lemmaEntity != null
                        && lemmaEntity.getFrequency() <= rarity
                ) {
                    System.out.println(lemmaEntity.getLemma() + " " + lemmaEntity.getFrequency());
                    list.add(lemmaRepository.findByLemmaAndSiteId(lemma, site));
                }
            }
        });
        return list;
    }


}
