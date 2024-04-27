package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE lemma", nativeQuery = true)
    void truncateTableLemma();

    @Modifying
    @Transactional
    @Query("update LemmaEntity l set l.frequency = :value where l.id = :id")
    void updateLemmaFrequency(@Param("value") Integer value, @Param("id") long id);

//    @Modifying
//    @Transactional
    @Query("select l from LemmaEntity l where l.siteId = :siteId AND l.lemma = :lemma" )
    LemmaEntity findBySiteIdAndAndLemma(@Param("siteId") SiteEntity siteId, @Param("lemma") String lemma);
//
//    @Transactional
//    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LemmaEntity l WHERE l.lemma = :lemma")
//    boolean existsLemmaBySite(@Param("siteId") SiteEntity siteId, @Param("lemma") String lemma);


//     save(LemmaEntity lemma);

//    @Transactional
//    @Query("update LemmaEntity l set l.frequency = :site_id where l.id = :lemma")
//    void existsLemmaBySite(@Param("site_id") Long site_id, @Param("lemma") String lemma);



}
