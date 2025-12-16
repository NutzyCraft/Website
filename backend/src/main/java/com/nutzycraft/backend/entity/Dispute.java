package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "disputes")
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private User freelancer;

    private String issue;

    @Enumerated(EnumType.STRING)
    private DisputeStatus status;

    private LocalDateTime createdAt;

    public enum DisputeStatus {
        OPEN, RESOLVED
    }
}
