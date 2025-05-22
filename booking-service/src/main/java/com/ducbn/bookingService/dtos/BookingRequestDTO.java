package com.ducbn.bookingService.dtos;

import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotEmpty(message = "Seats must not be empty")
    private List<String> seatNumbers;

    @NotNull(message = "Total amount is required")
    private Double totalAmount;
}