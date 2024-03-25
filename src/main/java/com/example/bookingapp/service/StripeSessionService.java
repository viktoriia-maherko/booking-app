package com.example.bookingapp.service;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.stripe.model.checkout.Session;

public interface StripeSessionService {
    Session createSession(PaymentRequestDto requestDto);
}
