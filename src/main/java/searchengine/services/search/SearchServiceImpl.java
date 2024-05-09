package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    @Override
    public SearchResponse search(String query) {
        if (query == null || query.isEmpty()) {
            log.error("query is null or empty");
            return SearchResponse.builder().result(false).error("Задан пустой поисковый запрос").build();
        }
        System.out.println(query);
        System.out.println(splitQuery(query));
        List<SearchResult> list = new ArrayList<>();
        list.add(SearchResult.builder().url("htttt").title("zagolovok").site("httt").siteName("kakoito").snippet("dgfgdf").relevance(0.6F).build());
        list.add(SearchResult.builder().url("htttg").title("zagolok").site("httu").siteName("kakoit").snippet("dgffffdf").relevance(0.9F).build());
        return SearchResponse.builder().result(true).count(1).data(list).build();
    }
    private List<String> splitQuery(String query) {
        return Arrays.stream(
                query.replaceAll("[^А-я ]","")
                        .replaceAll("\\s{2,}"," ")
                        .toLowerCase().split(" ")
        ).toList();
    }
}
