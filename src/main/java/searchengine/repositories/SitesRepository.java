package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

import java.util.Optional;

@Repository
public interface SitesRepository extends JpaRepository<SiteEntity, Long> {
    Optional<SiteEntity> findByUrl(String url);
    boolean existsByUrl(String url);
    boolean existsByStatus(StatusType statusType);

}
