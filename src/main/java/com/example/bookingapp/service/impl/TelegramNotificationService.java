package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.service.NotificationService;
import com.example.bookingapp.telegram.NotificationBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final NotificationBot notificationBot;

    @Override
    public void notifyCreationNewBooking(BookingResponseDto bookingResponseDto) {
        String message = "Booking was created. Booking details: " + bookingResponseDto;
        notificationBot.sendNotification(message);
    }

    @Override
    public void notifyCancellationBooking(Long id) {
        String message = "Booking with id " + id + " was deleted";
        notificationBot.sendNotification(message);
    }

    @Override
    public void notifyCreationNewAccommodation(AccommodationResponseDto accommodationResponseDto) {
        String message = "Accommodation was created. Accommodation details: "
                + accommodationResponseDto;
        notificationBot.sendNotification(message);
    }

    @Override
    public void notifyCancellationAccommodation(Long id) {
        String message = "Accommodation with id " + id + " was deleted";
        notificationBot.sendNotification(message);
    }

    @Override
    public void notifySuccessfulPayment() {
        String message = "Payment successful. Payment details: "; // toDo(add PaymentResponseDto
        notificationBot.sendNotification(message);
    }
}
