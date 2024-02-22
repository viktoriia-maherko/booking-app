package com.example.bookingapp.service;

import com.example.bookingapp.dto.payment.PaymentResponseDto;
import java.util.List;

public interface PaymentService {
    List<PaymentResponseDto> getPaymentInformationByUserId(Long id);

    void success(String sessionId);

    void cancel(String sessionId);
}
