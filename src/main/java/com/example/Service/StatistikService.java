package com.example.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StatistikService {

    private final WebClient webClient;

    public StatistikService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.statbank.dk/v1/data")
                .build();
    }

    public String fetchAdmissionStats() {
        String requestBody = """
            {
              "variables": [
                { "code": "KØN", "values": ["1", "2"] },
                { "code": "TID", "values": ["2022"] }
              ]
            }
        """;

        return webClient.post()
                .uri("/UDDAKT10/JSONSTAT") // This table is for higher education admission stats
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // You can later convert JSON to a Java class if needed
    }
}
