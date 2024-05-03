package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {



    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE lemma", nativeQuery = true)
    void truncateTableLemma();
    LemmaEntity findByLemma(String lemma);

    @Modifying
    @Transactional
    @Query("update LemmaEntity l set l.frequency = :value where l.id = :id")
    void updateLemmaFrequency(@Param("value") Integer value, @Param("id") long id);

    @Transactional
    @Query(value = "SELECT l.id, l.site_id, lemma, frequency " +
            "FROM page " +
            "JOIN search_engine.indexes i on page.id = i.page_id " +
            "join search_engine.lemma l on l.id = i.lemma_id " +
            "WHERE page.id = :page_id", nativeQuery = true)
    List<LemmaEntity> getLemmasFromPage(@Param("page_id") Long pageId);



}
