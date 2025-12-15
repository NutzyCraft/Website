package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private com.nutzycraft.backend.service.AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // In a real app, hash the password!
        return userRepository.save(user);
    }

    @PostMapping("/register-freelancer")
    public User registerFreelancer(@RequestBody com.nutzycraft.backend.dto.FreelancerRegisterRequest request) {
        return authService.registerFreelancer(request);
    }

    @PostMapping("/verify")
    public org.springframework.http.ResponseEntity<?> verifyUser(@RequestBody com.nutzycraft.backend.dto.VerifyRequest request) {
        boolean verified = authService.verifyUser(request.getEmail(), request.getCode());
        if (verified) {
            return org.springframework.http.ResponseEntity.ok("User verified successfully");
        } else {
            return org.springframework.http.ResponseEntity.badRequest().body("Invalid verification code");
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        // Mock Login Logic
        return "User logged in successfully (JWT Token Placeholder)";
    }
}
