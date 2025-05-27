package com.ducbn.chatService.dto;

import com.ducbn.chatService.model.MessageType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderType;
    private String content;
    private MessageType messageType;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private Map<String, String> metadata;
}