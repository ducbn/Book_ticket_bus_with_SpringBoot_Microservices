package com.ducbn.bookingService.services;

import com.ducbn.bookingService.dtos.BookingDTO;
import com.ducbn.bookingService.dtos.SeatUpdateRequest;
import com.ducbn.bookingService.models.Booking;
import com.ducbn.bookingService.models.BookingDetail;
import com.ducbn.bookingService.models.BookingStatus;
import com.ducbn.bookingService.repositories.BookingDetailRepository;
import com.ducbn.bookingService.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final WebClient.Builder webClientBuilder;

    public Booking createBooking(BookingDTO bookingDTO) {
        // 1. Check if user id exists
        // ... implementation needed

        // 2. Create booking
        Booking booking = Booking.builder()
                .userId(bookingDTO.getUserId())
                .busId(bookingDTO.getBusId())
                .bookingDate(LocalDateTime.now())
                .totalAmount(bookingDTO.getTotalAmount())
                .status(BookingStatus.PENDING)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        try {
            // 3. Send HTTP request to update seat status
            SeatUpdateRequest seatUpdateRequest = SeatUpdateRequest.builder()
                    .busId(bookingDTO.getBusId())
                    .seatNumbers(bookingDTO.getSeatNumbers())
                    .isAvailable(false)
                    .build();

            WebClient webClient = webClientBuilder.build();
            webClient.put()
                    .uri("http://bus-service/api/seats/update-status")
                    .bodyValue(seatUpdateRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10)) // Add timeout
                    .block();

        } catch (WebClientResponseException e) {
            // Rollback booking if seat update fails
            bookingRepository.delete(savedBooking);
            throw new RuntimeException("Bus service unavailable. Booking cancelled: " + e.getMessage());
        } catch (Exception e) {
            // Rollback booking for any other error
            bookingRepository.delete(savedBooking);
            throw new RuntimeException("Failed to update seat status. Booking cancelled: " + e.getMessage());
        }

        // 4. Create booking details
        BigDecimal pricePerSeat = bookingDTO.getTotalAmount()
                .divide(BigDecimal.valueOf(bookingDTO.getSeatNumbers().size()), 2, RoundingMode.HALF_UP);

        List<BookingDetail> details = bookingDTO.getSeatNumbers().stream()
                .map(seat -> BookingDetail.builder()
                        .booking(savedBooking)
                        .seatNumber(seat)
                        .price(pricePerSeat)
                        .build())
                .collect(Collectors.toList());

        bookingDetailRepository.saveAll(details);
        return savedBooking;
    }
}