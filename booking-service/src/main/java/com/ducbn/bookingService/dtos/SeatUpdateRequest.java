package com.ducbn.bookingService.dtos;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatUpdateRequest {
    private Long busId;
    private List<String> seatNumbers;
    private Boolean isAvailable;
}
