package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.UserRepository;
import com.example.bookingapp.service.NotificationService;
import com.example.bookingapp.telegram.NotificationBot;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final NotificationBot notificationBot;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    @Value("${admin.chatId}")
    private Long adminChatId;

    @Override
    public void notifyCreationNewBooking(BookingResponseDto bookingResponseDto) {
        User user = userRepository.findById(bookingResponseDto.getUserId()).get();
        Long chatId = user.getChatId();
        String message = "Booking was created. Booking details: " + bookingResponseDto;
        notificationBot.sendNotification(message, chatId);
    }

    @Override
    public void notifyCancellationBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Can't find a booking by id " + id));
        User user = userRepository.findById(booking.getUser().getId()).get();
        Long chatId = user.getChatId();
        String message = "Booking with id " + id + " was deleted";
        notificationBot.sendNotification(message, chatId);
    }

    @Override
    public void notifyCreationNewAccommodation(AccommodationResponseDto accommodationResponseDto) {
        String message = "Accommodation was created. Accommodation details: "
                + accommodationResponseDto;
        notificationBot.sendNotification(message, adminChatId);
    }

    @Override
    public void notifyCancellationAccommodation(Long id) {
        String message = "Accommodation with id " + id + " was deleted";
        notificationBot.sendNotification(message, adminChatId);
    }

    @Override
    public void notifySuccessfulPayment(Long paymentId) {
        String message = "Payment successful. Payment ID: " + paymentId;
        notificationBot.sendNotification(message, adminChatId);
    }
}
