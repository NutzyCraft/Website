package com.nutzycraft.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@Profile("dev")
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

            // Drop legacy password column since Neon Auth handles passwords now
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN IF EXISTS password");

            // Drop legacy is_verified column since Neon Auth handles verification now
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN IF EXISTS is_verified");

            System.out.println("SchemaFixer completed successfully.");
        } catch (Exception e) {
            System.err.println("SchemaFixer warning (can be ignored if columns exist): " + e.getMessage());
        }
    }
}
