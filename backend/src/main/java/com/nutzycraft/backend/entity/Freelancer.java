package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "freelancers")
public class Freelancer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Double hourlyRate;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String bannerImage;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    private Double rating = 0.0;
}
