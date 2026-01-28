package com.example.halo.service;

import com.google.genai.Client;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentResponse;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class GeminiService {

    private final Client geminiClient;
    private final String embeddingModel = "models/text-embedding-004";

    public GeminiService(@Value("${gemini.api.key}") String apiKey) throws IOException {
        this.geminiClient = Client.builder().apiKey(apiKey).build();
    }

    // Model for text generation
    private final String generationModel = "gemini-pro"; // Using gemini-pro for text generation

    public String generateContent(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "";
        }

        try {
            GenerateContentResponse response = geminiClient.models.generateContent(generationModel, prompt, null);
            return response.text();
        } catch (Exception e) {
            System.err.println("Error generating content from Gemini API: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[0];
        }

        try {
            // The third argument for EmbedContentConfig is required, passing null for default behavior.
            EmbedContentResponse response = geminiClient.models.embedContent(embeddingModel, text, null);

            Optional<List<ContentEmbedding>> embeddingsOptional = response.embeddings();
            if (embeddingsOptional.isPresent() && !embeddingsOptional.get().isEmpty()) {
                ContentEmbedding embedding = embeddingsOptional.get().get(0);
                Optional<List<Float>> valuesOptional = embedding.values();

                if (valuesOptional.isPresent()) {
                    List<Float> embeddingList = valuesOptional.get();
                    float[] embeddingArray = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        embeddingArray[i] = embeddingList.get(i);
                    }
                    return embeddingArray;
                }
            }
            
            System.err.println("Error: Received null or empty embedding from Gemini API.");
            return new float[0];

        } catch (Exception e) {
            System.err.println("Error getting embedding from Gemini API: " + e.getMessage());
            e.printStackTrace();
            return new float[0]; // Return empty array on error
        }
    }
}
