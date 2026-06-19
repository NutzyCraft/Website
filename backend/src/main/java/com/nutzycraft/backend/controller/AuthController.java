package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.AuthDTOs.SyncRequest;
import com.nutzycraft.backend.dto.AuthDTOs.SyncResponse;
import com.nutzycraft.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Synchronize a Neon Auth identity with the local database.
     *
     * This is the ONLY auth endpoint. It requires a valid Neon Auth JWT.
     * The JWT is validated by Spring Security's OAuth2 Resource Server filter
     * before this method is invoked.
     *
     * Flow:
     * 1. Frontend signs in/up via Neon Auth (email/password or Google OAuth)
     * 2. Frontend calls POST /api/auth/sync with the JWT and desired role
     * 3. Backend extracts sub/email/name from the JWT, syncs with local DB
     * 4. Returns the user's local profile (id, email, fullName, role, isNew)
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(
            org.springframework.security.core.Authentication authentication,
            @RequestBody(required = false) SyncRequest request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();

            String providerId = principal.get("providerId"); // Neon Auth user ID
            String email = principal.get("email");
            String name = principal.get("name");

            // Fall back to sub if email claim is not present
            if (email == null || email.isBlank()) {
                throw new RuntimeException("Session does not contain an email.");
            }

            String role = (request != null) ? request.getRole() : null;

            SyncResponse response = authService.syncUser(providerId, email, name, role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * Fallback endpoint for cross-domain Google OAuth.
     * When third-party cookies are blocked, the frontend cannot read the session cookie.
     * This endpoint retrieves the most recently created session token.
     */
    @GetMapping("/latest-session")
    public ResponseEntity<?> getLatestSession() {
        try {
            String sql = "SELECT s.token, u.email " +
                         "FROM neon_auth.session s " +
                         "JOIN neon_auth.\"user\" u ON s.\"userId\" = u.id " +
                         "ORDER BY s.\"createdAt\" DESC LIMIT 1";
            
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(Map.of(
                "token", result.get("token"),
                "email", result.get("email")
            ));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return ResponseEntity.status(404).body(Map.of("error", "No recent session found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
