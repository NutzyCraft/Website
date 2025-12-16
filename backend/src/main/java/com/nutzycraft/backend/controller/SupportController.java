package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.AdminDTOs;
import com.nutzycraft.backend.entity.SupportMessage;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.SupportMessageRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createSupportMessage(@RequestBody AdminDTOs.CreateSupportDTO request, @RequestParam String userEmail) {
        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SupportMessage message = new SupportMessage();
        message.setSender(sender);
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        message.setStatus(SupportMessage.SupportStatus.OPEN);
        message.setCreatedAt(LocalDateTime.now());

        supportMessageRepository.save(message);

        return ResponseEntity.ok("Support message sent successfully");
    }
}
