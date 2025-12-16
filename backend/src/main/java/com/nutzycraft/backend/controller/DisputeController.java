package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.AdminDTOs;
import com.nutzycraft.backend.entity.Dispute;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.DisputeRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins = "*")
public class DisputeController {

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createDispute(@RequestBody AdminDTOs.CreateDisputeDTO request, @RequestParam String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        if (request.getFreelancerId() == null) {
            return ResponseEntity.badRequest().body("Freelancer ID is required");
        }
        
        User freelancer = userRepository.findById(request.getFreelancerId())
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));

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
