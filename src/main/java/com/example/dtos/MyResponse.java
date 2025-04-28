package com.example.dtos;


public class MyResponse {
    private String response;
    private String recommendedEducation;
    private String explanation;

    public MyResponse(String response) {
        this.response = response;
        this.recommendedEducation = recommendedEducation;
        this.explanation = explanation;
    }



    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRecommendedEducation() {
        return recommendedEducation;
    }

    public void setRecommendedEducation(String recommendedEducation) {
        this.recommendedEducation = recommendedEducation;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

}

