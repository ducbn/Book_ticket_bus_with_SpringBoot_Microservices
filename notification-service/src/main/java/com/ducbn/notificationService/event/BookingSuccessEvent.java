package com.ducbn.notificationService.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSuccessEvent {
    private Long bookingId;
    private Long userId;
    private Long busId;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private LocalDateTime bookingDate;
}