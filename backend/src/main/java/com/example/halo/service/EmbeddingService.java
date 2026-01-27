package com.example.halo.service;

import com.pgvector.PGvector;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final GeminiService geminiService;

    public EmbeddingService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public PGvector embed(String text) {
        float[] embedding = geminiService.getEmbedding(text);
        if (embedding.length == 0) {
            // Handle error or return a default/empty vector
            // For now, returning a vector of zeros if embedding fails or is empty
            return new PGvector(new float[384]); // Assuming 384 is the expected dimension
        }
        return new PGvector(embedding);
    }
}
