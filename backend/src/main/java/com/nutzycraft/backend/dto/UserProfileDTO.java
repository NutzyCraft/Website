package com.nutzycraft.backend.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;

    // Client specific
    private String companyName;
    private String industry;

    // Freelancer specific
    // Add fields here if/when needed
    private String profileImage;
}
