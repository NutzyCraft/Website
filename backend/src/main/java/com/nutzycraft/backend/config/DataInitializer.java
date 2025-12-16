package com.nutzycraft.backend.config;

import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      com.nutzycraft.backend.repository.TransactionRepository transactionRepository,
                                      com.nutzycraft.backend.repository.DisputeRepository disputeRepository,
                                      com.nutzycraft.backend.repository.SupportMessageRepository supportMessageRepository) {
        return args -> {
            // 1. Admin User
            String adminEmail = "nutzycraft@gmail.com";
            User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
            admin.setEmail(adminEmail);
            admin.setFullName("Super Admin");
            admin.setPassword("NutzyCraft@123");
            admin.setRole(User.Role.ADMIN);
            admin.setVerified(true);
            userRepository.save(admin);

            // 2. Dummy Client
            String clientEmail = "client@test.com";
            User client = userRepository.findByEmail(clientEmail).orElseGet(User::new);
            if (client.getId() == null) {
                client.setEmail(clientEmail);
                client.setFullName("Test Client");
                client.setPassword("password");
                client.setRole(User.Role.CLIENT);
                client.setVerified(true);
                userRepository.save(client);
            }

            // 3. Dummy Freelancer
            String freelancerEmail = "freelancer@test.com";
            User freelancer = userRepository.findByEmail(freelancerEmail).orElseGet(User::new);
            if (freelancer.getId() == null) {
                freelancer.setEmail(freelancerEmail);
                freelancer.setFullName("Test Freelancer");
                freelancer.setPassword("password");
                freelancer.setRole(User.Role.FREELANCER);
                freelancer.setVerified(true);
                userRepository.save(freelancer);
            }

            // 4. Seed Transactions
            if (transactionRepository.count() == 0) {
                com.nutzycraft.backend.entity.Transaction t1 = new com.nutzycraft.backend.entity.Transaction();
                t1.setDescription("Job Payment - Logo Design");
                t1.setRelatedUser(client);
                t1.setAmount(150.0);
                t1.setType(com.nutzycraft.backend.entity.Transaction.TransactionType.CREDIT);
                t1.setStatus(com.nutzycraft.backend.entity.Transaction.TransactionStatus.PROCESSED);
                t1.setDate(java.time.LocalDateTime.now().minusDays(2));
                transactionRepository.save(t1);

                com.nutzycraft.backend.entity.Transaction t2 = new com.nutzycraft.backend.entity.Transaction();
                t2.setDescription("Payout to Freelancer");
                t2.setRelatedUser(freelancer);
                t2.setAmount(135.0);
                t2.setType(com.nutzycraft.backend.entity.Transaction.TransactionType.DEBIT);
                t2.setStatus(com.nutzycraft.backend.entity.Transaction.TransactionStatus.PENDING);
                t2.setDate(java.time.LocalDateTime.now().minusHours(5));
                transactionRepository.save(t2);
            }

            // 5. Seed Disputes
            if (disputeRepository.count() == 0) {
                com.nutzycraft.backend.entity.Dispute d1 = new com.nutzycraft.backend.entity.Dispute();
                d1.setClient(client);
                d1.setFreelancer(freelancer);
                d1.setIssue("Freelancer stopped responding after milestone 1.");
                d1.setStatus(com.nutzycraft.backend.entity.Dispute.DisputeStatus.OPEN);
                d1.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
                disputeRepository.save(d1);
            }

            // 6. Seed Support Messages
            if (supportMessageRepository.count() == 0) {
                com.nutzycraft.backend.entity.SupportMessage s1 = new com.nutzycraft.backend.entity.SupportMessage();
                s1.setSender(client);
                s1.setSubject("How do I change my billing address?");
                s1.setMessage("I moved to a new office and need to update invoice details.");
                s1.setStatus(com.nutzycraft.backend.entity.SupportMessage.SupportStatus.OPEN);
                s1.setCreatedAt(java.time.LocalDateTime.now().minusHours(24));
                supportMessageRepository.save(s1);
            }

            System.out.println("Data initialization complete. Admin: " + adminEmail);
        };
    }
}
