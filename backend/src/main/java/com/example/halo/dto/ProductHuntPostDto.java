package com.example.halo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductHuntPostDto {
    private String id;
    private String name;
    private String tagline;
    private String description;
    private String url; // Product Hunt URL
    private String website; // Product's actual website URL
    private Integer votesCount;
    private String createdAt; // ISO 8601 format
    private double similarity; // Field for semantic similarity score

    private Media thumbnail; // Nested object for thumbnail

    private TopicsConnection topics; // Nested object for topics

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Media {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicsConnection {
        private List<TopicEdge> edges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopicEdge {
        private TopicNode node;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicNode {
        private String name;
    }
}
