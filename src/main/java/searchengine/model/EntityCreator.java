package searchengine.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.parser.HttpParserJsoup;
import searchengine.parser.LemmaParser;
import searchengine.repositories.LemmaRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class EntityCreator {
    private final HttpParserJsoup httpParserJsoup;
    private final LemmaParser lemmaParser;

    public PageEntity createPageEntity(String link, SiteEntity siteEntity) {
        int responseCode;
        PageEntity pageEntity = new PageEntity();
        Connection.Response response = null;
        pageEntity.setPath(link.substring(siteEntity.getUrl().length()));
        pageEntity.setSiteId(siteEntity);

        try {
            response = httpParserJsoup.getConnect(link).execute();
            responseCode = response.statusCode();
            pageEntity.setCode(responseCode);
            pageEntity.setContent((responseCode == 200) ? response.parse().toString() :
                    response.statusMessage());
        } catch (IOException e) {
            log.error(e + e.getMessage() + " " + link + " createPageEntity ");
            return pageEntity;
        }
        return pageEntity;
    }

    public SiteEntity createSiteEntity(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl().substring(0, site.getUrl().length() - 1));
        siteEntity.setStatusTime(LocalDateTime.now());

        try {
            httpParserJsoup.getConnect(site.getUrl()).execute().statusCode();
            siteEntity.setStatus(StatusType.INDEXING);
        } catch ( IOException e) {
            log.error(e.getMessage());
            siteEntity.setStatus(StatusType.FAILED);
            site.setIndexingIsStopped(true);
            log.info(site.getUrl() + " " + site.isIndexingIsStopped());
            siteEntity.setLastError(e.getClass() + e.getMessage());
            System.out.println(siteEntity.getStatus());
        }
        return siteEntity;
    }

    public LemmaEntity createLemmaForPage(SiteEntity site, String lemma, Integer frequency){
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSiteId(site);
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(frequency);
        return lemmaEntity;
    }

    public IndexEntity createIndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity, Float rank){
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setPageId(pageEntity);
        indexEntity.setLemmaId(lemmaEntity);
        indexEntity.setRank(rank);

        return indexEntity;
    }
    public Map<String, Integer> getLemmaForPage(PageEntity pageEntity){
        log.info("getLemmaForPage + 1");
        return lemmaParser.getLemmasForPage(pageEntity);
    }

}
