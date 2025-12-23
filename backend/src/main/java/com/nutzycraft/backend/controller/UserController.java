package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.UserProfileDTO;
import com.nutzycraft.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private com.nutzycraft.backend.repository.UserRepository userRepository;

    @Autowired
    private com.nutzycraft.backend.repository.ClientRepository clientRepository;

    @Autowired
    private com.nutzycraft.backend.repository.FreelancerRepository freelancerRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserProfile(email));
    }

    @Autowired
    private com.nutzycraft.backend.service.FileUploadService fileUploadService;

    @PostMapping("/{id}/avatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            com.nutzycraft.backend.entity.User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            String imageUrl = fileUploadService.uploadFile(file);
            user.setProfilePictureUrl(imageUrl);
            userRepository.save(user);

            // Also update Client/Freelancer profile image if applicable for backward compatibility or syncing
            if (user.getRole() == com.nutzycraft.backend.entity.User.Role.CLIENT) {
                com.nutzycraft.backend.entity.Client client = clientRepository.findByUser_Id(user.getId()).orElse(null);
                if (client != null) {
                    client.setProfileImage(imageUrl);
                    clientRepository.save(client);
                }
            } else if (user.getRole() == com.nutzycraft.backend.entity.User.Role.FREELANCER) {
                com.nutzycraft.backend.entity.Freelancer freelancer = freelancerRepository.findByUser_Id(user.getId()).orElse(null);
                if (freelancer != null) {
                    freelancer.setProfileImage(imageUrl);
                    freelancerRepository.save(freelancer);
                }
            }

            return ResponseEntity.ok(imageUrl);
        } catch (java.io.IOException e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<String> getUserAvatar(@PathVariable Long id) {
        com.nutzycraft.backend.entity.User user = userRepository.findById(id).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            return ResponseEntity.ok(user.getProfilePictureUrl());
        }

        String avatar = null;
        if (user.getRole() == com.nutzycraft.backend.entity.User.Role.CLIENT) {
            com.nutzycraft.backend.entity.Client client = clientRepository.findByUser_Id(user.getId()).orElse(null);
            if (client != null)
                avatar = client.getProfileImage();
        } else if (user.getRole() == com.nutzycraft.backend.entity.User.Role.FREELANCER) {
            com.nutzycraft.backend.entity.Freelancer freelancer = freelancerRepository.findByUser_Id(user.getId())
                    .orElse(null);
            if (freelancer != null)
                avatar = freelancer.getProfileImage();
        }

        if (avatar == null || avatar.isEmpty()) {
            return ResponseEntity.ok("https://ui-avatars.com/api/?name="
                    + java.net.URLEncoder.encode(user.getFullName(), java.nio.charset.StandardCharsets.UTF_8)
                    + "&background=random");
        }
        return ResponseEntity.ok(avatar);
    }
}
