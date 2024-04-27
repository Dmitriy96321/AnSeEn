package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

@Repository
public interface IndexesRepository extends JpaRepository<IndexEntity, Long> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE indexes", nativeQuery = true)
    void truncateTableIndexes();
}
