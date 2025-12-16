package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "proposals")
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @Column(nullable = false)
    private Double bidAmount;

    @Column(nullable = false)
    private String deliveryTime; // e.g. "7 Days"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String coverLetter;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, ACCEPTED, DECLINED

    private LocalDateTime createdAt = LocalDateTime.now();
}
