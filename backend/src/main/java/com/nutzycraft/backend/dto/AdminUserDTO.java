package com.nutzycraft.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime joinedAt;
    private boolean active;
}
