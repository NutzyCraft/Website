package com.nutzycraft.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String content;
    private LocalDateTime timestamp;
    private UserSummary sender;

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String email;
        private String fullName;
    }
}
