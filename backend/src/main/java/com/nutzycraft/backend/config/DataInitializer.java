package com.nutzycraft.backend.config;

import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
            com.nutzycraft.backend.repository.TransactionRepository transactionRepository,
            com.nutzycraft.backend.repository.DisputeRepository disputeRepository,
            com.nutzycraft.backend.repository.SupportMessageRepository supportMessageRepository) {
        return args -> {
            System.out.println("Data initialization complete. No dummy users seeded.");
        };
    }
}
