package com.ducbn.busService.services;

import com.ducbn.busService.dtos.SeatUpdateRequest;
import com.ducbn.busService.responses.SeatResponse;

import java.util.List;

public interface ISeatService {
    List<SeatResponse> getAllSeatsByBusId(Long busId);
    List<SeatResponse> getAvailableSeatsByBusId(Long busId);

    void updateSeatStatus(SeatUpdateRequest request);
}
