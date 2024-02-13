package com.example.bookingapp.dto.booking;

import java.time.LocalDate;

public record UpdateBookingRequestDto(
        LocalDate checkInDate, LocalDate checkOutDate, String status
) {

}
