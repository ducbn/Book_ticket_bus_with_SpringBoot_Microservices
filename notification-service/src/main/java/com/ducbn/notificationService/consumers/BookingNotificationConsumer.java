package com.ducbn.notificationService.consumers;

import com.ducbn.notificationService.event.BookingSuccessEvent;
import com.ducbn.notificationService.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingNotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "booking-success-topic", groupId = "notification-service-group")
    public void handleBookingSuccess(BookingSuccessEvent event) {
        try {
            log.info("Received booking success event for booking ID: {}", event.getBookingId());

            // Send email notification
            emailService.sendBookingConfirmationEmail(event);

            log.info("Email notification sent successfully for booking ID: {}", event.getBookingId());

        } catch (Exception e) {
            log.error("Failed to process booking success event for booking ID: {}",
                    event.getBookingId(), e);
            // You can implement retry logic or dead letter queue here
        }
    }
}