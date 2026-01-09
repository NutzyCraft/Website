package com.nutzycraft.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "messages")
public class ChatMessage {
    @Id
    private String id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();
    private LocalDateTime deletedAt;
}
