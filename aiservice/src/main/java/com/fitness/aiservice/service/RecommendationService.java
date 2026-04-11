package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationReposiory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationReposiory recommendationReposiory;

    public List<Recommendation> getUserRecommendations(@PathVariable String userId) {
        return recommendationReposiory.findByUserId(userId);
    }

    public Recommendation getActivityRecommendation(String activityId) {
        return recommendationReposiory.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("Recommendation not found for activity: " + activityId));

    }
}
