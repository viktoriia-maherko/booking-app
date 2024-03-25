package com.example.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.PaymentMapper;
import com.example.bookingapp.model.Accommodation;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Payment;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PaymentRepository;
import com.example.bookingapp.service.impl.StripePaymentServiceImpl;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceImplTest {
    private static final LocalDate BOOKING_CHECK_IN = LocalDate
            .of(2024, 3, 18);
    private static final LocalDate BOOKING_CHECK_OUT = LocalDate
            .of(2024, 3, 25);
    private static final Long BOOKING_ID = 1L;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private StripeSessionService stripeSessionService;
    @InjectMocks
    private StripePaymentServiceImpl stripePaymentService;

    @Test
    void createPaymentSession_ShouldReturnPaymentResponseDto() throws MalformedURLException {
        PaymentRequestDto requestDto = createRequestDto();
        Booking booking = createBooking();
        Payment payment = createPayment();
        PaymentResponseDto expected = createPaymentResponseDto();
        Session session = mock(Session.class);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        doReturn(payment).when(paymentRepository).save(any(Payment.class));
        when(session.getUrl()).thenReturn(payment.getSessionUrl().toString());
        when(stripeSessionService.createSession(requestDto)).thenReturn(session);
        doReturn(expected).when(paymentMapper).toDto(any(Payment.class));

        PaymentResponseDto actual = stripePaymentService.createPaymentSession(requestDto);

        assertNotNull(actual);
        assertNotNull(actual.getSessionUrl());
        assertEquals(expected.getSessionUrl(), actual.getSessionUrl());
        assertEquals(expected.getSessionId(), actual.getSessionId());
    }

    @Test
    void createPaymentSession_ShouldThrowEntityNotFoundException() {
        when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(1L);
        requestDto.setAmountToPay(BigDecimal.valueOf(100));
        assertThrows(EntityNotFoundException.class, ()
                -> stripePaymentService.createPaymentSession(requestDto));
    }

    private Booking createBooking() {
        User user = new User();
        user.setId(1L);
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setStatus(Booking.Status.PENDING);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(BOOKING_CHECK_IN);
        booking.setCheckOutDate(BOOKING_CHECK_OUT);
        return booking;
    }

    private PaymentRequestDto createRequestDto() {
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setAmountToPay(BigDecimal.valueOf(100));
        requestDto.setBookingId(BOOKING_ID);
        return requestDto;
    }

    private Payment createPayment() throws MalformedURLException {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setSessionId("session_id");
        payment.setStatus(Payment.Status.PENDING);
        payment.setAmountToPay(BigDecimal.valueOf(100));
        payment.setBooking(createBooking());
        URL sessionUrl = new URL("https://example.com");
        payment.setSessionUrl(sessionUrl);
        return payment;
    }

    private PaymentResponseDto createPaymentResponseDto() throws MalformedURLException {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(1L);
        responseDto.setSessionId("session_id");
        responseDto.setStatus(Payment.Status.PENDING);
        responseDto.setAmountToPay(BigDecimal.valueOf(100));
        responseDto.setBookingId(1L);
        URL sessionUrl = new URL("https://example.com");
        responseDto.setSessionUrl(sessionUrl.toString());
        return responseDto;
    }
}
