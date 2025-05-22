package com.ducbn.bookingService.repositories;

import com.ducbn.bookingService.models.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
}
