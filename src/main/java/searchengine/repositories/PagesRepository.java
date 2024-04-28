package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

@Repository
public interface PagesRepository extends JpaRepository<PageEntity, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE page", nativeQuery = true)
    void truncateTablePage();

    @Transactional
    @Query(value = "select * from page p where p.path = :path", nativeQuery = true)
    PageEntity findByPageUrl(@Param("path") String path);



    @Transactional
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PageEntity p WHERE p.path = :path")
    boolean existsByPageUrl(@Param("path") String path);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM page WHERE id NOT IN (SELECT MIN(id) FROM page GROUP BY path)", nativeQuery = true)
    void deleteDuplicatePages();



}
