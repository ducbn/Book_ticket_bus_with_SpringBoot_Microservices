package com.ducbn.chatService.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO {
    private String type;
    private ChatMessageDTO message;
    private ChatRoomDTO chatRoom;
    private List<ChatMessageDTO> messages;
    private List<ChatRoomDTO> chatRooms;
    private Long roomId;
    private String error;
    private Map<String, Object> data;
}