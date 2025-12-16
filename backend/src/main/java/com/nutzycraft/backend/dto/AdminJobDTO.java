package com.nutzycraft.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminJobDTO {
    private Long id;
    private String title;
    private String clientName;
    private String freelancerName;
    private Double value;
    private String status;
}
