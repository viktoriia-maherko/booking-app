package com.example.bookingapp.controller;

import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.dto.booking.CreateBookingRequestDto;
import com.example.bookingapp.dto.booking.UpdateBookingRequestDto;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Booking management", description = "Endpoints for managing bookings")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    @Operation(summary = "Create a new booking",
            description = "Create a new booking")
    public BookingResponseDto save(@RequestBody @Valid CreateBookingRequestDto requestDto) {
        return bookingService.save(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    @Operation(summary = "Get bookings by user id and status",
            description = "Get a list of all bookings based on user id and their status")
    public List<BookingResponseDto> getBookingsByUserIdAndStatus(
            @RequestParam Long userId,
            @RequestParam Booking.Status status,
            Pageable pageable
    ) {
        return bookingService.getBookingsByUserIdAndStatus(userId, status, pageable);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/my")
    @Operation(summary = "Get bookings", description = "Get a list of current user's bookings")
    public List<BookingResponseDto> getBookingsOfCurrentUser(Pageable pageable) {
        return bookingService.getBookingsOfCurrentUser(pageable);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    @Operation(summary = "Get a booking",
            description = "Get a booking by id")
    public BookingResponseDto getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping(value = "/{id}")
    @Operation(summary = "Update a booking",
            description = "Update a booking by id")
    public BookingResponseDto updateById(@PathVariable Long id,
                                         @RequestBody @Valid UpdateBookingRequestDto requestDto) {
        return bookingService.updateById(id, requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a booking", description = "Delete a booking by id")
    public void deleteById(@PathVariable Long id) {
        bookingService.deleteById(id);
    }
}
