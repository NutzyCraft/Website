package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String companyName;
    private String industry;
    private String website;

    private String contactPerson;
    private String billingAddress;

    @Column(columnDefinition = "TEXT")
    private String description;
}
