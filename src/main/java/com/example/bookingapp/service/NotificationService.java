package com.example.bookingapp.service;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.booking.BookingResponseDto;

public interface NotificationService {
    void notifyCreationNewBooking(BookingResponseDto bookingResponseDto);

    void notifyCancellationBooking(Long id);

    void notifyCreationNewAccommodation(AccommodationResponseDto accommodationResponseDto);

    void notifyCancellationAccommodation(Long id);

    void notifySuccessfulPayment(Long paymentId);

    void sendTelegramNotification(String notificationMessage);
}
