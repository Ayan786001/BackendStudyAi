package com.example.dtos;

public class RecommendationRequest {
    private String input;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public RecommendationRequest(String input) {
        this.input = input;
    }
}
