package com.nutzycraft.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class AdminDTOs {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminFinanceDTO {
        private double totalRevenue;
        private double pendingPayouts;
        private double commissionEarnings;
        private List<AdminTransactionItemDTO> recentTransactions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminTransactionItemDTO {
        private Long id;
        private String description;
        private String userName;
        private Double amount;
        private String status; // Processed, Pending, etc.
        private String date;
        private String type; // CREDIT, DEBIT - useful for frontend coloring
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminDisputeDTO {
        private Long id;
        private String clientName;
        private String freelancerName;
        private String issue;
        private String status;
        private String date;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminSupportDTO {
        private Long id;
        private String senderName;
        private String role;
        private String subject;
        private String status;
        private String date;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String message;
        private String type;
        private String date;
        private String link;
        private boolean isRead;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemSettingsDTO {
        private String siteName;
        private String supportEmail;
        private String platformFee;
        private String maintenanceMode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateDisputeDTO {
        private String issue;
        private Long freelancerId; // Client creates dispute against freelancer
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateSupportDTO {
        private String subject;
        private String message;
    }
}
