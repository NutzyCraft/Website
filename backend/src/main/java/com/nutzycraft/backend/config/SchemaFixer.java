package com.nutzycraft.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class SchemaFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Running SchemaFixer to ensure columns exist...");

            // Add profile_image if missing
            jdbcTemplate.execute("ALTER TABLE clients ADD COLUMN IF NOT EXISTS profile_image TEXT");

            // Add banner_image if missing
            jdbcTemplate.execute("ALTER TABLE clients ADD COLUMN IF NOT EXISTS banner_image TEXT");

            System.out.println("SchemaFixer completed successfully.");
        } catch (Exception e) {
            System.err.println("SchemaFixer warning (can be ignored if columns exist): " + e.getMessage());
        }
    }
}
