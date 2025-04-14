package com.example.Service;

import com.example.dtos.ChatCompletionRequest;
import com.example.dtos.ChatCompletionResponse;
import com.example.dtos.MyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final WebClient client;
    private final StatistikService statistikService;
    private final String API_KEY;
    private final String URL;
    private final String MODEL;
    private final double TEMPERATURE;
    private final int MAX_TOKENS;
    private final double FREQUENCY_PENALTY;
    private final double PRESENCE_PENALTY;
    private final double TOP_P;

    public OpenAiRecommendationService(
            WebClient.Builder webClientBuilder,
            StatistikService statistikService,
            @Value("${app.api-key}") String apiKey,
            @Value("${app.url}") String url,
            @Value("${app.model}") String model,
            @Value("${app.temperature}") double temperature,
            @Value("${app.max_tokens}") int maxTokens,
            @Value("${app.frequency_penalty}") double frequencyPenalty,
            @Value("${app.presence_penalty}") double presencePenalty,
            @Value("${app.top_p}") double topP
    ) {
        this.client = webClientBuilder.build();
        this.statistikService = statistikService;
        this.API_KEY = apiKey;
        this.URL = url;
        this.MODEL = model;
        this.TEMPERATURE = temperature;
        this.MAX_TOKENS = maxTokens;
        this.FREQUENCY_PENALTY = frequencyPenalty;
        this.PRESENCE_PENALTY = presencePenalty;
        this.TOP_P = topP;
    }

    public MyResponse makeRequest(String userPrompt, String systemMessage) {
        logger.info("Making request with prompt: {} | systemMessage: {}", userPrompt, systemMessage);
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
        try {
            String json = mapper.writeValueAsString(requestDto);
            logger.debug("Sender JSON til OpenAI: {}", json);

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

            logger.info("Tokens brugt: {}", tokensUsed);
            logger.info("Estimeret pris: ${}", String.format("%.6f", tokensUsed * 0.0015 / 1000));

            return new MyResponse(responseMsg);

        } catch (WebClientResponseException e) {
            logger.error("Fejl fra OpenAI API: {}", e.getResponseBodyAsString(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fejl fra OpenAI – prøv igen senere.");
        } catch (Exception e) {
            logger.error("Intern fejl ved kald til OpenAI", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Intern fejl – prøv igen senere.");
        }
    }
}
