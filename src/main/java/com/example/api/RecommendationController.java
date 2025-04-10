package com.example.api;

import com.example.Service.OpenAiRecommendationService;
import com.example.dtos.RecommendationRequest;
import com.example.dtos.RecommendationResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for handling recommendation requests via the OpenAI API with IP-rate limiting.
 */
@RestController
@RequestMapping("/api/recommendationlimited")
@CrossOrigin(origins = "*") // Temporarily allow all domains for testing
public class RecommendationLimitedController {

    @Value("${app.bucket_capacity}")
    private int BUCKET_CAPACITY;

    @Value("${app.refill_amount}")
    private int REFILL_AMOUNT;

    @Value("${app.refill_time}")
    private int REFILL_TIME;

    private final OpenAiRecommendationService service;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Constructor for RecommendationLimitedController.
     * @param service The service that handles the OpenAI request.
     */
    public RecommendationLimitedController(OpenAiRecommendationService service) {
        this.service = service;
    }

    /**
     * Creates a new bucket for rate limiting the IP address.
     * @return A new bucket for handling IP rate limits.
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(BUCKET_CAPACITY, Refill.greedy(REFILL_AMOUNT, Duration.ofMinutes(REFILL_TIME)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Retrieves an existing bucket for the given IP address, or creates a new one.
     * @param key The IP address as the key.
     * @return The bucket associated with the given IP.
     */
    private Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Handles the recommendation request from the browser, with IP-rate limiting.
     * @param request The request containing the user's input.
     * @param about The topic the user wants a recommendation for.
     * @param httpServletRequest The current HTTP request used to get the IP address.
     * @return The recommendation response from OpenAI.
     */
    @PostMapping
    public RecommendationResponse getRecommendation(@RequestBody RecommendationRequest request, @RequestParam String about, HttpServletRequest httpServletRequest) {
        // Get the client's IP address
        String ip = httpServletRequest.getRemoteAddr();
        // Get or create the rate limiting bucket for the IP address
        Bucket bucket = getBucket(ip);

        // Check if the request exceeds the rate limit
        if (!bucket.tryConsume(1)) {
            // If the rate limit is exceeded, return "Too many requests" status
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, try again later");
        }

        // Proceed to fetch the recommendation using OpenAI service
        try {
            String systemMessage = "You are a friendly academic and career advisor for high school students. " +
                    "Based on the user's interests, suggest suitable education paths or careers.";
            String recommendation = service.makeRequest(request.getInput(), systemMessage).getResponse();
            return new RecommendationResponse(recommendation);
        } catch (Exception e) {
            System.err.println("Error in controller: " + e.getMessage());
            throw new RuntimeException("An error occurred while processing the recommendation.");
        }
    }
}
