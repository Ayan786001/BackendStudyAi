package com.example.dtos;

public class RecommendationResponse {
    private String recommendation;

    public RecommendationResponse(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
