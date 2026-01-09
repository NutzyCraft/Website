package com.nutzycraft.backend.service;

import com.nutzycraft.backend.dto.ConversationDTO;
import com.nutzycraft.backend.entity.ChatMessage;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.ChatRepository;
import com.nutzycraft.backend.repository.UserRepository;
import com.nutzycraft.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public com.nutzycraft.backend.dto.MessageResponse sendMessage(String senderEmail, Long receiverId, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (receiverId == null) throw new IllegalArgumentException("Receiver ID cannot be null");
        
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setSenderId(sender.getId());
        message.setReceiverId(receiverId);
        message.setContent(content);
        
        ChatMessage saved = chatRepository.save(message);

         // Create Notification
        com.nutzycraft.backend.entity.Notification notification = new com.nutzycraft.backend.entity.Notification();
        notification.setRecipient(receiver);
        notification.setTitle("New Message from " + sender.getFullName());
        
        String msgContent = content;
        if (msgContent != null && msgContent.length() > 50) {
            msgContent = msgContent.substring(0, 47) + "...";
        }
        notification.setMessage("You have received a new message: " + msgContent);
        notification.setType("INFO");
        try {
             notification.setLink("messages.html?userId=" + sender.getId() + "&name="
                            + java.net.URLEncoder.encode(sender.getFullName(),
                                            java.nio.charset.StandardCharsets.UTF_8.toString()));
        } catch (Exception e) {
             notification.setLink("messages.html");
        }
        notificationRepository.save(notification);

        return com.nutzycraft.backend.dto.MessageResponse.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .timestamp(saved.getTimestamp())
                .sender(com.nutzycraft.backend.dto.MessageResponse.UserSummary.builder()
                        .id(sender.getId())
                        .email(sender.getEmail())
                        .fullName(sender.getFullName())
                        .build())
                .build();
    }

    public List<com.nutzycraft.backend.dto.MessageResponse> getChatHistory(String currentUserEmail, Long otherUserId) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Other user not found"));
        
        // Use new query method that excludes deleted messages
        List<ChatMessage> messages = chatRepository.findBySenderIdAndReceiverIdAndDeletedAtIsNullOrSenderIdAndReceiverIdAndDeletedAtIsNullOrderByTimestampAsc(
                currentUser.getId(), otherUserId,
                otherUserId, currentUser.getId()
        );

        return messages.stream().map(msg -> {
            boolean isMe = msg.getSenderId().equals(currentUser.getId());
            User sender = isMe ? currentUser : otherUser;
            
            return com.nutzycraft.backend.dto.MessageResponse.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .timestamp(msg.getTimestamp())
                .sender(com.nutzycraft.backend.dto.MessageResponse.UserSummary.builder()
                    .id(sender.getId())
                    .email(sender.getEmail())
                    .fullName(sender.getFullName())
                    .build())
                .build();
        }).collect(Collectors.toList());
    }

    public List<ConversationDTO> getConversations(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all messages where user is sender OR receiver, excluding deleted messages
        List<ChatMessage> allMessages = chatRepository.findBySenderIdOrReceiverIdAndDeletedAtIsNullOrderByTimestampDesc(
                currentUser.getId(), currentUser.getId()
        );

        // Map to unique conversations info
        Map<Long, ChatMessage> latestMessages = new HashMap<>();

        for (ChatMessage msg : allMessages) {
            Long otherId = msg.getSenderId().equals(currentUser.getId()) ? msg.getReceiverId() : msg.getSenderId();
            if (!latestMessages.containsKey(otherId)) {
                latestMessages.put(otherId, msg);
            }
        }

        List<ConversationDTO> conversations = new ArrayList<>();
        for (Map.Entry<Long, ChatMessage> entry : latestMessages.entrySet()) {
            Long otherId = entry.getKey();
            ChatMessage lastMsg = entry.getValue();
            
            Optional<User> otherUserOpt = userRepository.findById(otherId);
            if (otherUserOpt.isPresent()) {
                User otherUser = otherUserOpt.get();
                conversations.add(ConversationDTO.builder()
                        .userId(otherUser.getId())
                        .name(otherUser.getFullName())
                        .lastMessage(lastMsg.getContent())
                        .lastMessageTime(lastMsg.getTimestamp())
                        .build());
            }
        }

        return conversations;
    }
}
