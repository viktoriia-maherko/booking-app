package com.example.bookingapp.dto.booking;

import java.time.LocalDate;

public record BookingResponseDto(LocalDate checkInDate,
        LocalDate checkOutDate,
        Long accommodationId,
        Long userId,
        String status) {

}
