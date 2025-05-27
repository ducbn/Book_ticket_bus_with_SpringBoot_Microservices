package com.ducbn.chatService.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long busOperatorId;
    private String busOperatorName;
    private Long busRouteId;
    private String routeName;
    private String roomName;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private Boolean isActive;
    private Long unreadCount;
    private ChatMessageDTO lastMessage;
}