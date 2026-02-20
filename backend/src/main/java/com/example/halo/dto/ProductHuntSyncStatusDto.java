package com.example.halo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHuntSyncStatusDto {
    private String syncKey;
    private String lastCursor;
    private boolean hasNextPage;
    private Instant nextRetryAt;
    private int consecutiveFailures;
    private String lastError;
    private Instant lastRunStartedAt;
    private Instant lastRunFinishedAt;
    private int lastUpsertedCount;
    private int lastPageCount;
    private long totalUpsertedCount;
}
