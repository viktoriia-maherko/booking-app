package com.example.bookingapp.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @Positive
    @NotNull
    private Long bookingId;
    @Positive
    @NotNull
    private BigDecimal amountToPay;
}
