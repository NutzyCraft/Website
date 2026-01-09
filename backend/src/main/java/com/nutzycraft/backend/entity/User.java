package com.nutzycraft.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
@Where(clause = "deleted = false")
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

    private boolean deleted = false;

    @JsonProperty("deletedAt")
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        CLIENT, FREELANCER, ADMIN
    }

    // Helper method to get display name for deleted users
    public String getDisplayName() {
        if (deletedAt != null) {
            return "User Deleted";
        }
        return fullName != null ? fullName : "Unknown User";
    }
}
