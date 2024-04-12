package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.dto.booking.CreateBookingRequestDto;
import com.example.bookingapp.dto.booking.UpdateBookingRequestDto;
import com.example.bookingapp.exception.DataProcessingException;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.BookingMapper;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.AccommodationRepository;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.service.BookingService;
import com.example.bookingapp.service.NotificationService;
import com.example.bookingapp.service.UserService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
        if (requestDto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new DataProcessingException("The check-in date must be in the future");
        }
        if (requestDto.getCheckOutDate().isBefore(requestDto.getCheckInDate())) {
            throw new DataProcessingException(
                    "The check-out date must be later than the check-in date"
            );
        }
        checkAvailabilityAndOverlap(
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
        checkAvailabilityAndOverlap(
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

    @Scheduled(cron = "0 0 0 * * *")
    public void processExpiredBookings() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(tomorrow);

        if (!expiredBookings.isEmpty()) {
            for (Booking booking : expiredBookings) {
                booking.setStatus(Booking.Status.EXPIRED);
                bookingRepository.save(booking);

                Accommodation accommodation = booking.getAccommodation();
                BookingResponseDto bookingDto = bookingMapper.toDto(booking);

                String notificationMessage = "Booking for accommodation with ID "
                        + accommodation.getId()
                        + " is expired. Details: " + bookingDto.toString();
                notificationService.sendTelegramNotification(notificationMessage);
            }
        } else {
            notificationService.sendTelegramNotification(
                    "There are no expired bookings for today!"
            );
        }
    }

    private void checkAvailabilityAndOverlap(LocalDate checkInDate,
                                             LocalDate checkOutDate,
                                             Accommodation accommodation) {
        List<Booking> bookings = bookingRepository
                .findAllBetweenCheckInDateAndCheckOutDate(checkInDate,
                        checkOutDate,
                        accommodation.getId());
        boolean checkAvailability = accommodation.getAvailability() - bookings.size() > 0;
        if (!checkAvailability) {
            throw new DataProcessingException(
                    "There are no available bookings for this accommodation in the interval from "
                            + checkInDate + " to " + checkOutDate
            );
        }

        boolean overlapExists = bookings.stream()
                .filter(booking -> booking.getStatus() != Booking.Status.CANCELED)
                .anyMatch(booking ->
                        (checkInDate.isBefore(booking.getCheckOutDate()) && checkOutDate
                                .isAfter(booking.getCheckInDate()))
                );

        if (overlapExists) {
            throw new DataProcessingException("Selected dates overlap with existing booking");
        }
    }
}
