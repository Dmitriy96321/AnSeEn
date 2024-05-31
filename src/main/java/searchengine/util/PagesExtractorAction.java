package searchengine.util;


import lombok.extern.slf4j.Slf4j;
import searchengine.config.LettuceCache;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.parser.HttpParserJsoup;
import searchengine.repositories.JpaIndexesRepository;
import searchengine.repositories.JpaLemmaRepository;
import searchengine.repositories.JpaPagesRepository;
import searchengine.repositories.JpaSitesRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class PagesExtractorAction extends RecursiveAction {


    private Site site;
    private SiteEntity siteEntity;
    private String url;
    private final HttpParserJsoup httpParserJsoup;
    private final JpaPagesRepository pagesRepository;
    private final JpaSitesRepository sitesRepository;
    private final JpaLemmaRepository lemmasRepository;
    private final JpaIndexesRepository indexRepository;
    private final ForkJoinPool thisPool;
    private final EntityCreator entityCreator;
    private final LettuceCache lettuceCach;
    private  Map<String,LemmaEntity> lemmasCache;
    private List<IndexEntity> indexEntities;
    private final boolean isFirst;
    long start = System.currentTimeMillis();


    public PagesExtractorAction(Site site, HttpParserJsoup httpParserJsoup,
                                JpaPagesRepository pagesRepository, JpaSitesRepository sitesRepository,
                                JpaLemmaRepository lemmasRepository, JpaIndexesRepository indexRepository,
                                ForkJoinPool thisPool, EntityCreator entityCreator) {
        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = entityCreator.createSiteEntity(site);
        this.url = site.getUrl();
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.thisPool = thisPool;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
        sitesRepository.save(siteEntity);
        this.lettuceCach = new LettuceCache(siteEntity);
        this.lemmasCache =  new ConcurrentHashMap<>();
        this.isFirst = true;
        this.indexEntities = new ArrayList<>();

    }

    public PagesExtractorAction(SiteEntity siteEntity, String url, Site site,
                                HttpParserJsoup httpParserJsoup, JpaPagesRepository pagesRepository,
                                JpaLemmaRepository lemmasRepository, JpaIndexesRepository indexRepository,
                                JpaSitesRepository sitesRepository, ForkJoinPool thisPool,
                                EntityCreator entityCreator, LettuceCache lettuceCache,
                                Map<String,LemmaEntity> lemmasCache) {
        this.entityCreator = entityCreator;
        this.site = site;
        this.siteEntity = siteEntity;
        this.url = url;
        this.httpParserJsoup = httpParserJsoup;
        this.pagesRepository = pagesRepository;
        this.sitesRepository = sitesRepository;
        this.thisPool = thisPool;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
        this.lettuceCach = lettuceCache;
        this.lemmasCache = lemmasCache;
        this.isFirst = false;
        this.indexEntities = new ArrayList<>();
    }

    @Override
    protected void compute() {

        Set<PagesExtractorAction> taskList = new HashSet<>();
        Set<String> links = httpParserJsoup.extractLinks(url);

        for (String link : links) {
            if (site.isIndexingIsStopped()) {
                indexRepository.saveAll(indexEntities);
                lemmasRepository.saveAll(new ArrayList<>(lemmasCache.values()));
                return;
            }
            if (lettuceCach.addSet("pages", link)) {
                PageEntity pageEntity = null;
                int count = 3;
                while (0 < count) {
                    try {
                        pageEntity = entityCreator.createPageEntity(link, siteEntity);
                        pagesRepository.save(pageEntity);
                        count = 0;
                    } catch (Exception e) {
                        log.error("{} {}", e.getMessage(), count);
                        count--;
                    }
                }

                if (pageEntity.getId() == null || pageEntity.getContent() == null) {
                    log.error("Error: create page entity failed for {}", link);
                    continue;
                }

                saveLemmas(pageEntity);
                saveTime();

                PagesExtractorAction task = new PagesExtractorAction(siteEntity, link, site,
                        httpParserJsoup, pagesRepository,
                        lemmasRepository, indexRepository,
                        sitesRepository, thisPool, entityCreator, lettuceCach, lemmasCache);
                task.fork();
                taskList.add(task);
            }

        }
        indexRepository.saveAll(indexEntities);
        for (PagesExtractorAction task : taskList) {
            task.join();
        }

        if (!site.isIndexingIsStopped() && isFirst) {
            siteEntity.setStatus(StatusType.INDEXED);
            siteEntity.setLastError("");
            sitesRepository.save(siteEntity);
            lemmasRepository.saveAll(new ArrayList<>(lemmasCache.values()));
            long endTime = System.currentTimeMillis();
            log.info("Время индексации {} : {} миллисекунд", siteEntity.getName(),(endTime - start));
        }

    }
    private void saveTime(){
        LocalDateTime time = LocalDateTime.now();
        siteEntity.setStatusTime(time);
        sitesRepository.setStatusTime(time, siteEntity.getId());
    }


    public void saveLemmas(PageEntity pageEntity) {
//        List<LemmaEntity> newLemmasEntityList = new ArrayList<>();

        entityCreator.getLemmaForPage(pageEntity).forEach((lemma, frequency) -> {
            if (site.isIndexingIsStopped() || pageEntity.getCode() != 200) {
                return;
            }
            if (lettuceCach.addSet("lemma", lemma)) {
                LemmaEntity lemmaEntity = entityCreator.createLemmaForPage(siteEntity, lemma);
//                System.out.println(lemma + " "+ pageEntity.getId());
                lemmasCache.put(lemma, lemmaEntity);
                lemmasRepository.save(lemmaEntity);
//                newLemmasEntityList.add(lemmaEntity);
                indexEntities.add(entityCreator.createIndexEntity(pageEntity, lemmaEntity, frequency));
            } else {
                LemmaEntity lemmaEntity = lemmasCache.get(lemma);
                indexEntities.add(entityCreator.createIndexEntity(pageEntity, lemmaEntity, frequency));
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmasCache.put(lemma, lemmaEntity);
            }
        });
//        lemmasRepository.saveAll(newLemmasEntityList);
    }
}
