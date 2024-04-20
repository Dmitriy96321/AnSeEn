package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

@Repository
public interface PagesRepository extends JpaRepository<PageEntity, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE page", nativeQuery = true)
    void truncateTablePage();
}
