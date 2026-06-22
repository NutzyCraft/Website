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

    private String fullName;

    @Column(unique = true)
    private String providerId; // Neon Auth subject ID (sub claim)

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profilePictureUrl;

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
