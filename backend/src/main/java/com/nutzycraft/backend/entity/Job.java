package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    private Double budget;
    private String duration;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private User freelancer;

    private LocalDateTime postedAt = LocalDateTime.now();

    // Status: OPEN, IN_PROGRESS, COMPLETED
    private String status = "OPEN";

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Milestone> milestones;

    // Timeline Step index
    private Integer currentStep = 1;

    // Custom Timeline Labels (comma-separated). If null, defaults to 4 steps.
    @Column(columnDefinition = "TEXT")
    private String timelineLabels;

    // Mutual Reviews
    // Rating given BY Freelancer TO Client
    private Integer ratingForClient;
    @Column(columnDefinition = "TEXT")
    private String reviewForClient;

    // Rating given BY Client TO Freelancer
    private Integer ratingForFreelancer;
    @Column(columnDefinition = "TEXT")
    private String reviewForFreelancer;

    @Column(columnDefinition = "TEXT")
    private String attachments; // Comma-separated URLs
}
