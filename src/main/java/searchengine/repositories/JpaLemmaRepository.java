package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface JpaLemmaRepository extends JpaRepository<LemmaEntity, Long> {

    int countBySiteId(SiteEntity siteEntity);


    LemmaEntity findByLemmaAndSiteId(String lemma, SiteEntity siteId);

    @Modifying
    @Query(value = "TRUNCATE TABLE lemma", nativeQuery = true)
    void truncateTableLemma();

    @Query(value = "SELECT DISTINCT l.id, l.site_id, l.lemma, l.frequency " +
            "FROM page " +
            "JOIN search_engine.indexes i ON page.id = i.page_id " +
            "JOIN search_engine.lemma l ON l.id = i.lemma_id " +
            "WHERE page.id = :page_id AND page.site_id = :site_id", nativeQuery = true)
    List<LemmaEntity> getLemmasFromPage(@Param("page_id") Long pageId, @Param("site_id") Long siteId);




}
