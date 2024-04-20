package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

import java.time.LocalDateTime;


@Repository
public interface SitesRepository extends JpaRepository<SiteEntity, Long> {
    


    @Modifying
    @Transactional
    @Query("update SiteEntity s set s.status = :value where s.id = :id")
    void setStatusBySite(@Param("value") StatusType value, @Param("id") long id);

    @Modifying
    @Transactional
    @Query("update SiteEntity s set s.statusTime = :value where s.id = :id")
    void setStatusTime(@Param("value") LocalDateTime value, @Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE site", nativeQuery = true)
    void truncateTableSite();

}
