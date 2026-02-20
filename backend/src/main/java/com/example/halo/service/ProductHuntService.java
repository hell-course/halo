package com.example.halo.service;

import com.example.halo.dto.ProductHuntPostDto;
import com.example.halo.dto.SearchQualityReportDto;
import com.example.halo.repository.ProductHuntItemRepository;
import com.example.halo.repository.ProductHuntSearchProjection;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductHuntService {
    private final ProductHuntItemRepository repository;
    private final EmbeddingService embeddingService;

    public ProductHuntService(ProductHuntItemRepository repository, EmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    public Mono<List<ProductHuntPostDto>> searchPosts(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Mono.just(Collections.emptyList());
        }

        float[] embedding = embeddingService.getEmbedding(query);
        if (embedding.length > 0) {
            try {
                List<ProductHuntSearchProjection> vectorRows = repository.searchByVector(vectorLiteral(embedding), limit);
                if (!vectorRows.isEmpty()) {
                    return Mono.just(vectorRows.stream().map(this::toDto).collect(Collectors.toList()));
                }
            } catch (Exception ignored) {
                // Fallback to keyword search below.
            }
        }

        List<ProductHuntSearchProjection> keywordRows = repository.searchByKeyword(query, limit);
        return Mono.just(keywordRows.stream().map(this::toDto).collect(Collectors.toList()));
    }

    public SearchQualityReportDto evaluateSearch(String query, int limit) {
        float[] embedding = embeddingService.getEmbedding(query);

        List<ProductHuntSearchProjection> vectorRows = Collections.emptyList();
        if (embedding.length > 0) {
            try {
                vectorRows = repository.searchByVector(vectorLiteral(embedding), limit);
            } catch (Exception ignored) {
                vectorRows = Collections.emptyList();
            }
        }
        List<ProductHuntSearchProjection> keywordRows = repository.searchByKeyword(query, limit);

        List<String> vectorIds = vectorRows.stream().map(ProductHuntSearchProjection::getExternalId).toList();
        List<String> keywordIds = keywordRows.stream().map(ProductHuntSearchProjection::getExternalId).toList();

        int overlap = (int) vectorIds.stream().filter(keywordIds::contains).count();
        double precisionAtK = limit == 0 ? 0.0 : (double) overlap / (double) limit;

        return new SearchQualityReportDto(
            query,
            limit,
            vectorRows.size(),
            keywordRows.size(),
            overlap,
            precisionAtK,
            vectorIds,
            keywordIds
        );
    }

    private ProductHuntPostDto toDto(ProductHuntSearchProjection row) {
        ProductHuntPostDto dto = new ProductHuntPostDto();
        dto.setId(row.getExternalId());
        dto.setName(row.getName());
        dto.setTagline(row.getTagline());
        dto.setDescription(row.getDescription());
        dto.setUrl(row.getProductHuntUrl());
        dto.setWebsite(row.getWebsiteUrl());
        dto.setVotesCount(row.getVotesCount());
        dto.setCreatedAt(row.getLaunchedAt());
        dto.setSimilarity(Objects.requireNonNullElse(row.getSimilarity(), 0.0));
        return dto;
    }

    private String vectorLiteral(float[] embedding) {
        return java.util.Arrays.toString(embedding);
    }
}
