package com.example.bookingapp.service;

import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.dto.booking.CreateBookingRequestDto;
import com.example.bookingapp.dto.booking.UpdateBookingRequestDto;
import com.example.bookingapp.model.Booking;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponseDto save(CreateBookingRequestDto requestDto);

    List<BookingResponseDto> getBookingsByUserIdAndStatus(
            Long userId, Booking.Status status, Pageable pageable
    );

    List<BookingResponseDto> getBookingsOfCurrentUser(Pageable pageable);

    BookingResponseDto getById(Long id);

    BookingResponseDto updateById(Long id, UpdateBookingRequestDto requestDto);

    void deleteById(Long id);
}
