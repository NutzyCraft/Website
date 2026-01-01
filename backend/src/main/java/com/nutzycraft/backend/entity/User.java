package com.nutzycraft.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profilePictureUrl;

    @JsonIgnore
    private String verificationCode;

    private boolean isVerified = false;

    @JsonIgnore
    private String resetToken;
    
    @JsonIgnore
    private java.time.LocalDateTime verificationCodeExpiresAt;
    
    @JsonIgnore
    private java.time.LocalDateTime resetTokenExpiresAt;

    public enum Role {
        CLIENT, FREELANCER, ADMIN
    }
}
