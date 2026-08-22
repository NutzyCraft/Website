package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description; // E.g., "Milestone Payment: Homepage Design"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User relatedUser; // The user involved in the transaction

    private Double amount;

    /**
     * INBOUND_PAYHERE  - A client payment received via PayHere (money IN to NutzyCraft)
     * OUTBOUND_MANUAL  - A manual bank transfer from NutzyCraft to a subcontractor (money OUT)
     */
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    /**
     * PENDING   - Payout queued, awaiting admin bank transfer
     * PROCESSING - Freelancer requested payout, admin action required
     * SETTLED   - Admin has confirmed the bank transfer is complete
     * RECEIVED  - Inbound payment confirmed received from PayHere
     */
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime date;

    /** Links an OUTBOUND_MANUAL payout to its source milestone */
    private Long milestoneId;

    /** Links an OUTBOUND_MANUAL payout to the specific freelancer subcontractor */
    private Long freelancerId;

    public enum TransactionType {
        INBOUND_PAYHERE,
        OUTBOUND_MANUAL
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        SETTLED,
        RECEIVED
    }
}
