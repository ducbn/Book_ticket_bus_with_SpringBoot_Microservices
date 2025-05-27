package com.ducbn.chatService.repository;

import com.ducbn.chatService.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customerId = :customerId AND cr.busOperatorId = :busOperatorId AND cr.isActive = true")
    Optional<ChatRoom> findActiveRoom(@Param("customerId") Long customerId,
                                      @Param("busOperatorId") Long busOperatorId);

    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.customerId = :userId OR cr.busOperatorId = :userId) AND cr.isActive = true ORDER BY cr.lastActivity DESC")
    List<ChatRoom> findUserActiveRooms(@Param("userId") Long userId);

    List<ChatRoom> findByCustomerIdAndIsActive(Long customerId, Boolean isActive);
    List<ChatRoom> findByBusOperatorIdAndIsActive(Long busOperatorId, Boolean isActive);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.busRouteId = :routeId AND cr.customerId = :customerId AND cr.isActive = true")
    Optional<ChatRoom> findByRouteAndCustomer(@Param("routeId") Long routeId,
                                              @Param("customerId") Long customerId);
}