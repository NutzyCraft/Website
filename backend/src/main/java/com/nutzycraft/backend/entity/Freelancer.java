package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "freelancers")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Freelancer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Double hourlyRate;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String bannerImage;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    private String languages;

    private String workStyle;

    private Double rating = 0.0;

    // ─── Subcontractor Bank Account Details ───────────────────────────────────
    // These fields are stored securely and only visible to admins when
    // processing manual bank transfer payouts. Never exposed to clients.

    /** The freelancer's local bank name (e.g., "Commercial Bank of Ceylon") */
    private String bankName;

    /** Branch code or SWIFT/BIC for the bank branch */
    private String bankBranchCode;

    /** Full legal account holder name as registered with the bank */
    private String bankAccountName;

    /** Bank account number for direct bank transfer */
    private String bankAccountNumber;
}
