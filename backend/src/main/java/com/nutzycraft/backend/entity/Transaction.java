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

    private String description; // E.g., "Monthly Freelancer Payout"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User relatedUser; // The user involved in the transaction

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // CREDIT, DEBIT

    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // PROCESSED, PENDING, RECEIVED

    private LocalDateTime date;

    public enum TransactionType {
        CREDIT, DEBIT
    }

    public enum TransactionStatus {
        PROCESSED, PENDING, RECEIVED
    }
}
