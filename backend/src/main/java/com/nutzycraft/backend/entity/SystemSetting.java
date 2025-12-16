package com.nutzycraft.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "system_settings")
public class SystemSetting {
    @Id
    @Column(unique = true)
    private String key; // e.g., "site_name", "platform_fee"

    private String value;
}
