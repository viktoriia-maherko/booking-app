package com.example.bookingapp.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.PaymentMapper;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Payment;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PaymentRepository;
import com.example.bookingapp.service.impl.PaymentServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    private static final Long USER_ID = 1L;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Verify list of paymentResponseDto was returned by valid userId")
    void getById_ShouldGetPaymentInformationByUserId() {
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();

        when(paymentRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(payment1, payment2));
        when(paymentMapper.toDto(payment1)).thenReturn(new PaymentResponseDto());
        when(paymentMapper.toDto(payment2)).thenReturn(new PaymentResponseDto());

        List<PaymentResponseDto> paymentInformation = paymentService
                .getPaymentInformationByUserId(USER_ID);

        Assertions.assertEquals(2, paymentInformation.size());
    }

    @Test
    @DisplayName("Verify successful payment process")
    void success_ShouldSuccessPayment() {
        String sessionId = "example_session_id";
        Payment payment = new Payment();
        Booking booking = new Booking();
        booking.setStatus(Booking.Status.PENDING);
        payment.setBooking(booking);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(bookingRepository.save(booking)).thenReturn(booking);

        paymentService.success(sessionId);

        Assertions.assertEquals(Payment.Status.PAID, payment.getStatus());
        Assertions.assertEquals(Booking.Status.CONFIRMED, booking.getStatus());
        verify(notificationService).notifySuccessfulPayment(payment.getId());
    }

    @Test
    @DisplayName("Verify payment cancellation")
    void cancel_ShouldCancelPayment() {
        String sessionId = "example_session_id";
        Payment payment = new Payment();
        Booking booking = new Booking();
        booking.setStatus(Booking.Status.PENDING);
        payment.setBooking(booking);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(bookingRepository.save(booking)).thenReturn(booking);

        paymentService.cancel(sessionId);

        Assertions.assertEquals(Payment.Status.CANCELED, payment.getStatus());
        Assertions.assertEquals(Booking.Status.CANCELED, booking.getStatus());
    }

    @Test
    @DisplayName("Verify the exception was thrown when sessionId was incorrect")
    void success_ShouldThrowEntityNotFoundException_WhenPaymentNotFound() {
        String sessionId = "nonexistent_session_id";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, ()
                -> paymentService.success(sessionId));
    }

    @Test
    @DisplayName("Verify the exception was thrown when booking for payment is not found")
    void success_ShouldThrowEntityNotFoundException_WhenBookingNotFoundForPayment() {
        String sessionId = "example_session_id";
        Payment payment = new Payment();

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        Assertions.assertThrows(EntityNotFoundException.class, ()
                -> paymentService.success(sessionId));
    }
}
