package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;


@Repository
public interface JpaPagesRepository extends JpaRepository<PageEntity, Long> {
    Integer countBySiteId(SiteEntity site);

    @Modifying
    @Query(value = "TRUNCATE TABLE page", nativeQuery = true)
    void truncateTablePage();

    @Query(value = "select * from page p where p.path = :path and p.site_id = :site"
            , nativeQuery = true)
    PageEntity findByPagePath(@Param("path") String path, @Param("site") Long siteId);

    @Query(value = "SELECT page.id, page.site_id, path, code, content " +
            "FROM page " +
            "JOIN search_engine.indexes i on page.id = i.page_id " +
            "join search_engine.lemma l on l.id = i.lemma_id " +
            "WHERE l.id = :lemma_id", nativeQuery = true)
    List<PageEntity> findByLemma(@Param("lemma_id") Long lemmaId);

    @Query("SELECT SUM(i.rank) AS totalRank " +
            "FROM IndexEntity i " +
            "WHERE i.pageId IN (SELECT DISTINCT p from PageEntity p where p in :pageIds) " +
            "AND i.lemmaId IN (SELECT l from LemmaEntity l where l in :lemmaIds) " +
            "GROUP BY i.pageId")
            List<Float> findTotalRankByPageIdsAndLemmaIds(@Param("pageIds") List<PageEntity> pageIds,
                                                          @Param("lemmaIds") List<LemmaEntity> lemmaIds);
}