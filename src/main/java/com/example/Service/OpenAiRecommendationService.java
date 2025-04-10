package com.example.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.dtos.ChatCompletionRequest;
import com.example.dtos.ChatCompletionResponse;
import com.example.dtos.MyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@Service
public class OpenAiRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiRecommendationService.class);

    @Value("${app.api-key}")
    private String API_KEY;

    @Value("${app.url}")
    public String URL;

    @Value("${app.model}")
    public String MODEL;

    @Value("${app.temperature}")
    public double TEMPERATURE;

    @Value("${app.max_tokens}")
    public int MAX_TOKENS;

    @Value("${app.frequency_penalty}")
    public double FREQUENCY_PENALTY;

    @Value("${app.presence_penalty}")
    public double PRESENCE_PENALTY;

    @Value("${app.top_p}")
    public double TOP_P;

    private final WebClient client;

    public OpenAiRecommendationService() {
        this.client = WebClient.create();
    }

    public OpenAiRecommendationService(WebClient client) {
        this.client = client;
    }

    public MyResponse makeRequest(String userPrompt, String systemMessage) {
        System.out.println("Making request to " + userPrompt + " " + systemMessage);
        System.out.println(API_KEY);
        System.out.println(URL);
        System.out.println(MODEL);
        ChatCompletionRequest requestDto = new ChatCompletionRequest();
        requestDto.setModel(MODEL);
        requestDto.setTemperature(TEMPERATURE);
        requestDto.setMax_tokens(MAX_TOKENS);
        requestDto.setTop_p(TOP_P);
        requestDto.setFrequency_penalty(FREQUENCY_PENALTY);
        requestDto.setPresence_penalty(PRESENCE_PENALTY);
        requestDto.getMessages().add(new ChatCompletionRequest.Message("system", systemMessage));
        requestDto.getMessages().add(new ChatCompletionRequest.Message("user", userPrompt));

        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(requestDto);
            logger.info("Sending request to OpenAI: " + json);

            ChatCompletionResponse response = client.post()
                    .uri(new URI(URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();

            String responseMsg = response.getChoices().get(0).getMessage().getContent();
            int tokensUsed = response.getUsage().getTotal_tokens();

            logger.info("Tokens used: {}", tokensUsed);
            logger.info("Estimated cost: ${}", String.format("%.6f", tokensUsed * 0.0015 / 1000));

            return new MyResponse(responseMsg);

        } catch (WebClientResponseException e) {
            logger.error("OpenAI API error: " + e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Fejl fra OpenAI – prøv igen senere.");
        } catch (Exception e) {
            logger.error("General error", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Intern fejl – prøv igen senere.");
        }
    }
}
