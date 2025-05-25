package com.ducbn.bookingService.repositories;

import com.ducbn.bookingService.models.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    List<BookingDetail> findByBookingId(Long bookingId);
}
