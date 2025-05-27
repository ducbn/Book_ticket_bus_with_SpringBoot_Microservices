package com.ducbn.chatService.websocket;

import com.ducbn.chatService.dto.ChatMessageDTO;
import com.ducbn.chatService.dto.ChatRoomDTO;
import com.ducbn.chatService.dto.SendMessageRequest;
import com.ducbn.chatService.dto.WebSocketMessageDTO;
import com.ducbn.chatService.model.ChatMessage;
import com.ducbn.chatService.model.ChatRoom;
import com.ducbn.chatService.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    private final ChatService chatService;
    private final ObjectMapper objectMapper;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        String userType = getUserTypeFromSession(session);

        if (userId != null) {
            String sessionKey = session.getId();
            sessions.put(sessionKey, session);
            userSessions.computeIfAbsent(userId, k -> new HashSet<>()).add(sessionKey);

            log.info("User {} ({}) connected with session {}", userId, userType, sessionKey);

            // Send user's chat rooms
            sendUserChatRooms(userId, session);
        } else {
            log.warn("Connection established without valid user ID");
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            WebSocketMessageDTO wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessageDTO.class);
            Long userId = getUserIdFromSession(session);

            if (userId == null) {
                sendError(session, "Unauthorized");
                return;
            }

            log.info("Handling message type: {} from user: {}", wsMessage.getType(), userId);

            switch (wsMessage.getType()) {
                case "SEND_MESSAGE":
                    handleSendMessage(wsMessage, userId);
                    break;
                case "JOIN_ROOM":
                    handleJoinRoom(wsMessage, userId, session);
                    break;
                case "MARK_READ":
                    handleMarkRead(wsMessage, userId);
                    break;
                case "GET_ROOMS":
                    sendUserChatRooms(userId, session);
                    break;
                case "TYPING":
                    handleTyping(wsMessage, userId);
                    break;
                default:
                    log.warn("Unknown message type: {}", wsMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        String sessionKey = session.getId();

        sessions.remove(sessionKey);

        if (userId != null) {
            Set<String> userSessionIds = userSessions.get(userId);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionKey);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        log.info("Session {} closed for user {}", sessionKey, userId);
    }

    private void handleSendMessage(WebSocketMessageDTO wsMessage, Long userId) {
        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .roomId(wsMessage.getRoomId())
                    .senderId(userId)
                    .senderName(wsMessage.getMessage().getSenderName())
                    .senderType(wsMessage.getMessage().getSenderType())
                    .content(wsMessage.getMessage().getContent())
                    .messageType(wsMessage.getMessage().getMessageType())
                    .metadata(wsMessage.getMessage().getMetadata())
                    .build();

            ChatMessage savedMessage = chatService.saveMessage(request);

            // Broadcast to room participants
            broadcastToRoom(savedMessage.getChatRoom().getId(),
                    WebSocketMessageDTO.builder()
                            .type("NEW_MESSAGE")
                            .message(convertToDTO(savedMessage))
                            .build());

        } catch (Exception e) {
            log.error("Error handling send message", e);
        }
    }

    private void handleJoinRoom(WebSocketMessageDTO wsMessage, Long userId, WebSocketSession session) {
        try {
            Long roomId = wsMessage.getRoomId();

            // Verify user has access to room
            if (!chatService.hasRoomAccess(roomId, userId)) {
                sendError(session, "Access denied to room");
                return;
            }

            // Get room messages
            List<ChatMessage> messages = chatService.getRoomMessages(roomId, 0, 50); // Last 50 messages

            WebSocketMessageDTO response = WebSocketMessageDTO.builder()
                    .type("ROOM_MESSAGES")
                    .roomId(roomId)
                    .messages(messages.stream().map(this::convertToDTO).collect(Collectors.toList()))
                    .build();

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

            // Mark messages as read
            chatService.markMessagesAsRead(roomId, userId);

        } catch (Exception e) {
            log.error("Error handling join room", e);
            sendError(session, "Error joining room");
        }
    }

    private void handleMarkRead(WebSocketMessageDTO wsMessage, Long userId) {
        try {
            chatService.markMessagesAsRead(wsMessage.getRoomId(), userId);

            // Notify other participants about read status
            broadcastToRoom(wsMessage.getRoomId(),
                    WebSocketMessageDTO.builder()
                            .type("MESSAGES_READ")
                            .roomId(wsMessage.getRoomId())
                            .data(Map.of("readBy", userId))
                            .build());

        } catch (Exception e) {
            log.error("Error handling mark read", e);
        }
    }

    private void handleTyping(WebSocketMessageDTO wsMessage, Long userId) {
        try {
            // Broadcast typing indicator to other room participants
            broadcastToRoomExceptSender(wsMessage.getRoomId(), userId,
                    WebSocketMessageDTO.builder()
                            .type("USER_TYPING")
                            .roomId(wsMessage.getRoomId())
                            .data(Map.of("userId", userId, "typing", wsMessage.getData().get("typing")))
                            .build());

        } catch (Exception e) {
            log.error("Error handling typing", e);
        }
    }

    private void sendUserChatRooms(Long userId, WebSocketSession session) {
        try {
            List<ChatRoom> rooms = chatService.getUserChatRooms(userId);
            List<ChatRoomDTO> roomDTOs = rooms.stream()
                    .map(this::convertToRoomDTO)
                    .collect(Collectors.toList());

            WebSocketMessageDTO response = WebSocketMessageDTO.builder()
                    .type("USER_CHAT_ROOMS")
                    .chatRooms(roomDTOs)
                    .build();

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            log.error("Error sending user chat rooms", e);
        }
    }

    private void broadcastToRoom(Long roomId, WebSocketMessageDTO message) {
        try {
            ChatRoom room = chatService.getChatRoom(roomId);
            if (room != null) {
                sendToUser(room.getCustomerId(), message);
                sendToUser(room.getBusOperatorId(), message);
            }
        } catch (Exception e) {
            log.error("Error broadcasting to room", e);
        }
    }

    private void broadcastToRoomExceptSender(Long roomId, Long senderId, WebSocketMessageDTO message) {
        try {
            ChatRoom room = chatService.getChatRoom(roomId);
            if (room != null) {
                if (!room.getCustomerId().equals(senderId)) {
                    sendToUser(room.getCustomerId(), message);
                }
                if (!room.getBusOperatorId().equals(senderId)) {
                    sendToUser(room.getBusOperatorId(), message);
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting to room except sender", e);
        }
    }

    private void sendToUser(Long userId, WebSocketMessageDTO message) {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds != null && !sessionIds.isEmpty()) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                sessionIds.forEach(sessionId -> {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(messageJson));
                        } catch (Exception e) {
                            log.error("Error sending message to session {}", sessionId, e);
                        }
                    }
                });
            } catch (Exception e) {
                log.error("Error serializing message for user {}", userId, e);
            }
        }
    }

    private void sendError(WebSocketSession session, String error) {
        try {
            WebSocketMessageDTO errorMessage = WebSocketMessageDTO.builder()
                    .type("ERROR")
                    .error(error)
                    .build();

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
        } catch (Exception e) {
            log.error("Error sending error message", e);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        // Extract from query params or headers
        // In real implementation, this would come from JWT token or session
        String userIdStr = session.getUri().getQuery();
        if (userIdStr != null && userIdStr.contains("userId=")) {
            try {
                return Long.parseLong(userIdStr.split("userId=")[1].split("&")[0]);
            } catch (Exception e) {
                log.error("Error parsing userId from session", e);
            }
        }
        return null;
    }

    private String getUserTypeFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userType=")) {
            try {
                return query.split("userType=")[1].split("&")[0];
            } catch (Exception e) {
                log.error("Error parsing userType from session", e);
            }
        }
        return "CUSTOMER";
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
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
                .build();
    }
}