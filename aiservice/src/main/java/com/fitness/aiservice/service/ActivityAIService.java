package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;

    public void generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        log.info("Response fomr AI {}", geminiService.getRecommendation(prompt));
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                Analyze the following fitness activity data and provide me Detailed recommendations for the user in this exact JSON formate:
                {
                    "Analysis": {
                    "Overall": "OverAll analysis here",
                    "Pace": "Pace analysis here",
                    "HeartRate": "Heart rate analysis here",
                    "Calories Burned": "Calories burned analysis here"
                    },
                    
                    "Improvements": [
                    {
                    "Area":"Area name",
                    "Suggestion":"Improvement suggestion"
                    }
                    ],
                    "Safety Tips": [
                    {
                    "workout":"workout name",
                    "Description":"Detailed workout description",
                    "Precautions":"Precautions to take while performing this workout"
                    }
                    ],
                    
                    "Safety Tips": [
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
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics());
    }
}
