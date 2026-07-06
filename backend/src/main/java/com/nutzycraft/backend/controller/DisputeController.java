package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.AdminDTOs;
import com.nutzycraft.backend.entity.Dispute;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.DisputeRepository;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @PostMapping
    public ResponseEntity<String> createDispute(@RequestBody AdminDTOs.CreateDisputeDTO request) {
        User client = userRepository.findByEmail(com.nutzycraft.backend.security.CurrentUser.email())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        if (client.getRole() != User.Role.CLIENT) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("Only clients can file disputes");
        }

        Long freelancerId = request.getFreelancerId();
        if (freelancerId == null) {
            return ResponseEntity.badRequest().body("Freelancer ID is required");
        }

        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));

        if (!jobRepository.existsByClient_EmailAndFreelancer_Id(client.getEmail(), freelancerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body("You can only file a dispute against a freelancer you have worked with");
        }

        Dispute dispute = new Dispute();
        dispute.setClient(client);
        dispute.setFreelancer(freelancer);
        dispute.setIssue(request.getIssue());
        dispute.setStatus(Dispute.DisputeStatus.OPEN);
        dispute.setCreatedAt(LocalDateTime.now());

        disputeRepository.save(dispute);

        return ResponseEntity.ok("Dispute created successfully");
    }
}
