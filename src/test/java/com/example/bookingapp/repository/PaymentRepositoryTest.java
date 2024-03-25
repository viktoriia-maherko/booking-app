package com.example.bookingapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.bookingapp.model.Payment;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Find all payments by user ID")
    @Sql(scripts = {
            "classpath:database/payments/add-payments-to-payments-table.sql",
            "classpath:database/users/add-user-bob-to-users-table.sql",
            "classpath:database/accommodations/add-accommodations"
                    + "-to-accommodations-table.sql",
            "classpath:database/bookings/add-bookings"
                    + "-to-bookings-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations-from-accommodations-table.sql",
            "classpath:database/bookings/remove-bookings-from-bookings-table.sql",
            "classpath:database/payments/remove-payments-from-payments-table.sql",
            "classpath:database/users/remove-user-bob-from-users-table.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findAllByUserId_ValidUserId_ReturnsListOfPayments()
            throws MalformedURLException {
        List<Payment> actual = paymentRepository.findAllByUserId(1L);
        assertEquals(1L, actual.size());
        assertEquals(new URL("https://example.com/session1"),
                actual.get(0).getSessionUrl());
    }

    @Test
    @DisplayName("Find payment by session ID when payment does not exist")
    @Sql(scripts = {
            "classpath:database/payments/add-payments-to-payments-table.sql",
            "classpath:database/users/add-user-bob-to-users-table.sql",
            "classpath:database/accommodations/add-accommodations"
                    + "-to-accommodations-table.sql",
            "classpath:database/bookings/add-bookings"
                    + "-to-bookings-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations-from-accommodations-table.sql",
            "classpath:database/bookings/remove-bookings-from-bookings-table.sql",
            "classpath:database/payments/remove-payments-from-payments-table.sql",
            "classpath:database/users/remove-user-bob-from-users-table.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findByUserId_InvalidUserId_ReturnsEmptyList() {
        List<Payment> payment = paymentRepository.findAllByUserId(-1L);
        assertTrue(payment.isEmpty());
    }

    @Test
    @DisplayName("Find payment by session ID")
    @Sql(scripts = {
            "classpath:database/payments/add-payments-to-payments-table.sql",
            "classpath:database/users/add-user-bob-to-users-table.sql",
            "classpath:database/accommodations/add-accommodations"
                    + "-to-accommodations-table.sql",
            "classpath:database/bookings/add-bookings"
                    + "-to-bookings-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations-from-accommodations-table.sql",
            "classpath:database/bookings/remove-bookings-from-bookings-table.sql",
            "classpath:database/payments/remove-payments-from-payments-table.sql",
            "classpath:database/users/remove-user-bob-from-users-table.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findBySessionId_ValidSessionId_ReturnsOptionalOfPayment() throws MalformedURLException {
        Payment actual = paymentRepository.findBySessionId("session1").orElseThrow();
        assertNotNull(actual);
        assertEquals(new URL("https://example.com/session1"), actual.getSessionUrl());
        assertEquals(Payment.Status.PENDING, actual.getStatus());
        assertEquals(BigDecimal.valueOf(100).intValue(), actual.getAmountToPay().intValue());
    }

    @Test
    @DisplayName("Find payment by session ID when payment does not exist")
    @Sql(scripts = {
            "classpath:database/payments/add-payments-to-payments-table.sql",
            "classpath:database/users/add-user-bob-to-users-table.sql",
            "classpath:database/accommodations/add-accommodations"
                    + "-to-accommodations-table.sql",
            "classpath:database/bookings/add-bookings"
                    + "-to-bookings-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts = {
            "classpath:database/accommodations/remove-accommodations-from-accommodations-table.sql",
            "classpath:database/bookings/remove-bookings-from-bookings-table.sql",
            "classpath:database/payments/remove-payments-from-payments-table.sql",
            "classpath:database/users/remove-user-bob-from-users-table.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findBySessionId_InvalidSessionId_ReturnsEmptyOptional() {
        Optional<Payment> payment = paymentRepository.findBySessionId("nonexistent_session_id");
        assertTrue(payment.isEmpty());
    }
}
