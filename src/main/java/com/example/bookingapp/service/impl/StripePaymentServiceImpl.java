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
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class StripePaymentServiceImpl implements StripePaymentService {
    private static final String USD = "usd";
    private static final Long QUANTITY = 1L;
    private static final Long PRICE_IN_CENTS = 100L;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    @Value("${stripe.domain.url}")
    private String domainUrl;
    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public PaymentResponseDto createPaymentSession(PaymentRequestDto requestDto)
            throws MalformedURLException {
        Session session = createSession(requestDto);
        Payment payment = savePayment(requestDto, session);
        return paymentMapper.toDto(payment);
    }

    private Session createSession(PaymentRequestDto requestDto) {
        String successUrl = UriComponentsBuilder.fromHttpUrl(domainUrl)
                .path("/success")
                .build()
                .toUriString();

        String cancelUrl = UriComponentsBuilder.fromHttpUrl(domainUrl)
                .path("/cancel")
                .build()
                .toUriString();
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(QUANTITY)
                                .setPriceData(calculateTotalPrice(requestDto.getAmountToPay())
                                )
                                .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .build();
        try {
            return Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Can't create payment session", e);
        }
    }

    private SessionCreateParams.LineItem.PriceData calculateTotalPrice(BigDecimal totalPrice) {
        return SessionCreateParams.LineItem.PriceData.builder()
                 .setCurrency(USD)
                 .setUnitAmount(PRICE_IN_CENTS * totalPrice.longValue())
                 .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Booking")
                                .build())
                 .build();
    }

    private Payment savePayment(PaymentRequestDto requestDto, Session session)
            throws MalformedURLException {
        Booking booking = bookingRepository.findById(requestDto.getBookingId()).orElseThrow(()
                -> new EntityNotFoundException("Can't find booking by id "
                + requestDto.getBookingId()));
        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PENDING);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(new URL(session.getUrl()));
        payment.setBooking(booking);
        payment.setAmountToPay(requestDto.getAmountToPay());
        return paymentRepository.save(payment);
    }
}
