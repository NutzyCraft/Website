package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Message;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.MessageRepository;
import com.nutzycraft.backend.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

        @Autowired
        private MessageRepository messageRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private com.nutzycraft.backend.repository.NotificationRepository notificationRepository;

        // Get all conversations (latest message per user)
        @GetMapping
        public List<ConversationDTO> getConversations(@RequestParam String email) {
                User currentUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<Message> allMessages = messageRepository.findUserMessages(currentUser.getId());

                // Group by the *other* user and get the latest message
                Map<Long, Message> latestMessages = allMessages.stream()
                                .collect(Collectors.toMap(
                                                m -> m.getSender().getId().equals(currentUser.getId())
                                                                ? m.getReceiver().getId()
                                                                : m.getSender().getId(),
                                                m -> m,
                                                (m1, m2) -> m1.getTimestamp().isAfter(m2.getTimestamp()) ? m1 : m2));

                return latestMessages.values().stream()
                                .map(m -> {
                                        User otherUser = m.getSender().getId().equals(currentUser.getId())
                                                        ? m.getReceiver()
                                                        : m.getSender();
                                        return new ConversationDTO(
                                                        otherUser.getId(),
                                                        otherUser.getFullName(),
                                                        otherUser.getRole().toString(), // Helper for avatar or label
                                                        m.getContent(),
                                                        m.getTimestamp());
                                })
                                .sorted((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()))
                                .collect(Collectors.toList());
        }

        // Get chat history with a specific user
        @GetMapping("/{otherUserId}")
        public List<Message> getChatHistory(@RequestParam String email, @PathVariable Long otherUserId) {
                User currentUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return messageRepository.findChatHistory(currentUser.getId(), otherUserId);
        }

        // Send a message
        @PostMapping
        public Message sendMessage(@RequestBody SendMessageRequest request) {
                if (request.getReceiverId() == null) {
                        throw new IllegalArgumentException("Receiver ID cannot be null");
                }

                User sender = userRepository.findByEmail(request.getSenderEmail())
                                .orElseThrow(() -> new RuntimeException("Sender not found"));

                User receiver = userRepository.findById(request.getReceiverId())
                                .orElseThrow(() -> new RuntimeException("Receiver not found"));

                Message message = new Message();
                message.setSender(sender);
                message.setReceiver(receiver);
                message.setContent(request.getContent());
                message.setTimestamp(LocalDateTime.now());
                message.setRead(false);

                Message savedMessage = messageRepository.save(message);

                // Create Notification
                com.nutzycraft.backend.entity.Notification notification = new com.nutzycraft.backend.entity.Notification();
                notification.setRecipient(receiver);
                notification.setTitle("New Message from " + sender.getFullName());
                // Truncate message if too long
                String msgContent = request.getContent();
                if (msgContent != null && msgContent.length() > 50) {
                        msgContent = msgContent.substring(0, 47) + "...";
                }
                notification.setMessage("You have received a new message: " + msgContent);
                notification.setType("INFO");
                // Link to messages page with the sender selected
                try {
                        notification.setLink("messages.html?userId=" + sender.getId() + "&name="
                                        + java.net.URLEncoder.encode(sender.getFullName(),
                                                        java.nio.charset.StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                        notification.setLink("messages.html");
                }
                notificationRepository.save(notification);

                return savedMessage;
        }

        @Data
        public static class ConversationDTO {
                private Long userId;
                private String name;
                private String role;
                private String lastMessage;
                private LocalDateTime lastMessageTime;

                public ConversationDTO(Long userId, String name, String role, String lastMessage,
                                LocalDateTime lastMessageTime) {
                        this.userId = userId;
                        this.name = name;
                        this.role = role;
                        this.lastMessage = lastMessage;
                        this.lastMessageTime = lastMessageTime;
                }
        }

        @Data
        public static class SendMessageRequest {
                private String senderEmail;
                private Long receiverId;
                private String content;
        }
}
