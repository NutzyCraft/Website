package com.nutzycraft.backend.dto;

import lombok.Data;

public class AuthDTOs {

    @Data
    public static class SyncRequest {
        private String role; // "CLIENT" or "FREELANCER" — only used on first-time registration
    }

    @Data
    public static class SyncResponse {
        private Long id;
        private String email;
        private String fullName;
        private String role;
        private boolean isNew;
    }
}
