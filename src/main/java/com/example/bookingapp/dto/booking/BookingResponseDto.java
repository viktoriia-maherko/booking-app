package com.example.bookingapp.dto.booking;

import java.time.LocalDate;
import lombok.Data;

@Data
public class BookingResponseDto {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long accommodationId;
    private Long userId;
    private String status;

}
