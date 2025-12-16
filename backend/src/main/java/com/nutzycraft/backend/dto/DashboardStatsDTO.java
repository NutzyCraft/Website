package com.nutzycraft.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.nutzycraft.backend.dto.AdminDTOs;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private long totalUsers;
    private long activeJobs;
    private double revenue;
    private long openDisputes;
    private java.util.List<AdminUserDTO> recentUsers;
    private java.util.List<AdminDTOs.NotificationDTO> recentActivities;
}
