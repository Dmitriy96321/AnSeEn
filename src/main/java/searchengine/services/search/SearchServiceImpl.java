package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
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
        return buildSearchResponse(site,query);
    }

    private SearchResponse buildSearchResponse(String site, String query) {
        List<SearchResult> resultList = new ArrayList<>();

        getPagesEntityFromQuery(site, query).forEach(page-> resultList.add(
                    SearchResult.builder()
                            .site(page.getSiteId().getUrl())
                            .siteName(page.getSiteId().getName())
                            .uri(page.getPath())
                            .title(Jsoup.parse(page.getContent()).title())
                            .snippet("какой-то сниппет")
                            .relevance(0)
                            .build())
        );

        if (resultList.isEmpty()) {
            return SearchResponse.builder().result(false).error("Ничего не найдено").build();
        }

        return SearchResponse.builder().result(true).count(resultList.size()).data(resultList).build();
    }


    private List<PageEntity> getPagesEntityFromQuery(String site,String query){

        List<PageEntity> listPageEntityFromSiteMap = new ArrayList<>();

        getLemmaEntityList(site,query).forEach((siteEntity, lemmaEntityList) -> {
            List<PageEntity> list = new ArrayList<>();
            int counter = lemmaEntityList.size();

            for (LemmaEntity lemmaEntity : lemmaEntityList) {
                if (list.isEmpty() && counter == lemmaEntityList.size()) {
                    list.addAll(pagesRepository.findByLemma(lemmaEntity.getId()));
                    continue;
                }
                counter--;

                list = list.stream()
                        .filter(pageEntity ->
                                lemmaRepository.getLemmasFromPage(pageEntity.getId())
                                        .contains(lemmaEntity))
                        .toList();


            }

            listPageEntityFromSiteMap.addAll(list);
        });

        return listPageEntityFromSiteMap;
    }


    private Map<SiteEntity, List<LemmaEntity>> getLemmaEntityList(String site,String query) {
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
        for (String lemma : lemmaParser.getLemmasFromQuery(query)){
            if (lemma != null || !lemma.isEmpty()) {
                LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSiteId(lemma, site);
                if (lemmaEntity == null){
                    list = new ArrayList<>();
                    return list;
                }
                if (lemmaEntity.getFrequency() <= rarity) {
                    list.add(lemmaRepository.findByLemmaAndSiteId(lemma, site));
                }
            }
        }

        list.sort(Comparator.comparing(LemmaEntity::getFrequency));

        return list;
    }




}
