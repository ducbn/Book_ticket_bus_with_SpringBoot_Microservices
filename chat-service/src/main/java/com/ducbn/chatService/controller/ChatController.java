package com.ducbn.chatService.controller;

import com.ducbn.chatService.dto.ChatMessageDTO;
import com.ducbn.chatService.dto.ChatRoomDTO;
import com.ducbn.chatService.dto.CreateChatRoomRequest;
import com.ducbn.chatService.dto.SendMessageRequest;
import com.ducbn.chatService.model.ChatMessage;
import com.ducbn.chatService.model.ChatRoom;
import com.ducbn.chatService.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        try {
            ChatRoom room = chatService.createOrGetChatRoom(request);
            ChatRoomDTO roomDTO = convertToRoomDTO(room);
            return ResponseEntity.ok(roomDTO);
        } catch (Exception e) {
            log.error("Error creating chat room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(@PathVariable Long userId) {
        try {
            List<ChatRoom> rooms = chatService.getUserChatRooms(userId);
            List<ChatRoomDTO> roomDTOs = rooms.stream()
                    .map(this::convertToRoomDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(roomDTOs);
        } catch (Exception e) {
            log.error("Error getting user chat rooms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            List<ChatMessage> messages = chatService.getRoomMessages(roomId, page, size);
            List<ChatMessageDTO> messageDTOs = messages.stream()
                    .map(this::convertToMessageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(messageDTOs);
        } catch (Exception e) {
            log.error("Error getting room messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDTO> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            ChatMessage message = chatService.saveMessage(request);
            ChatMessageDTO messageDTO = convertToMessageDTO(message);
            return ResponseEntity.ok(messageDTO);
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/rooms/{roomId}/read/{userId}")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long roomId, @PathVariable Long userId) {
        try {
            chatService.markMessagesAsRead(roomId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rooms/{roomId}/unread/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long roomId, @PathVariable Long userId) {
        try {
            Long count = chatService.getUnreadMessageCount(roomId, userId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            log.error("Error getting unread count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deactivateRoom(@PathVariable Long roomId) {
        try {
            chatService.deactivateRoom(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deactivating room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ChatRoomDTO convertToRoomDTO(ChatRoom room) {
        return ChatRoomDTO.builder()
                .id(room.getId())
                .customerId(room.getCustomerId())
                .customerName(room.getCustomerName())
                .busOperatorId(room.getBusOperatorId())
                .busOperatorName(room.getBusOperatorName())
                .busRouteId(room.getBusRouteId())
                .routeName(room.getRouteName())
                .roomName(room.getRoomName())
                .createdAt(room.getCreatedAt())
                .lastActivity(room.getLastActivity())
                .isActive(room.getIsActive())
                .unreadCount(0L) // Will be calculated separately if needed
                .build();
    }

    private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .metadata(message.getMetadata())
                .build();
    }
}