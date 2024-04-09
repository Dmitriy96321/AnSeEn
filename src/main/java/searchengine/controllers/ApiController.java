package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexind.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    //    /startIndexing
    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }
    @GetMapping("/deleteAll")
    public void deleteAll() {
        log.info("Deleting all indexings is CONTROLLER");
        indexingService.deleteAllSite();

    }
}
