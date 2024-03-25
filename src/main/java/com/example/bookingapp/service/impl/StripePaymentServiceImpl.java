package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.exception.EntityNotFoundException;
import com.example.bookingapp.mapper.PaymentMapper;
import com.example.bookingapp.model.Booking;
import com.example.bookingapp.model.Payment;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PaymentRepository;
import com.example.bookingapp.service.StripePaymentService;
import com.example.bookingapp.service.StripeSessionService;
import com.stripe.model.checkout.Session;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripePaymentServiceImpl implements StripePaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripeSessionService stripeSessionService;

    @Override
    public PaymentResponseDto createPaymentSession(PaymentRequestDto requestDto)
            throws MalformedURLException {
        Session session = stripeSessionService.createSession(requestDto);
        Payment payment = savePayment(requestDto, session);
        return paymentMapper.toDto(payment);
    }

    private Payment savePayment(PaymentRequestDto requestDto, Session session)
            throws MalformedURLException {
        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find booking by id "
                        + requestDto.getBookingId())
        );
        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PENDING);
        payment.setBooking(booking);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(new URL(session.getUrl()));
        payment.setAmountToPay(requestDto.getAmountToPay());
        paymentRepository.save(payment);
        return payment;
    }
}
