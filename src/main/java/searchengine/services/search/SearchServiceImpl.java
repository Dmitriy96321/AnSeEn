package searchengine.services.search;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.parser.HttpParserJsoup;
import searchengine.parser.LemmaParser;
import searchengine.repositories.JpaIndexesRepository;
import searchengine.repositories.JpaLemmaRepository;
import searchengine.repositories.JpaPagesRepository;
import searchengine.repositories.JpaSitesRepository;
import searchengine.util.SnippetBuilder;


import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaParser lemmaParser;
    private final JpaSitesRepository sitesRepository;
    private final JpaPagesRepository pagesRepository;
    private final JpaLemmaRepository lemmaRepository;
    private final JpaIndexesRepository indexesRepository;
    private final HttpParserJsoup httpParserJsoup;
    private final SnippetBuilder snippetBuilder;


    @Override
    public SearchResponse search(String site, int offset, int limit, String query) {
        log.info("Searching for site: " + site);
        if (query == null || query.isEmpty()) {
            log.error("query is null or empty");
            return SearchResponse.builder().result(false).error("Задан пустой поисковый запрос").build();
        }

        if (site != null){
            SiteEntity siteEntity = sitesRepository.findBySiteUrl(site);
            if (siteEntity == null) {
                return SearchResponse.builder().result(false).error("Указанная страница не найдена").build();
            } else if (siteEntity.getStatus().equals(StatusType.INDEXING)){
                return SearchResponse.builder().result(false).error("Индексация " + site + " не завершена.").build();
            }
        }
        Map<SiteEntity, List<LemmaEntity>> siteLemmasListMap = getLemmaEntityList(site, query);

        List<SearchResult> resultList = getPagesEntityFromQuery(siteLemmasListMap, query);
        float maxAbsolutRelevance = resultList.stream()
                .map(SearchResult::getRelevance)
                .max(Float::compareTo)
                .orElse(0f);

        resultList.forEach(
                searchResult -> searchResult.setRelevance(searchResult.getRelevance() / maxAbsolutRelevance)
        );
        resultList.sort(Comparator.comparing(SearchResult::getRelevance).reversed());

        if (resultList.isEmpty()) {
            return SearchResponse.builder().result(false).error("Ничего не найдено").build();
        }

        return SearchResponse.builder()
                .result(true)
                .count(resultList.size())
                .data(resultList.size() > limit ?
                        offset + limit < resultList.size() ?
                                resultList.subList(offset, offset + limit)
                                : resultList.subList(offset, resultList.size())
                        : resultList
                ).build();
    }


    private List<SearchResult> getPagesEntityFromQuery(Map<SiteEntity, List<LemmaEntity>> siteLemmasListMap
            , String query) {

        List<SearchResult> listPageEntityFromSiteMap = new ArrayList<>();

        siteLemmasListMap.forEach((siteEntity, lemmaEntityList) -> {
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
                                lemmaRepository.getLemmasFromPage(pageEntity.getId(),siteEntity.getId())
                                        .contains(lemmaEntity))
                        .toList();


            }

            listPageEntityFromSiteMap.addAll(list.stream().map(pageEntity -> {
                float relevance = 0;
                for (LemmaEntity lemmaEntity : lemmaEntityList) {
                    relevance = relevance + indexesRepository.findByPageIdAndLemmaId(pageEntity, lemmaEntity).getRank();
                }
                String title = "";
                try {
                    title = httpParserJsoup.getConnect(pageEntity.getSiteId().getUrl() + pageEntity.getPath())
                            .execute().parse().title();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                return SearchResult.builder()
                        .site(pageEntity.getSiteId().getUrl())
                        .siteName(pageEntity.getSiteId().getName())
                        .uri(pageEntity.getPath())
                        .title(title)
                        .snippet(snippetBuilder.getSnippet(pageEntity,query))
                        .relevance(relevance)
                        .build();

            }).toList());
        });

        return listPageEntityFromSiteMap;
    }


    private Map<SiteEntity, List<LemmaEntity>> getLemmaEntityList(String site, String query) {
        log.info("get");
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
        log.info("{}",listLemmasEntityFromSiteMap.keySet().size());
        return listLemmasEntityFromSiteMap;
    }


    private List<LemmaEntity> getLemmaEntityListFromSite(String query, SiteEntity site) {
        List<LemmaEntity> list = new ArrayList<>();
        int rarity = (pagesRepository.countBySiteId(site) * 75) / 100;
        for (String lemma : lemmaParser.getLemmasFromQuery(query)) {
            if (lemma != null || !lemma.isEmpty()) {
                LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSiteId(lemma, site);
                if (lemmaEntity == null) {
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

