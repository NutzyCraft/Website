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
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // In a real app, hash the password!
        return userRepository.save(user);
    }

    @PostMapping("/register-freelancer")
    public User registerFreelancer(@RequestBody com.nutzycraft.backend.dto.AuthDTOs.FreelancerRegisterRequest request) {
        return authService.registerFreelancer(request);
    }

    @PostMapping("/verify")
    public org.springframework.http.ResponseEntity<?> verifyUser(
            @RequestBody com.nutzycraft.backend.dto.AuthDTOs.VerifyRequest request) {
        boolean verified = authService.verifyUser(request.getEmail(), request.getCode());
        if (verified) {
            return org.springframework.http.ResponseEntity.ok("User verified successfully");
        } else {
            return org.springframework.http.ResponseEntity.badRequest().body("Invalid verification code");
        }
    }

    @PostMapping("/register-client")
    public User registerClient(@RequestBody com.nutzycraft.backend.dto.AuthDTOs.ClientRegisterRequest request) {
        return authService.registerClient(request);
    }

    @PostMapping("/login")
    public User login(@RequestBody com.nutzycraft.backend.dto.AuthDTOs.LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/forgot-password")
    public org.springframework.http.ResponseEntity<?> forgotPassword(
            @RequestBody com.nutzycraft.backend.dto.AuthDTOs.ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return org.springframework.http.ResponseEntity.ok("If email exists, reset link sent.");
    }

    @PostMapping("/reset-password")
    public org.springframework.http.ResponseEntity<?> resetPassword(
            @RequestBody com.nutzycraft.backend.dto.AuthDTOs.ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return org.springframework.http.ResponseEntity.ok("Password reset successfully.");
    }

    @PostMapping("/google")
    public User googleLogin(@RequestBody com.nutzycraft.backend.dto.AuthDTOs.GoogleLoginRequest request) {
        return authService.loginOrRegisterWithGoogle(request.getToken(), request.getRole());
    }

    @PostMapping("/resend-verification")
    public org.springframework.http.ResponseEntity<?> resendVerification(
            @RequestBody com.nutzycraft.backend.dto.AuthDTOs.ForgotPasswordRequest request) {
        authService.resendVerificationCode(request.getEmail());
        return org.springframework.http.ResponseEntity.ok("Verification code sent.");
    }
}
