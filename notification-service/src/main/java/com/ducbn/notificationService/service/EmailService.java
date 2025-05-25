package com.ducbn.notificationService.service;

import com.ducbn.notificationService.event.BookingSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    //private final UserService userService;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendBookingConfirmationEmail(BookingSuccessEvent event) {
        try {
            // Get user email from user service (you need to implement this)
            //String userEmail = userService.getUserEmail(event.getUserId());
            String userEmail = "22010065@st.phenikaa-uni.edu.vn";

            if (userEmail == null || userEmail.isEmpty()) {
                log.warn("User email not found for user ID: {}", event.getUserId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Booking Confirmation - Bus Ticket");
            message.setText(buildEmailContent(event));

            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", userEmail);

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for booking ID: {}",
                    event.getBookingId(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    private String buildEmailContent(BookingSuccessEvent event) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return String.format(
                "Dear Valued Customer,\n\n" +
                        "Your bus ticket booking has been confirmed successfully!\n\n" +
                        "Booking Details:\n" +
                        "- Booking ID: %d\n" +
                        "- Bus ID: %d\n" +
                        "- Seat Numbers: %s\n" +
                        "- Total Amount: %s\n" +
                        "- Booking Date: %s\n\n" +
                        "Please keep this email as your booking confirmation.\n" +
                        "Have a safe journey!\n\n" +
                        "Best regards,\n" +
                        "Bus Company Team",
                event.getBookingId(),
                event.getBusId(),
                String.join(", ", event.getSeatNumbers()),
                currencyFormat.format(event.getTotalAmount()),
                event.getBookingDate().format(dateFormatter)
        );
    }
}