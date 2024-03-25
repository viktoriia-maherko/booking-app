package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.PaymentMapper;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Payment;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PaymentRepository;
import com.example.bookingapp.service.NotificationService;
import com.example.bookingapp.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Override
    public List<PaymentResponseDto> getPaymentInformationByUserId(Long id) {
        return paymentRepository.findAllByUserId(id)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public void success(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find payment by session id "
                        + sessionId));
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
        Booking booking = payment.getBooking();
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found for payment with session id "
                    + sessionId);
        }
        booking.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(booking);
        notificationService.notifySuccessfulPayment(payment.getId());
    }

    @Override
    public void cancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find payment by session id "
                        + sessionId));
        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);
        Booking booking = payment.getBooking();
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found for payment with session id "
                    + sessionId);
        }
        booking.setStatus(Booking.Status.CANCELED);
        bookingRepository.save(booking);
    }
}
