package com.ducbn.busService.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatUpdateRequest {
    @JsonProperty("bus_id")
    private Long busId;

    @JsonProperty("seat_numbers")
    private List<String> seatNumbers;

    @JsonProperty("is_available")
    private Boolean isAvailable;
}