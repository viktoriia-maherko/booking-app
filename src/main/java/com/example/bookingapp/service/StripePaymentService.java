package com.example.bookingapp.service;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.example.bookingapp.dto.payment.PaymentResponseDto;
import java.net.MalformedURLException;

public interface StripePaymentService {
    PaymentResponseDto createPaymentSession(PaymentRequestDto requestDto)
            throws MalformedURLException;
}
