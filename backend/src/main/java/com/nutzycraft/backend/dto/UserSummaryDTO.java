package com.nutzycraft.backend.dto;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
}
