package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;


@Repository
public interface SitesRepository extends JpaRepository<SiteEntity, Long> {
    SiteEntity getIdByUrl(String url);
    boolean existsByUrl(String url);
    boolean existsByStatus(StatusType statusType);

}
