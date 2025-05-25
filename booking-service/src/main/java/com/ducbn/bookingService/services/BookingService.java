package com.ducbn.bookingService.services;

import com.ducbn.bookingService.dtos.BookingDTO;
import com.ducbn.bookingService.dtos.SeatUpdateRequest;
import com.ducbn.bookingService.event.BookingSuccessEvent;
import com.ducbn.bookingService.models.Booking;
import com.ducbn.bookingService.models.BookingDetail;
import com.ducbn.bookingService.models.BookingStatus;
import com.ducbn.bookingService.repositories.BookingDetailRepository;
import com.ducbn.bookingService.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService{
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BOOKING_SUCCESS_TOPIC = "booking-success-topic";

    @Override
    @Transactional
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

        // 5. Send Kafka event for email notification
        try {
            BookingSuccessEvent event = BookingSuccessEvent.builder()
                    .bookingId(savedBooking.getId())
                    .userId(savedBooking.getUserId())
                    .busId(savedBooking.getBusId())
                    .seatNumbers(bookingDTO.getSeatNumbers())
                    .totalAmount(savedBooking.getTotalAmount())
                    .bookingDate(savedBooking.getBookingDate())
                    .build();

            kafkaTemplate.send(BOOKING_SUCCESS_TOPIC, event);
            log.info("Booking success event sent to Kafka for booking ID: {}", savedBooking.getId());

        } catch (Exception e) {
            // Log error but don't fail the booking process
            log.error("Failed to send booking success event to Kafka for booking ID: {}",
                    savedBooking.getId(), e);
        }
        return savedBooking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingHistory(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already canceled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        try {
            SeatUpdateRequest seatUpdateRequest = SeatUpdateRequest.builder()
                    .busId(booking.getBusId())
                    .seatNumbers(
                            bookingDetailRepository.findByBookingId(bookingId).stream()
                                    .map(BookingDetail::getSeatNumber)
                                    .collect(Collectors.toList())
                    )
                    .isAvailable(true)
                    .build();

            WebClient webClient = webClientBuilder.build();
            webClient.put()
                    .uri("http://bus-service/api/seats/update-status")
                    .bodyValue(seatUpdateRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            log.error("Failed to update seat status after cancel booking: {}", e.getMessage());
        }

        return booking;
    }
}