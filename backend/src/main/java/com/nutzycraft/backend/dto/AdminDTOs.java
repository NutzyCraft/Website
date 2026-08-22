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
        /** Subcontractor payouts queued for manual bank transfer */
        private List<PendingPayoutDTO> pendingPayoutQueue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminTransactionItemDTO {
        private Long id;
        private String description;
        private String userName;
        private Double amount;
        private String status;
        private String date;
        /** INBOUND_PAYHERE or OUTBOUND_MANUAL */
        private String type;
    }

    /**
     * Represents a single pending subcontractor payout in the admin queue.
     * Admins use this to see bank details and mark payouts as settled
     * after completing the manual bank transfer.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PendingPayoutDTO {
        private Long transactionId;
        private String freelancerName;
        private String freelancerEmail;
        // Bank details for manual transfer
        private String bankName;
        private String bankBranchCode;
        private String bankAccountName;
        private String bankAccountNumber;
        private Double amount;
        private String currency;
        private String milestoneDescription;
        private String queuedAt;
        private String status; // PENDING or PROCESSING (freelancer requested payout)
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
