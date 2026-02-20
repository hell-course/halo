package com.example.halo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product_hunt_item")
public class ProductHuntItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String name;

    private String tagline;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "product_hunt_url")
    private String productHuntUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "votes_count")
    private Integer votesCount;

    @Column(name = "launched_at")
    private Instant launchedAt;

    @Column(name = "source_updated_at")
    private Instant sourceUpdatedAt;

    @Column(name = "embedding_retry_count", nullable = false)
    private int embeddingRetryCount = 0;

    @Column(name = "last_embedding_error", columnDefinition = "text")
    private String lastEmbeddingError;

    @Column(name = "next_embedding_retry_at")
    private Instant nextEmbeddingRetryAt;

    @Column(name = "embedding_updated_at")
    private Instant embeddingUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
