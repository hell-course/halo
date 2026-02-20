package com.example.halo.controller;

import com.example.halo.dto.ProductHuntPostDto;
import com.example.halo.dto.ProductHuntSyncResultDto;
import com.example.halo.dto.ProductHuntSyncStatusDto;
import com.example.halo.dto.SearchQualityReportDto;
import com.example.halo.service.ProductHuntEmbeddingBatchService;
import com.example.halo.service.ProductHuntIngestionService;
import com.example.halo.service.ProductHuntService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market-research")
public class MarketResearchController {

    private final ProductHuntService productHuntService;
    private final ProductHuntIngestionService ingestionService;
    private final ProductHuntEmbeddingBatchService embeddingBatchService;

    public MarketResearchController(ProductHuntService productHuntService,
                                    ProductHuntIngestionService ingestionService,
                                    ProductHuntEmbeddingBatchService embeddingBatchService) {
        this.productHuntService = productHuntService;
        this.ingestionService = ingestionService;
        this.embeddingBatchService = embeddingBatchService;
    }

    @GetMapping("/search")
    public Mono<List<ProductHuntPostDto>> searchProductHuntPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return productHuntService.searchPosts(query, limit);
    }

    @PostMapping("/sync")
    public ProductHuntSyncResultDto syncProductHuntData(
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "5") int maxPages) {
        return ingestionService.syncIncremental(limit, maxPages);
    }

    @PostMapping("/backfill")
    public ProductHuntSyncResultDto backfillProductHuntData(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "20") int maxPages) {
        return ingestionService.backfill(limit, maxPages);
    }

    @GetMapping("/sync-status")
    public ProductHuntSyncStatusDto getSyncStatus() {
        return ingestionService.getStatus();
    }

    @PostMapping("/embed")
    public Map<String, Integer> processEmbeddingBatch(@RequestParam(defaultValue = "30") int batchSize) {
        int success = embeddingBatchService.processBatch(batchSize);
        return Map.of("embedded", success);
    }

    @GetMapping("/evaluate")
    public SearchQualityReportDto evaluateSearchQuality(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return productHuntService.evaluateSearch(query, limit);
    }
}
