package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profilePictureUrl;

    private String verificationCode;

    private boolean isVerified = false;

    private String resetToken;
    private java.time.LocalDateTime verificationCodeExpiresAt;
    private java.time.LocalDateTime resetTokenExpiresAt;

    public enum Role {
        CLIENT, FREELANCER, ADMIN
    }
}
