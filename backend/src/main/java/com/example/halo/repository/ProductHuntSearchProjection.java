package com.example.halo.repository;

public interface ProductHuntSearchProjection {
    String getExternalId();
    String getName();
    String getTagline();
    String getDescription();
    String getProductHuntUrl();
    String getWebsiteUrl();
    Integer getVotesCount();
    String getLaunchedAt();
    Double getSimilarity();
}
