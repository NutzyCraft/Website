package com.nutzycraft.backend.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String senderEmail;
    private Long receiverId;
    private String content;
}
