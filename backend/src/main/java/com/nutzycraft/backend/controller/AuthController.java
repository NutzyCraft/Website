package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.AuthDTOs.SyncRequest;
import com.nutzycraft.backend.dto.AuthDTOs.SyncResponse;
import com.nutzycraft.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Synchronize a Clerk identity with the local database.
     *
     * This is the ONLY auth endpoint. It requires a valid Clerk-issued JWT,
     * verified by Spring Security's OAuth2 Resource Server filter before this
     * method is invoked. The session token must include a custom "email" claim
     * (configured in the Clerk Dashboard under Sessions -> Customize session token).
     *
     * Flow:
     * 1. Frontend signs in/up via Clerk (email/password or Google OAuth)
     * 2. Frontend calls POST /api/auth/sync with the JWT and desired role
     * 3. Backend extracts sub/email/name from the JWT, syncs with local DB
     * 4. Returns the user's local profile (id, email, fullName, role, isNew)
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) SyncRequest request) {
        try {
            String providerId = jwt.getSubject(); // Clerk user ID
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            String picture = jwt.getClaimAsString("picture");

            if (email == null || email.isBlank()) {
                throw new RuntimeException("Session does not contain an email.");
            }

            String role = (request != null) ? request.getRole() : null;

            SyncResponse response = authService.syncUser(providerId, email, name, picture, role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

}
