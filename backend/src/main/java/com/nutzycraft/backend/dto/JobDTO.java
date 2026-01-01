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
    private String duration; // Changed to String to match Entity
    private String status;
    private LocalDateTime postedAt;
    private UserSummaryDTO client;
    private UserSummaryDTO freelancer;
    private int proposalCount;

    // New fields
    private Integer currentStep;
    private String attachments;
    private Integer ratingForClient;
    private Integer ratingForFreelancer;
    private String reviewForClient;
    private String reviewForFreelancer;
}
