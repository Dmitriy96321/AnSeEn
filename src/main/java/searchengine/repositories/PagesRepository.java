package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;


@Repository
public interface PagesRepository extends JpaRepository<PageEntity, Long> {


    @Modifying
    @Query(value = "TRUNCATE TABLE page", nativeQuery = true)
    void truncateTablePage();


    @Query(value = "select * from page p where p.path = :path", nativeQuery = true)
    PageEntity findByPageUrl(@Param("path") String path);











}
