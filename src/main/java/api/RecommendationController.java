package api;

import Service.OpenAiRecommendationService;
import dtos.RecommendationRequest;
import dtos.RecommendationResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/recommendation")
public class RecommendationController {

    private final OpenAiRecommendationService service;

    public RecommendationController(OpenAiRecommendationService service) {
        this.service = service;
    }

    @PostMapping()
    public RecommendationResponse getRecommendation(@RequestBody RecommendationRequest request) {
        String systemMessage = "You are a friendly academic and career advisor for high school students. Based on the user's interests, suggest suitable education paths or careers.";
        String recommendation = service.makeRequest(request.getInput(), systemMessage).getResponse();
        return new RecommendationResponse(recommendation);
    }
}
