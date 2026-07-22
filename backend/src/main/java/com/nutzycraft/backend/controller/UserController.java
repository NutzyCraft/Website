package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.UserProfileDTO;
import com.nutzycraft.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final com.nutzycraft.backend.repository.UserRepository userRepository;
    private final com.nutzycraft.backend.repository.ClientRepository clientRepository;
    private final com.nutzycraft.backend.repository.FreelancerRepository freelancerRepository;
    private final com.nutzycraft.backend.repository.PresenceRepository presenceRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@RequestParam String email) {
        String callerEmail = com.nutzycraft.backend.security.CurrentUser.email();
        if (!callerEmail.equalsIgnoreCase(email)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You can only view your own profile");
        }
        return ResponseEntity.ok(authService.getUserProfile(email));
    }

    private final com.nutzycraft.backend.service.FileUploadService fileUploadService;
    private final com.nutzycraft.backend.service.UserDeletionService userDeletionService;

    @Autowired
    public UserController(AuthService authService,
                          com.nutzycraft.backend.repository.UserRepository userRepository,
                          com.nutzycraft.backend.repository.ClientRepository clientRepository,
                          com.nutzycraft.backend.repository.FreelancerRepository freelancerRepository,
                          com.nutzycraft.backend.repository.PresenceRepository presenceRepository,
                          com.nutzycraft.backend.service.FileUploadService fileUploadService,
                          com.nutzycraft.backend.service.UserDeletionService userDeletionService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.freelancerRepository = freelancerRepository;
        this.presenceRepository = presenceRepository;
        this.fileUploadService = fileUploadService;
        this.userDeletionService = userDeletionService;
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable @NonNull Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            com.nutzycraft.backend.entity.User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            String callerEmail = com.nutzycraft.backend.security.CurrentUser.email();
            if (!callerEmail.equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body("You can only update your own avatar");
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
    public ResponseEntity<String> getUserAvatar(@PathVariable @NonNull Long id) {
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

    /**
     * Presence of a given user: whether they currently hold a live WebSocket
     * session and, if not, how long ago they were last seen. The online flag is
     * maintained by {@code PresenceService} from STOMP connect/disconnect events;
     * time-since is computed server-side so the client never has to reconcile
     * server vs. browser time zones.
     */
    @GetMapping("/{id}/presence")
    public ResponseEntity<java.util.Map<String, Object>> getPresence(@PathVariable @NonNull Long id) {
        java.util.Optional<com.nutzycraft.backend.entity.UserPresence> presence = presenceRepository.findById(id);
        boolean online = presence.map(com.nutzycraft.backend.entity.UserPresence::isOnline).orElse(false);
        java.time.LocalDateTime lastSeen = presence
                .map(com.nutzycraft.backend.entity.UserPresence::getLastSeen)
                .orElse(null);

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("online", online);
        if (online || lastSeen == null) {
            body.put("lastSeenSecondsAgo", null);
        } else {
            long secondsAgo = java.time.Duration.between(lastSeen, java.time.LocalDateTime.now()).getSeconds();
            body.put("lastSeenSecondsAgo", Math.max(0, secondsAgo));
        }
        return ResponseEntity.ok(body);
    }



    /**
     * Delete user account and all associated data.
     * Requires password confirmation for security.
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount() {
        try {
            String email = com.nutzycraft.backend.security.CurrentUser.email();
            // Verify the user exists
            com.nutzycraft.backend.entity.User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Perform the deletion
            userDeletionService.deleteUserAccount(email);

            return ResponseEntity.ok(java.util.Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Failed to delete account: " + e.getMessage()));
        }
    }
}
