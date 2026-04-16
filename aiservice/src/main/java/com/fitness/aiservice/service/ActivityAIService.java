package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getRecommendation(prompt);
        log.info("Response for AI {}", aiResponse);
        return processAIResponse(activity, aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("'''json\\n", "")
                    .replaceAll("\\n'''", "")
                    .trim();


            JsonNode analsisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analsisJson.path("Analysis");
            StringBuilder fullAnalysis = new  StringBuilder();
            addAnalysisSection(fullAnalysis,analysisNode,"Overall", "Ovverall: ");
            addAnalysisSection(fullAnalysis,analysisNode,"Pace", "Pace: ");
            addAnalysisSection(fullAnalysis,analysisNode,"HeartRate", "Heart Rate: ");
            addAnalysisSection(fullAnalysis,analysisNode,"CaloriesBurned", "Calories Burned: ");

            List<String> improvements = extractImprovements(analsisJson.path("Improvements"));
            List<String> safety = extractSafetyGuidelines(analsisJson.path("Safety"));
            List<String> suggestion = extractSuggestions(analsisJson.path("Suggestions"));

            return Recommendation.builder()
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .type(activity.getType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvement(improvements)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .suggestions(suggestion)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .userId(activity.getUserId())
                .activityId(activity.getId())
                .type(activity.getType().toString())
                .recommendation("Unable to generate specific recommendations at the moment. Please try again later.")
                .improvement(Collections.singletonList("No specific improvements identified. Keep up the good work!"))
                .safety(Arrays.asList("No specific safety concerns identified. Continue to follow general fitness safety guidelines!"))
                .createdAt(LocalDateTime.now())
                .suggestions(Collections.singletonList("No specific workout suggestions at the moment. Keep up the good work!"))
                .build();
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("Description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ?
                Collections.singletonList("No specific workout suggestions at the moment. Keep up the good work!"):
                suggestions;

    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if(safetyNode.isArray()){
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ?
                Collections.singletonList("No specific safety concerns identified. Continue to follow general fitness safety guidelines!"):
                safety;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if(improvementsNode.isArray()){
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("Area").asText();
                String suggestion = improvement.path("Suggestion").asText();
                improvements.add(String.format("%s: %s", area, suggestion));
            });
        }
        return improvements.isEmpty() ?
                Collections.singletonList("No specific improvements identified. Keep up the good work!"):
                improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()){

            fullAnalysis.append(prefix).append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                Analyze the following fitness activity data and provide me Detailed recommendations for the user in this exact JSON formate:
                {
                    "Analysis": {
                    "Overall": "OverAll analysis here",
                    "Pace": "Pace analysis here",
                    "HeartRate": "Heart rate analysis here",
                    "CaloriesBurned": "Calories burned analysis here"
                    },
                    
                    "Improvements": [
                    {
                    "Area":"Area name",
                    "Suggestion":"Improvement suggestion"
                    }
                    ],
                    "Suggestions": [
                    {
                    "workout":"workout name",
                    "Description":"Detailed workout description"
                    }
                    ],
                    
                    "Safety": [
                    {
                    "Safety point 1",
                    "Safety point 2"
                    }
                    ]
                                      
                }
                
                Analyze the following activity data:
                activityType: %s
                duration: %d minutes
                caloriesBurned: %d
                additionalMetrics: %s
                
                provide detailed analysis and recommendations based on the above activity data, focussing on performance, improvement, next workout suggestion, and safety guidlines.
                ensure the response follows the specified JSON format shown above.
                Give response in pure JSON without markdown or backticks
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics());
    }
}
