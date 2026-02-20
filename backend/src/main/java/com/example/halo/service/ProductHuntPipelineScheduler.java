package com.example.halo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ProductHuntPipelineScheduler {
    private final ProductHuntIngestionService ingestionService;
    private final ProductHuntEmbeddingBatchService embeddingBatchService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${producthunt.ingestion.limit:30}")
    private int ingestionLimit;

    @Value("${producthunt.ingestion.max-pages:5}")
    private int ingestionMaxPages;

    @Value("${producthunt.embedding.batch-size:30}")
    private int embeddingBatchSize;

    @Value("${producthunt.backfill.limit:50}")
    private int backfillLimit;

    @Value("${producthunt.backfill.max-pages:20}")
    private int backfillMaxPages;

    public ProductHuntPipelineScheduler(ProductHuntIngestionService ingestionService,
                                        ProductHuntEmbeddingBatchService embeddingBatchService) {
        this.ingestionService = ingestionService;
        this.embeddingBatchService = embeddingBatchService;
    }

    @Scheduled(cron = "${producthunt.ingestion.cron:0 */30 * * * *}")
    public void scheduledIngestion() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            ingestionService.syncIncremental(ingestionLimit, ingestionMaxPages);
        } finally {
            running.set(false);
        }
    }

    @Scheduled(cron = "${producthunt.embedding.cron:0 */5 * * * *}")
    public void scheduledEmbedding() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            embeddingBatchService.processBatch(embeddingBatchSize);
        } finally {
            running.set(false);
        }
    }

    @Scheduled(cron = "${producthunt.backfill.cron:0 0 3 * * *}")
    public void scheduledBackfill() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            ingestionService.backfill(backfillLimit, backfillMaxPages);
        } finally {
            running.set(false);
        }
    }
}
