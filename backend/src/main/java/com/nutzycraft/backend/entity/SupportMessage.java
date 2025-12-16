package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "support_messages")
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private SupportStatus status;

    private LocalDateTime createdAt;

    public enum SupportStatus {
        OPEN, RESOLVED
    }
}
