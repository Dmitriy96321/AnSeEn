package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

import java.time.LocalDateTime;


@Repository
public interface SitesRepository extends JpaRepository<SiteEntity, Long> {


    @Modifying
    @Query("update SiteEntity s set s.statusTime = :value where s.id = :id")
    void setStatusTime(@Param("value") LocalDateTime value, @Param("id") long id);

}
