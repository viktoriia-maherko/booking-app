package com.example.bookingapp.service.impl;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.example.bookingapp.service.StripeSessionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class StripeSessionServiceImpl implements StripeSessionService {
    private static final String USD = "usd";
    private static final Long QUANTITY = 1L;
    private static final Long PRICE_IN_CENTS = 100L;
    @Value("${stripe.domain.url}")
    private String domainUrl;
    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public Session createSession(PaymentRequestDto requestDto) {
        String successUrl = UriComponentsBuilder.fromHttpUrl(domainUrl)
                .path("/success")
                .build()
                .toUriString();

        String cancelUrl = UriComponentsBuilder.fromHttpUrl(domainUrl)
                .path("/cancel")
                .build()
                .toUriString();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(QUANTITY)
                                .setPriceData(calculateTotalPrice(requestDto.getAmountToPay()))
                                .build())
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
}
