package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

@Repository
public interface PagesRepository extends JpaRepository<PageEntity, Long> {
}
