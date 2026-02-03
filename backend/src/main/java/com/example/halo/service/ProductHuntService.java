package com.example.halo.service;

import com.example.halo.dto.ProductHuntPostDto;
import org.springframework.stereotype.Service;


import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Comparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class ProductHuntService {

    private static final Logger logger = LoggerFactory.getLogger(ProductHuntService.class);

    private final GeminiService geminiService; // Injected GeminiService

    // ProductHuntService no longer directly interacts with Product Hunt API

    public ProductHuntService(GeminiService geminiService) {
        this.geminiService = geminiService;
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




    public Mono<List<ProductHuntPostDto>> searchPosts(String query, int first) {
        logger.info("Starting market research search for query: '{}' using Gemini generation.", query);

        if (query == null || query.trim().isEmpty()) {
            logger.warn("Query is empty. Returning empty list.");
            return Mono.just(Collections.emptyList());
        }

        // Construct the prompt for Gemini
        String prompt = String.format(
            "사용자가 '%s' 아이디어를 냈어. 이와 유사한 실제 존재하는 서비스나 앱 5개를 찾아서 알려줘. 각 서비스의 이름, 간단한 설명(tagline), 그리고 가상의 투표수(votesCount)를 JSON 형식으로 줘. 응답은 반드시 JSON 배열만 포함해야 해. Respond with raw JSON array only, no markdown.",
            query
        );
        logger.info("Sending prompt to Gemini for generation: {}", prompt);

        String generatedJson = ""; // Declare outside try block for catch accessibility
        try {
            generatedJson = geminiService.generateContent(prompt); // Assign value here

            logger.info("Received raw generated JSON from Gemini: {}", generatedJson);
            System.out.println("Raw Gemini Response: " + generatedJson); // Debugging log

            if (generatedJson == null || generatedJson.trim().isEmpty()) {
                logger.error("Gemini returned empty response. Abort parsing.");
                return Mono.just(Collections.emptyList());
            }
            // Robust JSON extraction
            int startIndex = generatedJson.indexOf("[");
            int endIndex = generatedJson.lastIndexOf("]");

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                generatedJson = generatedJson.substring(startIndex, endIndex + 1);
            } else {
                // If we can't find a JSON array, try cleaning markdown blocks anyway as a fallback
                generatedJson = generatedJson.replace("```json", "").replace("```", "").trim();
                logger.warn("Could not find a valid JSON array structure (start '[' and end ']') in Gemini response. Proceeding with markdown cleanup fallback. Cleaned JSON: {}", generatedJson);
            }
            logger.info("Prepared JSON for parsing: {}", generatedJson); // Log the prepared JSON

            // Parse the JSON into a list of ProductHuntPostDto
            ObjectMapper objectMapper = new ObjectMapper();
            List<ProductHuntPostDto> generatedPosts = objectMapper.readValue(generatedJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ProductHuntPostDto.class));

            // Populate other fields and assign dummy similarity (since it's generated data)
            // Or remove similarity if it's not relevant for generated data.
            // For now, let's assign a high dummy similarity.
            generatedPosts.forEach(post -> {
                if (post.getId() == null) post.setId(String.valueOf(System.nanoTime())); // Ensure ID if not generated
                if (post.getUrl() == null) post.setUrl("https://example.com/generated");
                if (post.getWebsite() == null) post.setWebsite("https://example.com");
                if (post.getCreatedAt() == null) post.setCreatedAt(java.time.Instant.now().toString());
                if (post.getVotesCount() == null) post.setVotesCount(new java.util.Random().nextInt(1000) + 100); // Random votes

                // Assign a high dummy similarity as it's directly generated
                post.setSimilarity(0.95 + (new java.util.Random().nextDouble() * 0.05)); // High similarity
            });

            logger.info("Successfully parsed {} generated posts from Gemini.", generatedPosts.size());
            return Mono.just(generatedPosts);

        } catch (Exception e) {
            logger.error("Error generating or parsing content from Gemini: {}. Raw response was: {}", e.getMessage(), generatedJson, e);

            // Fallback: return a dummy post to prevent complete failure
            ProductHuntPostDto dummyPost = new ProductHuntPostDto();
            dummyPost.setId("dummy-" + System.nanoTime());
            dummyPost.setName("AI 분석 결과 파싱 실패");
            dummyPost.setTagline("Gemini 응답을 처리하는 중 오류가 발생했습니다. (임시 결과)");
            dummyPost.setDescription("Gemini가 JSON 형식으로 응답하지 않았거나, 응답 파싱에 실패했습니다.");
            dummyPost.setVotesCount(0);
            dummyPost.setSimilarity(0.0);
            dummyPost.setUrl("#");
            dummyPost.setWebsite("#");
            dummyPost.setCreatedAt(java.time.Instant.now().toString());

            return Mono.just(Collections.singletonList(dummyPost));
        }
    }
}
