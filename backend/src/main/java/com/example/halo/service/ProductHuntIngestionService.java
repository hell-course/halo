package com.example.halo.service;

import com.example.halo.domain.ProductHuntItem;
import com.example.halo.domain.ProductHuntSyncState;
import com.example.halo.dto.ProductHuntSyncResultDto;
import com.example.halo.dto.ProductHuntSyncStatusDto;
import com.example.halo.repository.ProductHuntItemRepository;
import com.example.halo.repository.ProductHuntSyncStateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;

@Service
public class ProductHuntIngestionService {
    private static final Logger logger = LoggerFactory.getLogger(ProductHuntIngestionService.class);
    private static final String SYNC_KEY = "default";
    private static final int MAX_PAGE_RETRIES = 3;

    private final ProductHuntItemRepository itemRepository;
    private final ProductHuntSyncStateRepository stateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${producthunt.api.url:https://api.producthunt.com/v2/api/graphql}")
    private String apiUrl;

    @Value("${producthunt.api.token:}")
    private String apiToken;

    @Value("${producthunt.ingestion.page-delay-ms:200}")
    private long pageDelayMs;

    @Value("${producthunt.ingestion.absolute-max-pages:1000}")
    private int absoluteMaxPages;

    public ProductHuntIngestionService(ProductHuntItemRepository itemRepository,
                                       ProductHuntSyncStateRepository stateRepository) {
        this.itemRepository = itemRepository;
        this.stateRepository = stateRepository;
    }

    public ProductHuntSyncResultDto syncIncremental(int perPage, int maxPages) {
        return syncInternal(perPage, maxPages, false, "incremental");
    }

    public ProductHuntSyncResultDto backfill(int perPage, int maxPages) {
        return syncInternal(perPage, maxPages, true, "backfill");
    }

    public ProductHuntSyncStatusDto getStatus() {
        ProductHuntSyncState state = loadState();
        return new ProductHuntSyncStatusDto(
            state.getSyncKey(),
            state.getLastCursor(),
            Boolean.TRUE.equals(state.getHasNextPage()),
            state.getNextRetryAt(),
            state.getConsecutiveFailures(),
            state.getLastError(),
            state.getLastRunStartedAt(),
            state.getLastRunFinishedAt(),
            state.getLastUpsertedCount(),
            state.getLastPageCount(),
            state.getTotalUpsertedCount()
        );
    }

    private ProductHuntSyncResultDto syncInternal(int perPage, int maxPages, boolean resetCursor, String mode) {
        int effectiveMaxPages = maxPages <= 0 ? absoluteMaxPages : Math.min(maxPages, absoluteMaxPages);
        ProductHuntSyncState state = loadState();
        if (apiToken == null || apiToken.isBlank()) {
            state.setLastError("producthunt.api.token is empty");
            stateRepository.save(state);
            logger.warn("Skipping Product Hunt ingestion: token is empty.");
            return new ProductHuntSyncResultDto(mode, 0, 0, Boolean.TRUE.equals(state.getHasNextPage()), state.getLastCursor(), 0);
        }

        if (state.getNextRetryAt() != null && state.getNextRetryAt().isAfter(Instant.now())) {
            return new ProductHuntSyncResultDto(mode, 0, 0, Boolean.TRUE.equals(state.getHasNextPage()), state.getLastCursor(), 0);
        }

        String cursor = resetCursor ? null : state.getLastCursor();
        boolean hasNextPage = true;
        int pagesProcessed = 0;
        int upserted = 0;
        int retries = 0;
        int attemptsWithoutProgress = 0;

        state.setLastRunStartedAt(Instant.now());
        state.setLastError(null);
        stateRepository.save(state);

        while (pagesProcessed < effectiveMaxPages && hasNextPage) {
            PageFetchResult page;
            try {
                page = fetchPageWithRetry(perPage, cursor);
            } catch (Exception e) {
                state.setConsecutiveFailures(state.getConsecutiveFailures() + 1);
                state.setLastError(trim(e.getMessage()));
                state.setNextRetryAt(Instant.now().plusSeconds(backoffSeconds(state.getConsecutiveFailures())));
                state.setLastRunFinishedAt(Instant.now());
                stateRepository.save(state);
                logger.error("Product Hunt ingestion failed after retries. cursor={}", cursor, e);
                return new ProductHuntSyncResultDto(mode, pagesProcessed, upserted, hasNextPage, cursor, retries + MAX_PAGE_RETRIES);
            }

            retries += page.retries();
            upserted += page.upserted();
            pagesProcessed++;

            if (cursor != null && cursor.equals(page.endCursor())) {
                attemptsWithoutProgress++;
            } else {
                attemptsWithoutProgress = 0;
            }

            cursor = page.endCursor();
            hasNextPage = page.hasNextPage();

            if (attemptsWithoutProgress >= 2) {
                logger.warn("Stopping ingestion due to cursor not advancing: {}", cursor);
                break;
            }

            safeSleep(pageDelayMs);
        }

        state.setConsecutiveFailures(0);
        state.setNextRetryAt(null);
        state.setLastError(null);
        state.setLastCursor(cursor);
        state.setHasNextPage(hasNextPage);
        state.setLastPageCount(pagesProcessed);
        state.setLastUpsertedCount(upserted);
        state.setTotalUpsertedCount(state.getTotalUpsertedCount() + upserted);
        state.setLastRunFinishedAt(Instant.now());
        stateRepository.save(state);

        return new ProductHuntSyncResultDto(mode, pagesProcessed, upserted, hasNextPage, cursor, retries);
    }

    private PageFetchResult fetchPageWithRetry(int perPage, String cursor) throws Exception {
        Exception last = null;
        for (int i = 1; i <= MAX_PAGE_RETRIES; i++) {
            try {
                PageFetchResult result = fetchPage(perPage, cursor);
                return new PageFetchResult(result.upserted(), result.hasNextPage(), result.endCursor(), i - 1);
            } catch (Exception e) {
                last = e;
                safeSleep(200L * i);
            }
        }
        throw last == null ? new RuntimeException("unknown ingestion failure") : last;
    }

    private PageFetchResult fetchPage(int perPage, String cursor) throws Exception {
        String body = """
            {
              "query": "query FetchPosts($first: Int!, $after: String) { posts(first: $first, after: $after) { edges { cursor node { id name tagline description url website votesCount createdAt } } pageInfo { hasNextPage endCursor } } }",
              "variables": { "first": %d, "after": %s }
            }
            """.formatted(perPage, cursor == null ? "null" : objectMapper.writeValueAsString(cursor));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiToken)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " body=" + trim(response.body()));
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode errors = root.path("errors");
        if (errors.isArray() && !errors.isEmpty()) {
            throw new IllegalStateException("GraphQL errors: " + trim(errors.toString()));
        }

