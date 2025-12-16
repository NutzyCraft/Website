package com.nutzycraft.backend.service;

import com.nutzycraft.backend.dto.AuthDTOs.*;
import com.nutzycraft.backend.entity.Client;
import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.ClientRepository;
import com.nutzycraft.backend.repository.FreelancerRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerFreelancer(FreelancerRegisterRequest request) {
        validatePassword(request.getPassword());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = createUser(request.getEmail(), request.getFullName(), request.getPassword(), User.Role.FREELANCER);

        Freelancer freelancer = new Freelancer();
        freelancer.setUser(user);
        freelancerRepository.save(freelancer);

        sendVerification(user);
        return user;
    }

    @Transactional
    public User registerClient(ClientRegisterRequest request) {
        validatePassword(request.getPassword());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = createUser(request.getEmail(), request.getFullName(), request.getPassword(), User.Role.CLIENT);

        Client client = new Client();
        client.setUser(user);
        client.setCompanyName(request.getCompanyName());
        client.setIndustry(request.getIndustry());
        clientRepository.save(client);

        sendVerification(user);
        return user;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("Password must contain at least one number");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new RuntimeException("Password must contain at least one special character");
        }
    }

    private User createUser(String email, String fullName, String password, User.Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(password); // Should be hashed
        user.setRole(role);
        return userRepository.save(user);
    }

    private void sendVerification(User user) {
        String code = String.valueOf((int) (Math.random() * 9000) + 1000);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    public boolean verifyUser(String email, String code) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (code.equals(user.getVerificationCode()) &&
                            (user.getVerificationCodeExpiresAt() == null
                                    || user.getVerificationCodeExpiresAt().isAfter(LocalDateTime.now()))) {
                        user.setVerified(true);
                        user.setVerificationCode(null);
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (password.equals(user.getPassword())) { // Simple check, should use Encoder matches
                if (!user.isVerified()) {
                    throw new RuntimeException("Account not verified. Please check your email.");
                }
                return user;
            }
        }
        throw new RuntimeException("Invalid credentials");
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    public void resetPassword(String token, String newPassword) {
        validatePassword(newPassword);
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken())) // Ideally findByResetToken
                .findFirst();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token expired");
            }
            user.setPassword(newPassword); // Should hash
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid reset token");
        }
    }

    @Autowired
    private GoogleAuthService googleAuthService;

    public User loginOrRegisterWithGoogle(String token, String roleStr) {
        java.util.Map<String, Object> payload = googleAuthService.verifyToken(token);
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // New User Registration
        if (roleStr == null || roleStr.isEmpty()) {
            throw new RuntimeException("Role is required for new user registration");
        }

        User.Role role;
        try {
            role = User.Role.valueOf(roleStr.toUpperCase());
            if (role == User.Role.ADMIN) {
                throw new RuntimeException("Cannot register as ADMIN");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified");
        }

        // Auto-register
        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setPassword(UUID.randomUUID().toString()); // Random pass
        user.setRole(role);
        user.setVerified(true); // Google verified
        userRepository.save(user);

        if (role == User.Role.FREELANCER) {
            Freelancer freelancer = new Freelancer();
            freelancer.setUser(user);
            freelancerRepository.save(freelancer);
        } else if (role == User.Role.CLIENT) {
            Client client = new Client();
            client.setUser(user);
            clientRepository.save(client);
        }

        return user;
    }

    public void resendVerificationCode(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isVerified()) {
                throw new RuntimeException("User is already verified");
            }
            sendVerification(user);
        });
    }

    public com.nutzycraft.backend.dto.UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.nutzycraft.backend.dto.UserProfileDTO dto = new com.nutzycraft.backend.dto.UserProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        if (user.getRole() == User.Role.CLIENT) {
            clientRepository.findByUser(user).ifPresent(client -> {
                dto.setCompanyName(client.getCompanyName());
                dto.setIndustry(client.getIndustry());
            });
        }
        // Add freelancer logic if needed

        return dto;
    }
}
