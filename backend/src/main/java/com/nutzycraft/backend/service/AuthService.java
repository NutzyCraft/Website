package com.nutzycraft.backend.service;

import com.nutzycraft.backend.dto.FreelancerRegisterRequest;
import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.FreelancerRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerFreelancer(FreelancerRegisterRequest request) {
        // 1. Create User
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // In real app: encode password
        user.setRole(User.Role.FREELANCER);
        
        // Generate OTP
        String code = String.valueOf((int)(Math.random() * 9000) + 1000);
        user.setVerificationCode(code);

        user = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), code);

        // 2. Create Freelancer Profile linked to User
        Freelancer freelancer = new Freelancer();
        freelancer.setUser(user);
        // Initialize other fields as needed, e.g., empty profile
        freelancerRepository.save(freelancer);

        return user;
    }

    public boolean verifyUser(String email, String code) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (code.equals(user.getVerificationCode())) {
                        user.setVerified(true);
                        user.setVerificationCode(null); // Clear code after usage
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
