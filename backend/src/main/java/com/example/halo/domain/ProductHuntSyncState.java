package com.example.halo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "product_hunt_sync_state")
public class ProductHuntSyncState {
    @Id
    @Column(name = "sync_key", nullable = false, length = 64)
    private String syncKey;

    @Column(name = "last_cursor")
    private String lastCursor;

    @Column(name = "has_next_page")
    private Boolean hasNextPage = true;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures = 0;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "last_run_started_at")
    private Instant lastRunStartedAt;

    @Column(name = "last_run_finished_at")
    private Instant lastRunFinishedAt;

    @Column(name = "last_upserted_count", nullable = false)
    private int lastUpsertedCount = 0;

    @Column(name = "last_page_count", nullable = false)
    private int lastPageCount = 0;

    @Column(name = "total_upserted_count", nullable = false)
    private long totalUpsertedCount = 0;

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
