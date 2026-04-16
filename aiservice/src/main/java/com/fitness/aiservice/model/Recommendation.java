package com.fitness.aiservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "recommendations")
@Builder
public class Recommendation {
    @Id
    private String id;
    private String userId;
    private String type;
    private String activityId;
    private String recommendation;
    private List<String> improvement;
    private List<String> safety;
    private List<String> suggestions;

    @CreatedDate
    private LocalDateTime createdAt;
}
