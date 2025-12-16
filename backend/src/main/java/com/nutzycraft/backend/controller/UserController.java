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

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserProfile(email));
    }
}
