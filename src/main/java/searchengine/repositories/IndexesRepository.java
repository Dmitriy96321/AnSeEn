package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexesRepository extends JpaRepository<IndexEntity, Long> {


    @Modifying
    @Query(value = "TRUNCATE TABLE indexes", nativeQuery = true)
    void truncateTableIndexes();

    @Modifying
    @Query(value = "DELETE FROM search_engine.indexes WHERE page_id = :pageId", nativeQuery = true)
    void deleteIndexesByPageId(@Param("pageId") Long pageId);
}
