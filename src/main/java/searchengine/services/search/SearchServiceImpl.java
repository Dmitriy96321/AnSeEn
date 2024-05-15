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

        List<SearchResult> resultList = getPagesEntityFromQuery(site, query);
        float maxAbsolutRelevance = resultList.stream()
                .map(SearchResult::getRelevance)
                .max(Float::compareTo)
                .orElse(0f);

        resultList.forEach(searchResult -> {
            searchResult.setSnippet(getSnippet(searchResult, query));
            searchResult.setRelevance(searchResult.getRelevance() / maxAbsolutRelevance);
                }
        );
        resultList.sort(Comparator.comparing(SearchResult::getRelevance).reversed());





        if (resultList.isEmpty()) {
            return SearchResponse.builder().result(false).error("Ничего не найдено").build();
        }

        return SearchResponse.builder().result(true).count(resultList.size()).data(resultList).build();
    }


    private List<SearchResult> getPagesEntityFromQuery(String site,String query){

        List<SearchResult> listPageEntityFromSiteMap = new ArrayList<>();

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

            listPageEntityFromSiteMap.addAll(list.stream().map(pageEntity -> {
                float relevance = 0;
                for (LemmaEntity lemmaEntity : lemmaEntityList) {
                    relevance = relevance + indexesRepository.findByPageIdAndLemmaId(pageEntity, lemmaEntity).getRank();
                }
                return SearchResult.builder()
                        .site(pageEntity.getSiteId().getUrl())
                        .siteName(pageEntity.getSiteId().getName())
                        .uri(pageEntity.getPath())
                        .title(Jsoup.parse(pageEntity.getContent()).title())

                        .relevance(relevance)
                        .build();
            }).toList());
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

    private String getSnippet(SearchResult result, String query){
        log.info(result.getUri());
        log.info(lemmaParser.getLemmasFromQuery(query)+"");
        PageEntity page = pagesRepository.findByPagePath(result.getUri(),
                sitesRepository.findBySiteUrl(result.getSite()).getId());
        String search = Jsoup.parse(page.getContent()).body().text();
        StringBuilder snippet = new StringBuilder();
        log.info("{}", search.contains(query));
        if (search.contains(query)){

            int index = search.indexOf(query);
            if(index !=-1){

                int wordStart = Math.max(index - 40, 0);
                int wordEnd = Math.min(index + query.length() + 40, search.length());
                String extractedText = search.substring(wordStart, wordEnd);
                System.out.println("Извлеченный текст: " + extractedText);
                snippet.append("...").append(extractedText).append("...\n");

            }else{
                if(search.startsWith(query)){

                    String extractedText = search.substring(0, Math.min(query.length() + 20, search.length()));
                    System.out.println("Извлеченный текст: "+extractedText);
                    snippet.append("...").append(extractedText).append("...\n");


                }
            }
            return snippet.toString();
        }

//        query.split(" ")

        for (String lemma: lemmaParser.getLemmasFromQuery(query)){
            log.info(page.getSiteId().getName() + " " + lemma + " " + page.getSiteId().getUrl()+page.getPath());
            if(search.contains(lemma)) {
                int index = search.indexOf(lemma);
                if(index !=-1){

                    int wordStart = Math.max(index - 40, 0);
                    int wordEnd = Math.min(index + lemma.length() + 40, search.length());
                    String extractedText = search.substring(wordStart, wordEnd);
                    System.out.println("Извлеченный текст: "+extractedText);
                    snippet.append("...").append(extractedText).append("...\n");

                }else{
                    if(search.startsWith(lemma)){

                        String extractedText = search.substring(0, Math.min(lemma.length() + 20, search.length()));
                        System.out.println("Извлеченный текст: "+extractedText);
                        snippet.append("...").append(extractedText).append("...\n");


                    }
                }
            } else {
                System.out.println("Слово '" + lemma + "' не найдено в тексте");
            }
        }

        System.out.println(page);


        return snippet.toString();
    }

}

