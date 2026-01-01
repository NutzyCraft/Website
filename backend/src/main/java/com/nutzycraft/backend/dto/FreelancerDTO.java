package com.nutzycraft.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class FreelancerDTO {
    private Long id;
    private String title;
    private String bio;
    private Double hourlyRate;
    private List<String> skills;
    private Double rating;
    // Exclude heavy image fields
    private UserSummaryDTO user;
}
