package com.example.halo.repository;

import com.example.halo.domain.ProductHuntItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductHuntItemRepository extends JpaRepository<ProductHuntItem, Long> {
    Optional<ProductHuntItem> findByExternalId(String externalId);

    @Query("SELECT p FROM ProductHuntItem p WHERE p.embeddingUpdatedAt IS NULL OR (p.nextEmbeddingRetryAt IS NOT NULL AND p.nextEmbeddingRetryAt <= :now)")
    List<ProductHuntItem> findEmbeddingCandidates(@Param("now") Instant now, Pageable pageable);

    @Query(value = """
        SELECT
          external_id AS externalId,
          name AS name,
          tagline AS tagline,
          description AS description,
          product_hunt_url AS productHuntUrl,
          website_url AS websiteUrl,
          votes_count AS votesCount,
          CAST(launched_at AS text) AS launchedAt,
          GREATEST(0.0, 1 - (embedding <=> CAST(:embedding AS vector))) AS similarity
        FROM product_hunt_item
        WHERE embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ProductHuntSearchProjection> searchByVector(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = """
        SELECT
          external_id AS externalId,
          name AS name,
          tagline AS tagline,
          description AS description,
          product_hunt_url AS productHuntUrl,
          website_url AS websiteUrl,
          votes_count AS votesCount,
          CAST(launched_at AS text) AS launchedAt,
          0.0 AS similarity
        FROM product_hunt_item
        WHERE LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(COALESCE(tagline, '')) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(COALESCE(description, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY votes_count DESC NULLS LAST, created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<ProductHuntSearchProjection> searchByKeyword(@Param("query") String query, @Param("limit") int limit);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE product_hunt_item
        SET embedding = CAST(:embedding AS vector),
            embedding_updated_at = NOW(),
            embedding_retry_count = 0,
            last_embedding_error = NULL,
            next_embedding_retry_at = NULL,
            updated_at = NOW()
        WHERE id = :id
        """, nativeQuery = true)
    int updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);
}
