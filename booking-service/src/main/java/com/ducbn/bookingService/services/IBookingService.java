package com.ducbn.bookingService.services;

import com.ducbn.bookingService.dtos.BookingDTO;
import com.ducbn.bookingService.models.Booking;

import java.util.List;

public interface IBookingService {
    Booking createBooking(BookingDTO bookingDTO);
    Booking cancelBooking(Long bookingId);
    List<Booking> getBookingHistory(Long userId);
}
