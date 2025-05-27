package com.ducbn.chatService.service;

import com.ducbn.chatService.dto.CreateChatRoomRequest;
import com.ducbn.chatService.dto.SendMessageRequest;
import com.ducbn.chatService.model.ChatMessage;
import com.ducbn.chatService.model.ChatRoom;
import com.ducbn.chatService.repository.ChatMessageRepository;
import com.ducbn.chatService.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Tạo phòng chat mới nếu chưa tồn tại, hoặc trả về phòng chat đã tồn tại giữa người dùng và nhà xe.
     * Nếu có thông tin tuyến xe mới được truyền vào thì sẽ cập nhật.
     */
    public ChatRoom createOrGetChatRoom(CreateChatRoomRequest request) {
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findActiveRoom(request.getCustomerId(), request.getBusOperatorId());

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            // Update route info if provided
            if (request.getBusRouteId() != null) {
                room.setBusRouteId(request.getBusRouteId());
                room.setRouteName(request.getRouteName());
                room = chatRoomRepository.save(room);
            }
            return room;
        }

        // Tạo phòng mới nếu chưa có
        ChatRoom newRoom = ChatRoom.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .busOperatorId(request.getBusOperatorId())
                .busOperatorName(request.getBusOperatorName())
                .busRouteId(request.getBusRouteId())
                .routeName(request.getRouteName())
                .roomName(generateRoomName(request.getCustomerName(), request.getBusOperatorName()))
                .isActive(true)
                .build();

        return chatRoomRepository.save(newRoom);
    }

    /**
     * Lưu tin nhắn vào phòng chat và cập nhật thời gian hoạt động gần nhất của phòng.
     */
    public ChatMessage saveMessage(SendMessageRequest request) {
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .senderId(request.getSenderId())
                .senderName(request.getSenderName())
                .senderType(request.getSenderType())
                .content(request.getContent())
                .messageType(request.getMessageType())
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .isRead(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Cập nhật thời gian hoạt động gần nhất của phòng
        room.setLastActivity(LocalDateTime.now());
        chatRoomRepository.save(room);

        return savedMessage;
    }

    /**
     * Lấy danh sách tin nhắn trong phòng chat theo phân trang (trả về theo thứ tự thời gian tăng dần).
     */
    public List<ChatMessage> getRoomMessages(Long roomId, int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page, size);
        Page<ChatMessage> messagesPage = chatMessageRepository
                .findByRoomOrderBySentAtDesc(roomId, pageable);

        List<ChatMessage> messages = messagesPage.getContent();
        Collections.reverse(messages); // Trả về theo thứ tự thời gian từ cũ đến mới
        return messages;
    }

    /**
     * Lấy danh sách các phòng chat đang hoạt động của người dùng.
     */
    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatRoomRepository.findUserActiveRooms(userId);
    }

    /**
     * Đánh dấu tất cả các tin nhắn chưa đọc trong phòng chat là đã đọc đối với người dùng cụ thể.
     */
    public void markMessagesAsRead(Long roomId, Long userId) {
        chatMessageRepository.markMessagesAsRead(roomId, userId);
    }

    /**
     * Đếm số lượng tin nhắn chưa đọc trong phòng chat đối với người dùng cụ thể.
     */
    public Long getUnreadMessageCount(Long roomId, Long userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }

    /**
     * Lấy thông tin phòng chat theo ID.
     */
    public ChatRoom getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    /**
     * Kiểm tra người dùng có quyền truy cập vào phòng chat hay không (phải là khách hoặc nhà xe).
     */
    public boolean hasRoomAccess(Long roomId, Long userId) {
        ChatRoom room = getChatRoom(roomId);
        return room != null &&
                (room.getCustomerId().equals(userId) || room.getBusOperatorId().equals(userId));
    }

    /**
     * Vô hiệu hóa (đóng) một phòng chat (không xóa, chỉ set isActive = false).
     */
    public void deactivateRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setIsActive(false);
        chatRoomRepository.save(room);
    }

    /**
     * Tạo tên phòng chat từ tên khách hàng và tên nhà xe.
     */
    private String generateRoomName(String customerName, String busOperatorName) {
        return String.format("%s - %s", customerName, busOperatorName);
    }
}
