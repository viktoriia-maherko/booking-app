package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.dto.booking.CreateBookingRequestDto;
import com.example.bookingapp.dto.booking.UpdateBookingRequestDto;
import com.example.bookingapp.exception.DataProcessingException;
import com.example.bookingapp.mapper.BookingMapper;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.AccommodationRepository;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.service.BookingService;
import com.example.bookingapp.service.NotificationService;
import com.example.bookingapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final AccommodationRepository accommodationRepository;
    private final NotificationService notificationService;

    @Override
    public BookingResponseDto save(CreateBookingRequestDto requestDto) {
        Accommodation accommodation = accommodationRepository
                .findById(requestDto.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find an accommodation by id "
                        + requestDto.getAccommodationId()));
        checkAvailabilityAndDateOfBooking(
                requestDto.getCheckInDate(), requestDto.getCheckOutDate(), accommodation
        );
        Booking booking = bookingMapper.toModel(requestDto);
        booking.setUser(userService.getAuthenticatedUserIfExists());
        booking.setAccommodation(accommodation);
        Booking savedBooking = bookingRepository.save(booking);
        BookingResponseDto responseDto = bookingMapper.toDto(savedBooking);
        notificationService.notifyCreationNewBooking(responseDto);
        return responseDto;
    }

    @Override
    public List<BookingResponseDto> getBookingsByUserIdAndStatus(Long userId,
                                                                 Booking.Status status,
                                                                 Pageable pageable) {
        if (userService.existsById(userId)) {
            return bookingRepository.findByUserIdAndStatus(userId, status, pageable)
                    .stream()
                    .map(bookingMapper::toDto)
                    .toList();
        } else {
            throw new EntityNotFoundException("Can't find an user by user id " + userId);
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsOfCurrentUser(Pageable pageable) {
        User user = userService.getAuthenticatedUserIfExists();
        List<Booking> bookings = bookingRepository.findAllByUserId(user.getId(), pageable);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public BookingResponseDto getById(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException(
                "Can't find a booking by id " + id
        ));
        return bookingMapper.toDto(booking);
    }

    @Override
    public BookingResponseDto updateById(Long id, UpdateBookingRequestDto requestDto) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Can't find a booking by id " + id));
        checkAvailabilityAndDateOfBooking(
                requestDto.checkInDate(), requestDto.checkOutDate(), booking.getAccommodation()
        );
        booking.setCheckInDate(requestDto.checkInDate());
        booking.setCheckOutDate(requestDto.checkOutDate());
        booking.setStatus(Booking.Status.valueOf(requestDto.status()));
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    public void deleteById(Long id) {
        notificationService.notifyCancellationBooking(id);
        bookingRepository.deleteById(id);
    }

    private void checkAvailabilityAndDateOfBooking(LocalDate checkInDate,
                                                      LocalDate checkOutDate,
                                                      Accommodation accommodation) {
        List<Booking> bookings = bookingRepository
                .findAllBetweenCheckInDateAndCheckOutDate(checkInDate,
                        checkOutDate, accommodation.getId());
        List<Booking> goodBookings = checkStatus(bookings);
        boolean checkAvailability = accommodation
                .getAvailability() - goodBookings.size() > 0;
        if (!bookings.isEmpty() && !checkAvailability) {
            throw new DataProcessingException(
                    "There are no available bookings in the interval from "
                    + checkInDate + " to " + checkOutDate
            );
        }
    }

    private List<Booking> checkStatus(List<Booking> bookings) {
        List<Booking> bookingWithPendingStatus = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking != null && (booking.getStatus().equals(Booking.Status.PENDING)
                    || booking.getStatus().equals(Booking.Status.CONFIRMED))) {
                bookingWithPendingStatus.add(booking);
            }
        }
        return bookingWithPendingStatus;
    }
}
