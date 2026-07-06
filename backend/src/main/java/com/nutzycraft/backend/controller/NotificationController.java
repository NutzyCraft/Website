package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.entity.Notification;
import com.nutzycraft.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public List<NotificationDTO> getNotifications() {
        String email = com.nutzycraft.backend.security.CurrentUser.email();
        List<Notification> notifications = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);

        return notifications.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable @NonNull Long id) {
        String email = com.nutzycraft.backend.security.CurrentUser.email();
        notificationRepository.findById(id).ifPresent(notification -> {
            if (!notification.getRecipient().getEmail().equalsIgnoreCase(email)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "Not your notification");
            }
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @PostMapping("/mark-all-read")
    public void markAllAsRead() {
        String email = com.nutzycraft.backend.security.CurrentUser.email();
        List<Notification> unread = notificationRepository.findByRecipientEmailAndIsReadFalse(email);

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationDTO convertToDTO(Notification n) {
        return new NotificationDTO(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt());
    }

    @lombok.Data
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String message;
        private String type;
        private String link;
        private boolean read;
        private java.time.LocalDateTime createdAt;

        public NotificationDTO(Long id, String title, String message, String type, String link, boolean read,
                java.time.LocalDateTime createdAt) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.type = type;
            this.link = link;
            this.read = read;
            this.createdAt = createdAt;
        }
    }
}
