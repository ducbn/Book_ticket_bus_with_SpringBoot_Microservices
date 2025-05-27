package com.ducbn.chatService.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to external services
    private Long customerId;      // From User Service
    private Long busOperatorId;   // From Bus Service
    private Long busRouteId;      // From Bus Service (optional)

    private String roomName;      // Generated room name for display

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime lastActivity;

    private Boolean isActive = true;

    // Metadata for external service info (cached for performance)
    private String customerName;
    private String busOperatorName;
    private String routeName;
}