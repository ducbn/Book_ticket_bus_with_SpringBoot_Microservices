package com.ducbn.chatService.dto;

import com.ducbn.chatService.model.MessageType;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderType;
    private String content;
    private MessageType messageType = MessageType.TEXT;
    private Map<String, String> metadata;
}