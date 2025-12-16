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
}
