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

    private String fullName;

    @Column(unique = true)
    private String providerId; // Neon Auth subject ID (sub claim)

    @Enumerated(EnumType.STRING)
    private Role role;

    private String profilePictureUrl;

    public enum Role {
        CLIENT, FREELANCER, ADMIN
    }
}
