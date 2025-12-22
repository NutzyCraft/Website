package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.ConversationDTO;
import com.nutzycraft.backend.dto.MessageRequest;
import com.nutzycraft.backend.dto.MessageResponse;
import com.nutzycraft.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    // Get all conversations (latest message per user)
    @GetMapping
    public List<ConversationDTO> getConversations(@RequestParam String email) {
        return chatService.getConversations(email);
    }

    // Get chat history with a specific user
    @GetMapping("/{otherUserId}")
    public List<MessageResponse> getChatHistory(@RequestParam String email, @PathVariable Long otherUserId) {
        return chatService.getChatHistory(email, otherUserId);
    }

    // Send a message
    @PostMapping
    public MessageResponse sendMessage(@RequestBody MessageRequest request) {
        return chatService.sendMessage(request.getSenderEmail(), request.getReceiverId(), request.getContent());
    }
}
