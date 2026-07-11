package com.nutzycraft.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Presence ping for a user, stored in MongoDB alongside chat messages.
 * The document id is the user's (SQL) id, so each user has exactly one
 * presence record that heartbeats overwrite in place.
 */
@Data
@Document(collection = "presence")
public class UserPresence {
    @Id
    private Long userId;

    // True while the user holds at least one live WebSocket session.
    private boolean online;

    // When the user's last session ended (or last connected). Used for "last seen".
    private LocalDateTime lastSeen;
}
