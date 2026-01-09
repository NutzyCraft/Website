package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySenderIdOrReceiverIdOrderByTimestampDesc(Long senderId, Long receiverId);
    
    // Query methods that exclude deleted messages
    List<ChatMessage> findBySenderIdOrReceiverIdAndDeletedAtIsNullOrderByTimestampDesc(Long senderId, Long receiverId);
    
    // For specific chat history between A and B
    // We need (sender=A AND receiver=B) OR (sender=B AND receiver=A)
    // MongoDB repositories support this query structure
    List<ChatMessage> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
        Long senderId1, Long receiverId1, Long senderId2, Long receiverId2
    );
    
    // Conversation query that excludes deleted messages
    List<ChatMessage> findBySenderIdAndReceiverIdAndDeletedAtIsNullOrSenderIdAndReceiverIdAndDeletedAtIsNullOrderByTimestampAsc(
        Long senderId1, Long receiverId1, Long senderId2, Long receiverId2
    );
}
