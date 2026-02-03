package com.example.halo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // ✅ REST에서 정상 동작하는 모델
    private static final String MODEL = "gemini-2.5-flash-lite";

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        // [중요] 키가 제대로 로드되었는지 로그로 확인 (보안상 앞 4자리만 출력)
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("🚨 [오류] API 키가 로드되지 않았습니다! application-secret.yml 설정을 확인하세요.");
        } else {
            // 키가 있으면 앞 4글자만 보여줌 (예: AIza****)
            String maskedKey = (apiKey.length() > 4) ? apiKey.substring(0, 4) + "****" : "****";
            System.out.println("✅ [성공] API 키 로드 완료: " + maskedKey);
        }
        this.apiKey = apiKey; // Assign to field
    }

    public String generateContent(String prompt) {
        if (prompt == null || prompt.trim().isEmpty() || apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ Gemini API 요청 실패: 프롬프트 또는 API 키가 유효하지 않습니다.");
            return "";
        }

        try {
            System.out.println("🚀 Gemini API 요청 시작 (Generate Model: " + MODEL + ")");
            String requestBody = """
            {
              "contents": [{
                "parts": [{
                  "text": %s
                }]
              }]
            }
            """.formatted(objectMapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT.formatted(MODEL, apiKey)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ Gemini GenerateContent HTTP Error: " + response.statusCode());
                System.err.println(response.body());
                return "";
            }

            JsonNode root = objectMapper.readTree(response.body());

            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText("");

        } catch (Exception e) {
            System.err.println("❌ Gemini GenerateContent REST Error: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty() || apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ Gemini API 요청 실패: 텍스트 또는 API 키가 유효하지 않습니다.");
            return new float[0];
        }

        try {
            System.out.println("🚀 Gemini API 요청 시작 (Embedding Model: " + "text-embedding-004" + ")"); // Hardcode for now
            String requestBody = """
            {
              "content": {
                "parts": [{
                  "text": %s
                }]
              }
            }
            """.formatted(objectMapper.writeValueAsString(text));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=%s".formatted(apiKey)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ Gemini EmbedContent HTTP Error: " + response.statusCode());
                System.err.println(response.body());
                return new float[0];
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode valuesNode = root
                    .path("embedding")
                    .path("values");

            if (!valuesNode.isArray()) {
                System.err.println("⚠️ Gemini Embedding: 'values' field is not an array in response.");
                return new float[0];
            }

            float[] embedding = new float[valuesNode.size()];
            for (int i = 0; i < valuesNode.size(); i++) {
                embedding[i] = (float) valuesNode.get(i).asDouble();
            }
            System.out.println("✅ Gemini Embedding 성공 (Dimension: " + embedding.length + ")");
            return embedding;

        } catch (Exception e) {
            System.err.println("❌ Gemini EmbedContent REST Error: " + e.getMessage());
            e.printStackTrace();
            return new float[0];
        }
    }
}
