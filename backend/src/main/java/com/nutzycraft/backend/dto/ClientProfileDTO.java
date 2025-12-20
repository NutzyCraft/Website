package com.nutzycraft.backend.dto;

import com.nutzycraft.backend.entity.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientProfileDTO {
    private Client client;
    private long totalHires;
    private double totalSpent;
    private int memberSince;
    private Double rating;
}
