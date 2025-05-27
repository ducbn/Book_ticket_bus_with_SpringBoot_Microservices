package com.ducbn.chatService.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private Long senderId;        // From User Service
    private String senderName;    // Cached from external service
    private String senderType;    // CUSTOMER or BUS_OPERATOR

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;

    @CreationTimestamp
    private LocalDateTime sentAt;

    private Boolean isRead = false;

    // Optional: Message metadata
    @ElementCollection
    @CollectionTable(name = "message_metadata")
    private Map<String, String> metadata = new HashMap<>();
}
