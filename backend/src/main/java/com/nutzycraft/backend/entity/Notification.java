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

    private String title;
    private String message;
    private String type; // ERROR, INFO, WARNING
    private String link; // Optional link to redirect (e.g., to dispute page)
    
    private boolean isRead;
    private LocalDateTime createdAt;
}
