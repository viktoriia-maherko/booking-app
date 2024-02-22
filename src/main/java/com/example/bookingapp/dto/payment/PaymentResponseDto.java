package com.example.bookingapp.dto.payment;

import com.example.bookingapp.model.Payment;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long id;
    private Payment.Status status;
    private Long bookingId;
    private BigDecimal amountToPay;
    private String paymentUrl;
}
