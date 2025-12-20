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

    @GetMapping("/{id}/avatar")
    public ResponseEntity<String> getUserAvatar(@PathVariable Long id) {
        com.nutzycraft.backend.entity.User user = userRepository.findById(id).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();

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
