package com.ducbn.chatService.repository;

import com.ducbn.chatService.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByRoomOrderBySentAt(@Param("roomId") Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findByRoomOrderBySentAtDesc(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId AND cm.senderId != :userId AND cm.isRead = false")
    Long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chatRoom.id = :roomId AND cm.senderId != :userId")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.senderId = :userId ORDER BY cm.sentAt DESC")
    List<ChatMessage> findBySenderIdOrderBySentAtDesc(@Param("userId") Long userId);
}
