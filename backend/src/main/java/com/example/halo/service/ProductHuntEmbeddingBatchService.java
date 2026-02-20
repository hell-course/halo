package com.example.halo.service;

import com.example.halo.domain.ProductHuntItem;
import com.example.halo.repository.ProductHuntItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductHuntEmbeddingBatchService {
    private static final Logger logger = LoggerFactory.getLogger(ProductHuntEmbeddingBatchService.class);

    private final ProductHuntItemRepository repository;
    private final EmbeddingService embeddingService;

    public ProductHuntEmbeddingBatchService(ProductHuntItemRepository repository, EmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    public int processBatch(int batchSize) {
        List<ProductHuntItem> candidates = repository.findEmbeddingCandidates(Instant.now(), PageRequest.of(0, batchSize));
        int successCount = 0;

        for (ProductHuntItem item : candidates) {
            String sourceText = buildEmbeddingText(item);
            float[] embedding = embeddingService.getEmbedding(sourceText);

            if (embedding.length > 0) {
                repository.updateEmbedding(item.getId(), Arrays.toString(embedding));
                successCount++;
                continue;
            }

            markFailure(item, "Embedding API returned empty vector.");
        }

        logger.info("Embedding batch finished. candidates={} success={}", candidates.size(), successCount);
        return successCount;
    }

    private String buildEmbeddingText(ProductHuntItem item) {
        String tagline = item.getTagline() == null ? "" : item.getTagline();
        String description = item.getDescription() == null ? "" : item.getDescription();
        return (item.getName() + " " + tagline + " " + description).trim();
    }

    private void markFailure(ProductHuntItem item, String message) {
        int retryCount = item.getEmbeddingRetryCount() + 1;
        item.setEmbeddingRetryCount(retryCount);
        item.setLastEmbeddingError(message);
        item.setNextEmbeddingRetryAt(Instant.now().plusSeconds(backoffSeconds(retryCount)));
        repository.save(item);
    }

    private long backoffSeconds(int retryCount) {
        long exp = Math.min(3600, (long) Math.pow(2, Math.min(retryCount, 10)) * 30L);
        return Math.max(30, exp);
    }
}
