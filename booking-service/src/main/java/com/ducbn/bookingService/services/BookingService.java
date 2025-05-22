package com.ducbn.bookingService.services;

import com.ducbn.bookingService.dtos.BookingRequestDTO;
import com.ducbn.bookingService.dtos.SeatUpdateRequest;
import com.ducbn.bookingService.models.Booking;
import com.ducbn.bookingService.models.BookingDetail;
import com.ducbn.bookingService.repositories.BookingDetailRepository;
import com.ducbn.bookingService.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final WebClient.Builder webClientBuilder;

    public Booking createBooking(BookingRequestDTO request) {

        // 1. Tạo booking chính
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .busId(request.getBusId())
                .bookingDate(LocalDate.parse(request.getBookingDate()))
                .totalAmount(request.getTotalAmount())
                .status("CONFIRMED")
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 2. Tạo các booking detail
        List<BookingDetail> details = request.getSeatNumbers().stream().map(seat ->
                BookingDetail.builder()
                        .booking(savedBooking)
                        .seatNumber(seat)
                        .price(request.getTotalAmount() / request.getSeatNumbers().size()) // tạm chia đều
                        .build()
        ).collect(Collectors.toList());

        bookingDetailRepository.saveAll(details);

        // 3. Gọi sang bus-service để cập nhật ghế
        SeatUpdateRequest updateRequest = SeatUpdateRequest.builder()
                .busId(request.getBusId())
                .seatNumbers(request.getSeatNumbers())
                .isAvailable(false) // Đánh dấu là đã đặt
                .build();

        webClientBuilder.build()
                .put()
                .uri("http://bus-service/api/seats/update-status")
                .bodyValue(updateRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .block(); // đồng bộ đơn giản, có thể dùng async nếu muốn

        return savedBooking;
    }
}
