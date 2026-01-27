package com.example.halo.service;

import com.example.halo.dto.ProductHuntPostDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductHuntService {

    private static final Logger logger = LoggerFactory.getLogger(ProductHuntService.class);

    private final WebClient webClient;
    private final String productHuntApiKey;
    private final String productHuntApiSecret;
    private final GeminiService geminiService; // Injected GeminiService

    private final String productHuntDeveloperToken; // Injected developer token
    // Removed accessToken field as developer token is used directly

    public ProductHuntService(@Value("${producthunt.api.key}") String productHuntApiKey,
                              @Value("${producthunt.api.secret}") String productHuntApiSecret,
                              @Value("${producthunt.api.developer-token}") String productHuntDeveloperToken, // New
                              GeminiService geminiService) {
        this.productHuntApiKey = productHuntApiKey;
        this.productHuntApiSecret = productHuntApiSecret;
        this.productHuntDeveloperToken = productHuntDeveloperToken; // Assign developer token
        this.geminiService = geminiService;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.producthunt.com/v2/api/graphql")
                .build();
    }

    // Helper method to calculate cosine similarity between two embeddings
    private double cosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null || embedding1.length == 0 || embedding2.length == 0 || embedding1.length != embedding2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // Method to get or refresh the access token using developer token
    private Mono<String> getAccessToken() {
        logger.info("Using Product Hunt Developer Token for authentication.");
        return Mono.just(productHuntDeveloperToken);
    }


    public Mono<List<ProductHuntPostDto>> searchPosts(String query, int first) {
        logger.info("Starting market research search for query: '{}'", query);
        return getAccessToken().flatMap(token -> {
            String graphQlQuery = buildGraphQLQuery(query, first); // Pass query to buildGraphQLQuery

            Map<String, Object> body = new HashMap<>();
            body.put("query", graphQlQuery);

            logger.info("Sending GraphQL query to Product Hunt: {}", body); // Log the full query body

            return webClient.post()
                    .uri("")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // Check for GraphQL errors
                        if (response.containsKey("errors")) {
                            logger.error("GraphQL response contains errors: {}", response.get("errors"));
                            return Collections.<ProductHuntPostDto>emptyList(); // Return empty list on error
                        }

                        Map<String, Object> data = (Map<String, Object>) response.get("data");
                        List<ProductHuntPostDto> productHuntPosts = Collections.emptyList();

                        if (data != null && data.containsKey("posts")) {
                            Map<String, Object> postsConnection = (Map<String, Object>) data.get("posts");
                            if (postsConnection != null && postsConnection.containsKey("edges")) {
                                List<Map<String, Object>> edges = (List<Map<String, Object>>) postsConnection.get("edges");
                                if (edges != null) {
                                    productHuntPosts = edges.stream()
                                            .map(edge -> (Map<String, Object>) edge.get("node"))
                                            .map(node -> {
                                                ProductHuntPostDto post = new ProductHuntPostDto();
                                                post.setId(String.valueOf(node.get("id")));
                                                post.setName((String) node.get("name"));
                                                post.setTagline((String) node.get("tagline"));
                                                post.setDescription((String) node.get("description"));
                                                post.setUrl((String) node.get("url"));
                                                post.setWebsite((String) node.get("website"));
                                                post.setVotesCount((Integer) node.get("votesCount"));
                                                post.setCreatedAt((String) node.get("createdAt"));

                                                Map<String, Object> thumbnailMap = (Map<String, Object>) node.get("thumbnail");
                                                if (thumbnailMap != null) {
                                                    post.setThumbnail(new ProductHuntPostDto.Media((String) thumbnailMap.get("url")));
                                                }

                                                Map<String, Object> topicsConnectionMap = (Map<String, Object>) node.get("topics");
                                                if (topicsConnectionMap != null) {
                                                    List<Map<String, Object>> topicEdges = (List<Map<String, Object>>) topicsConnectionMap.get("edges");
                                                    if (topicEdges != null) {
                                                        List<ProductHuntPostDto.TopicEdge> mappedTopicEdges = topicEdges.stream()
                                                                .map(topicEdgeMap -> {
                                                                    Map<String, Object> topicNodeMap = (Map<String, Object>) topicEdgeMap.get("node");
                                                                    return new ProductHuntPostDto.TopicEdge(new ProductHuntPostDto.TopicNode((String) topicNodeMap.get("name")));
                                                                })
                                                                .collect(java.util.stream.Collectors.toList());
                                                        post.setTopics(new ProductHuntPostDto.TopicsConnection(mappedTopicEdges));
                                                    }
                                                }
                                                return post;
                                            })
                                            .collect(java.util.stream.Collectors.toList());
                                }
                            }
                        }

                        logger.info("Fetched {} posts from Product Hunt API.", productHuntPosts.size());

                        // Semantic filtering using GeminiService
                        if (query != null && !query.trim().isEmpty() && !productHuntPosts.isEmpty()) {
                            try {
                                float[] queryEmbedding = geminiService.getEmbedding(query);
                                if (queryEmbedding.length > 0) {
                                    productHuntPosts.forEach(post -> {
                                        String postText = String.format("%s. %s %s",
                                                post.getName() != null ? post.getName() : "",
                                                post.getTagline() != null ? post.getTagline() : "",
                                                post.getDescription() != null ? post.getDescription() : "");
                                        float[] postEmbedding = geminiService.getEmbedding(postText);
                                        if (postEmbedding.length > 0) {
                                            double similarity = cosineSimilarity(queryEmbedding, postEmbedding);
                                            post.setSimilarity(similarity); // Add a transient field for similarity in DTO
                                        }
                                    });

                                    // Sort by similarity in descending order
                                    productHuntPosts.sort(Comparator.comparingDouble(ProductHuntPostDto::getSimilarity).reversed());
                                    logger.info("Finished semantic filtering. Returning {} posts.", productHuntPosts.size());
                                } else {
                                    logger.warn("Could not get embedding for query: '{}'. Skipping semantic filtering and returning raw list.", query);
                                }
                            } catch (Exception e) {
                                logger.warn("Error during Gemini embedding process: {}. Returning raw list without semantic filtering.", e.getMessage());
                                // Return raw list if embedding fails due to Gemini API error (e.g., 429)
                                return productHuntPosts;
                            }
                        } else {
                            logger.info("No query provided or no posts fetched. Returning raw list of {} posts.", productHuntPosts.size());
                        }
                        return productHuntPosts;
                    });
        });
    }

    private String buildGraphQLQuery(String query, int first) { // query parameter is now only for logging/context
        // Product Hunt API 'posts' field does not accept a direct 'search' argument.
        // We will fetch posts ordered by VOTES and then perform semantic filtering using GeminiService.
        // The 'query' parameter is used for semantic filtering *after* fetching.

        return String.format("""
            query {
              posts(first: %d, order: VOTES) { # order can be RANKING, VOTES, NEWEST. User requested VOTES.
                edges {
                  node {
                    id
                    name
                    tagline
                    description
                    url
                    website
                    votesCount
                    createdAt
                    thumbnail {
                      url
                    }
                    topics {
                      edges {
                        node {
                          name
                        }
                      }
                    }
                  }
                }
              }
            }
            """, first);
    }
}
