package com.example.halo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmbeddingService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Stable Gemini embeddings model (Gemini API)
    private static final String MODEL = "gemini-embedding-001";
    private static final int OUTPUT_DIMENSIONALITY = 384;

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent?key=%s";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[0];
        }

        try {
            String requestBody = """
            {
              "content": {
                "parts": [{
                  "text": %s
                }]
              },
              "outputDimensionality": %d
            }
            """.formatted(objectMapper.writeValueAsString(text), OUTPUT_DIMENSIONALITY);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT.formatted(MODEL, apiKey)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ Gemini Embedding HTTP Error: " + response.statusCode());
                System.err.println(response.body());
                return new float[0];
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode valuesNode = root
                    .path("embedding")
                    .path("values");

            if (!valuesNode.isArray()) {
                return new float[0];
            }

            float[] embedding = new float[valuesNode.size()];
            for (int i = 0; i < valuesNode.size(); i++) {
                embedding[i] = (float) valuesNode.get(i).asDouble();
            }

            return embedding;

        } catch (Exception e) {
            System.err.println("❌ Gemini Embedding REST Error: " + e.getMessage());
            return new float[0];
        }
    }
}
