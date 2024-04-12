package com.example.bookingapp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.bookingapp.dto.booking.BookingResponseDto;
import com.example.bookingapp.dto.booking.CreateBookingRequestDto;
import com.example.bookingapp.dto.booking.UpdateBookingRequestDto;
import com.example.bookingapp.exception.DataProcessingException;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.BookingMapper;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Address;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Role;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.AccommodationRepository;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.service.impl.BookingServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private static final Long USER_ID = 1L;
    private static final Long ACCOMMODATION_ID = 1L;
    private static final LocalDate BOOKING_CHECK_IN = LocalDate.now();
    private static final LocalDate BOOKING_CHECK_OUT = LocalDate.now().plusDays(1);
    private static final LocalDate UPDATE_BOOKING_CHECK_IN = LocalDate.now().plusWeeks(1);
    private static final LocalDate UPDATE_BOOKING_CHECK_OUT = LocalDate.now().plusWeeks(2);
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserService userService;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    @DisplayName("Verify saving booking to DB")
    void save_ValidCreateBookingRequestDto_BookingResponseDto() {
        CreateBookingRequestDto createBookingRequestDto
                = createBookingRequestDto();
        Booking booking = createBooking();
        BookingResponseDto expected = createBookingResponseDto();

        when(accommodationRepository.findById(ACCOMMODATION_ID))
                .thenReturn(Optional.of(createAccommodation1()));
        when(bookingRepository.findAllBetweenCheckInDateAndCheckOutDate(
                BOOKING_CHECK_IN, BOOKING_CHECK_OUT, ACCOMMODATION_ID
        )).thenReturn(Collections.emptyList());
        when(bookingMapper.toModel(createBookingRequestDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(expected);
        BookingResponseDto actual = bookingService.save(createBookingRequestDto);

        notificationService.notifyCreationNewBooking(expected);
        notificationService.notifyCreationNewBooking(actual);

        Assertions.assertEquals(expected, actual);
        Assertions.assertNotNull(actual);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("Test save method with invalid accommodation ID")
    void save_InvalidAccommodationId_ShouldThrowEntityException() {
        CreateBookingRequestDto requestDto = createBookingRequestDto();
        requestDto.setAccommodationId(-10L);

        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.save(requestDto)
        );

        String expected = "Can't find an accommodation by id "
                + requestDto.getAccommodationId();
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    @DisplayName("Test save method with check-in date in the past")
    void save_CheckInDateInThePast_ShouldThrowDataProcessingException() {
        CreateBookingRequestDto requestDto = createBookingRequestDto();
        requestDto.setCheckInDate(LocalDate.now().minusDays(1));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);

        when(accommodationRepository.findById(anyLong())).thenReturn(Optional.of(accommodation));

        Assertions.assertThrows(DataProcessingException.class, ()
                -> bookingService.save(requestDto));
        verify(bookingRepository, never()).save(any());
        verify(notificationService, never()).notifyCreationNewBooking(any());
    }

    @Test
    @DisplayName("Test save method with check-out date before check-in date")
    void save_ShouldThrowDataProcessingException() {
        CreateBookingRequestDto requestDto = new CreateBookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDate.now().plusDays(2));
        requestDto.setCheckOutDate(LocalDate.now().plusDays(1));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);

        when(accommodationRepository.findById(anyLong())).thenReturn(Optional.of(accommodation));

        Assertions.assertThrows(DataProcessingException.class, ()
                -> bookingService.save(requestDto));
        verify(bookingRepository, never()).save(any());
        verify(notificationService, never()).notifyCreationNewBooking(any());
    }

    @Test
    @DisplayName("Verify the correct list of bookingsDto was returned")
    void getAll_ValidPageableUserIdAndStatus_ReturnsAllBookings() {
        Booking booking1 = createBooking();
        Booking booking2 = createBooking();
        booking2.setAccommodation(createAccommodation2());

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(booking1, booking2);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable,
                bookings.size());

        when(userService.existsById(USER_ID)).thenReturn(true);
        when(bookingRepository.findByUserIdAndStatus(USER_ID,
                Booking.Status.PENDING, pageable)).thenReturn(bookingPage);

        BookingResponseDto bookingResponseDto1 = createBookingResponseDto();
        BookingResponseDto bookingResponseDto2 = createBookingResponseDto();
        bookingResponseDto2.setAccommodationId(2L);

        when(bookingMapper.toDto(booking1)).thenReturn(bookingResponseDto1);
        when(bookingMapper.toDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> expected = List.of(bookingResponseDto1, bookingResponseDto2);
        List<BookingResponseDto> actual = bookingService
                .getBookingsByUserIdAndStatus(USER_ID, Booking.Status.PENDING, pageable);

        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertNotNull(actual);
        verifyNoMoreInteractions(userService, bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("Verify the exception was thrown when bookingId was incorrect")
    void getAll_InvalidPageableUserIdAndStatus_ShouldThrowEntityException() {
        Long userId = -10L;
        Pageable pageable = PageRequest.of(0, 10);

        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getBookingsByUserIdAndStatus(userId,
                        Booking.Status.EXPIRED, pageable)
        );

        String expected = "Can't find an user by user id " + userId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    @DisplayName("Verify the correct list of bookingsDto was returned")
    void getAll_ValidPageableAndUser_ReturnsAllBookings() {
        Booking booking1 = createBooking();
        Booking booking2 = createBooking();
        booking2.setAccommodation(createAccommodation2());

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(booking1, booking2);

        when(userService.getAuthenticatedUserIfExists()).thenReturn(createUser());
        when(bookingRepository.findAllByUserId(USER_ID, pageable)).thenReturn(bookings);

        BookingResponseDto bookingResponseDto1 = createBookingResponseDto();
        BookingResponseDto bookingResponseDto2 = createBookingResponseDto();
        bookingResponseDto2.setAccommodationId(2L);

        when(bookingMapper.toDto(booking1)).thenReturn(bookingResponseDto1);
        when(bookingMapper.toDto(booking2)).thenReturn(bookingResponseDto2);

        List<BookingResponseDto> expected = List.of(bookingResponseDto1, bookingResponseDto2);
        List<BookingResponseDto> actual = bookingService
                .getBookingsOfCurrentUser(pageable);

        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertNotNull(actual);
        verifyNoMoreInteractions(userService, bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("Verify the correct bookingDto was returned by valid bookingId")
    void getById_WithValidBookingId_ShouldReturnValidBookingDto() {
        Booking booking = createBooking();
        BookingResponseDto expected = createBookingResponseDto();

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        BookingResponseDto actual = bookingService.getById(booking.getId());
        Assertions.assertEquals(expected, actual);
        Assertions.assertNotNull(actual);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("Verify the exception was thrown when bookingId was incorrect")
    void getById_WithInvalidBookingId_ShouldThrowEntityException() {
        Long bookingId = -10L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getById(bookingId)
        );

        String expected = "Can't find a booking by id " + bookingId;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    @DisplayName("Verify the bookingDto was updated correctly "
            + "by valid bookingId and updateBookingRequestDto")
    void updateById() {
        BookingResponseDto expected = createBookingResponseDto();
        expected.setCheckInDate(UPDATE_BOOKING_CHECK_IN);
        expected.setCheckOutDate(UPDATE_BOOKING_CHECK_OUT);
        Booking booking = createBooking();

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findAllBetweenCheckInDateAndCheckOutDate(
                UPDATE_BOOKING_CHECK_IN, UPDATE_BOOKING_CHECK_OUT, ACCOMMODATION_ID
        )).thenReturn(List.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        UpdateBookingRequestDto requestDto = new UpdateBookingRequestDto(UPDATE_BOOKING_CHECK_IN,
                UPDATE_BOOKING_CHECK_OUT, Booking.Status.PENDING.name());
        BookingResponseDto actual = bookingService.updateById(booking.getId(),
                requestDto);

        Assertions.assertEquals(expected, actual);
        Assertions.assertNotNull(actual);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("Test updateById method with invalid booking id")
    void updateById_InvalidId_ShouldThrowEntityNotFoundException() {
        Long bookingId = -1L;
        UpdateBookingRequestDto requestDto = new UpdateBookingRequestDto(UPDATE_BOOKING_CHECK_IN,
                UPDATE_BOOKING_CHECK_OUT, Booking.Status.PENDING.name());

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, ()
                -> bookingService.updateById(bookingId, requestDto));
        verify(bookingRepository, times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingRepository, bookingMapper);
    }

    @Test
    @DisplayName("Verify method delete was invoked")
    void deleteById_ValidBookingId_Success() {
        Long bookingId = 1L;
        bookingService.deleteById(bookingId);
        verify(bookingRepository, times(1)).deleteById(bookingId);
        doAnswer(invocation -> {
            invocation.getArgument(0);
            return null;
        }).when(notificationService).notifyCancellationAccommodation(anyLong());

        notificationService.notifyCancellationAccommodation(bookingId);
        verifyNoMoreInteractions(bookingRepository);
    }

    private BookingResponseDto createBookingResponseDto() {
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setStatus(Booking.Status.PENDING.name());
        bookingResponseDto.setAccommodationId(ACCOMMODATION_ID);
        bookingResponseDto.setUserId(USER_ID);
        bookingResponseDto.setCheckInDate(BOOKING_CHECK_IN);
        bookingResponseDto.setCheckOutDate(BOOKING_CHECK_OUT);
        return bookingResponseDto;
    }

    private Booking createBooking() {
        User user = new User();
        user.setId(USER_ID);
        Accommodation accommodation = createAccommodation1();
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.Status.PENDING);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(BOOKING_CHECK_IN);
        booking.setCheckOutDate(BOOKING_CHECK_OUT);
        return booking;
    }

    private CreateBookingRequestDto createBookingRequestDto() {
        CreateBookingRequestDto createBookingRequestDto = new CreateBookingRequestDto();
        createBookingRequestDto.setAccommodationId(ACCOMMODATION_ID);
        createBookingRequestDto.setCheckInDate(BOOKING_CHECK_IN);
        createBookingRequestDto.setCheckOutDate(BOOKING_CHECK_OUT);
        return createBookingRequestDto;
    }

    private Accommodation createAccommodation1() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(ACCOMMODATION_ID);
        accommodation.setAddress(createAddress());
        accommodation.setAmenities(List.of("Bath", "Big kitchen", "Wi-fi"));
        accommodation.setType(Accommodation.Type.APARTMENT);
        accommodation.setDailyRate(BigDecimal.valueOf(100));
        accommodation.setSize("Small");
        accommodation.setAvailability(2);
        return accommodation;
    }

    private Accommodation createAccommodation2() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(2L);
        accommodation.setAddress(createAddress());
        accommodation.setAmenities(List.of("Pool"));
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setDailyRate(BigDecimal.valueOf(150));
        accommodation.setSize("Large");
        accommodation.setAvailability(1);
        return accommodation;
    }

    private Address createAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setRegion("New York");
        address.setCity("New York");
        address.setStreet("John Glen Blvd, 10");
        return address;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("bob@gmail.com");
        user.setPassword("qwerty12345");
        user.setFirstName("Bob");
        user.setLastName("Smith");
        Role role = new Role();
        role.setRoleName(Role.RoleName.ROLE_USER);
        user.setRoles(Set.of(role));
        user.setDeleted(false);
        return user;
    }
}
