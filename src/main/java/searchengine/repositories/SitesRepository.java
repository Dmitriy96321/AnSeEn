package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;

import java.util.List;


@Repository
public interface SitesRepository extends JpaRepository<SiteEntity, Long> {
    boolean existsByStatus(StatusType statusType);
    List<SiteEntity> findAllByStatus(StatusType statusType);

}
