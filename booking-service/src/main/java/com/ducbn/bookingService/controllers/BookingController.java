package com.ducbn.bookingService.controllers;

import com.ducbn.bookingService.dtos.BookingDTO;
import com.ducbn.bookingService.models.Booking;
import com.ducbn.bookingService.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody @Validated BookingDTO request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Booking>> getBookingHistory(@PathVariable Long userId) {
        List<Booking> history = bookingService.getBookingHistory(userId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        Booking canceledBooking = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(canceledBooking);
    }


}
