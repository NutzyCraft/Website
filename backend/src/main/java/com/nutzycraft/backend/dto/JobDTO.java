package com.nutzycraft.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Double budget;
    private String duration;
    private LocalDateTime postedAt;
    private String status;
    private Integer currentStep;
    private String attachments;
    
    // User summaries instead of full User entities
    private UserSummaryDTO client;
    private UserSummaryDTO freelancer;
    
    // Ratings
    private Integer ratingForClient;
    private String reviewForClient;
    private Integer ratingForFreelancer;
    private String reviewForFreelancer;
}
