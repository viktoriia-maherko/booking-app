package com.example.bookingapp.controller;

import com.example.bookingapp.dto.payment.PaymentRequestDto;
import com.example.bookingapp.dto.payment.PaymentResponseDto;
import com.example.bookingapp.service.PaymentService;
import com.example.bookingapp.service.StripePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.MalformedURLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for managing payments")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final StripePaymentService stripePaymentService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{userId}")
    @Operation(summary = "Get payment information",
            description = "Get a list of payment information for user")
    public List<PaymentResponseDto> getPaymentInformationByUserId(@PathVariable Long userId) {
        return paymentService.getPaymentInformationByUserId(userId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    @Operation(summary = "Initiate payment session",
            description = "Initiate payment session for booking transaction")
    public PaymentResponseDto initiatePaymentSession(
            @RequestBody @Valid PaymentRequestDto requestDto
    )
            throws MalformedURLException {
        return stripePaymentService.createPaymentSession(requestDto);
    }

    @PreAuthorize("hasAnyRole()('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping(value = "/success")
    @Operation(summary = "Handles successful payment",
            description = "Handles successful payment processing through Stripe redirection")
    public String success(String sessionId) {
        paymentService.success(sessionId);
        return "The payment was successful. Session ID: " + sessionId;
    }

    @PreAuthorize("hasAnyRole()('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping(value = "/cancel")
    @Operation(summary = " Manages payment cancellation",
            description = "Manages payment cancellation and returns "
                    + "payment paused messages during Stripe redirection")
    public String cancel(String sessionId) {
        paymentService.cancel(sessionId);
        return "Payment cancelled. You can try again later. "
                + "Session available for 24 hours. Session ID: " + sessionId;
    }
}
