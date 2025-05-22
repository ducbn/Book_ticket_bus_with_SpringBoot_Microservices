package com.ducbn.bookingService.controllers;

import com.ducbn.bookingService.dtos.BookingRequestDTO;
import com.ducbn.bookingService.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody @Validated BookingRequestDTO request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }
}
