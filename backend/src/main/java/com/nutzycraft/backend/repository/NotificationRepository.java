package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findTop50ByOrderByCreatedAtDesc();

    List<Notification> findByRecipientEmailAndIsReadFalse(String email);

    List<Notification> findByRecipientIdAndIsReadFalse(Long userId);
}