        JsonNode posts = root.path("data").path("posts");
        JsonNode edges = posts.path("edges");
        if (!edges.isArray()) {
            return new PageFetchResult(0, false, cursor, 0);
        }

        int upserted = 0;
        Iterator<JsonNode> it = edges.elements();
        while (it.hasNext()) {
            JsonNode node = it.next().path("node");
            String externalId = node.path("id").asText("");
            if (externalId.isBlank()) {
                continue;
            }

            Optional<ProductHuntItem> existing = itemRepository.findByExternalId(externalId);
            ProductHuntItem item = existing.orElseGet(ProductHuntItem::new);
            item.setExternalId(externalId);
            item.setName(node.path("name").asText(""));
            item.setTagline(node.path("tagline").asText(null));
            item.setDescription(node.path("description").asText(null));
            item.setProductHuntUrl(node.path("url").asText(null));
            item.setWebsiteUrl(node.path("website").asText(null));
            item.setVotesCount(node.path("votesCount").isMissingNode() ? null : node.path("votesCount").asInt());

            String createdAt = node.path("createdAt").asText(null);
            if (createdAt != null && !createdAt.isBlank()) {
                try {
                    item.setLaunchedAt(Instant.parse(createdAt));
                } catch (Exception ignored) {
                    // Keep parsing resilient to API changes.
                }
            }

            item.setSourceUpdatedAt(Instant.now());
            itemRepository.save(item);
            upserted++;
        }

        JsonNode pageInfo = posts.path("pageInfo");
        boolean hasNextPage = pageInfo.path("hasNextPage").asBoolean(false);
        String endCursor = pageInfo.path("endCursor").asText(cursor);
        if (endCursor != null && endCursor.isBlank()) {
            endCursor = cursor;
        }

        return new PageFetchResult(upserted, hasNextPage, endCursor, 0);
    }

    private ProductHuntSyncState loadState() {
        return stateRepository.findById(SYNC_KEY).orElseGet(() -> {
            ProductHuntSyncState state = new ProductHuntSyncState();
            state.setSyncKey(SYNC_KEY);
            state.setHasNextPage(true);
            return stateRepository.save(state);
        });
    }

    private long backoffSeconds(int failures) {
        long exp = Math.min(3600, (long) Math.pow(2, Math.min(failures, 8)) * 30L);
        return Math.max(30, exp);
    }

    private void safeSleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String trim(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.length() > 500 ? raw.substring(0, 500) : raw;
    }

    private record PageFetchResult(int upserted, boolean hasNextPage, String endCursor, int retries) { }
}
