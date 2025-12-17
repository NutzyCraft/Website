package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "milestones")
public class Milestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double amount;

    // Status: PENDING, IN_PROGRESS, COMPLETED
    private String status = "PENDING";

    private LocalDate dueDate;
}
