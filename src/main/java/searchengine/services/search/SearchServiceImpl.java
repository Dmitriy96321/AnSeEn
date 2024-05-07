package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    @Override
    public SearchResponse search() {
        return null;
    }
}
