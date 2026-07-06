package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.ConversationDTO;
import com.nutzycraft.backend.dto.MessageRequest;
import com.nutzycraft.backend.dto.MessageResponse;
import com.nutzycraft.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    // Get all conversations (latest message per user)
    @GetMapping
    public List<ConversationDTO> getConversations() {
        return chatService.getConversations(com.nutzycraft.backend.security.CurrentUser.email());
    }

    // Get chat history with a specific user
    @GetMapping("/{otherUserId}")
    public List<MessageResponse> getChatHistory(@PathVariable @NonNull Long otherUserId) {
        return chatService.getChatHistory(com.nutzycraft.backend.security.CurrentUser.email(), otherUserId);
    }

    // Send a message (sender is always the authenticated user, never the request body)
    @PostMapping
    public MessageResponse sendMessage(@RequestBody MessageRequest request) {
        return chatService.sendMessage(com.nutzycraft.backend.security.CurrentUser.email(), request.getReceiverId(), request.getContent());
    }
}
