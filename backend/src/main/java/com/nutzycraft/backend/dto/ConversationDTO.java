package com.nutzycraft.backend.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class ConversationDTO {
    private Long userId;
    private String name;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
