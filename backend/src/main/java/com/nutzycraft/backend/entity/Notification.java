package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    private String title;
    private String message;
    private String type; // ERROR, INFO, WARNING
    private String link; // Optional link to redirect (e.g., to dispute page)

    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
