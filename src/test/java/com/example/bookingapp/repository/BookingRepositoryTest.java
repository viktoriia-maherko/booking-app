package com.example.bookingapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.bookingapp.model.Booking;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("""
            Find all bookings between check-in and check-out date
            """)
    @Sql(scripts = {
            "classpath:database/bookings/add-bookings"
                    + "-to-bookings-table.sql",
            "classpath:database/accommodations/add-accommodations-to-accommodations-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts =
            "classpath:database/accommodations/remove-accommodations-from-accommodations-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(scripts =
            "classpath:database/bookings/remove-bookings-from-bookings-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findAllBetweenCheckInDateAndCheckOutDate_ValidDate_ReturnsABooking() {
        List<Booking> actual = bookingRepository
                .findAllBetweenCheckInDateAndCheckOutDate(LocalDate.of(2024, 3, 18),
                LocalDate.of(2024, 3, 25), 1L);
        assertEquals(1, actual.size());
    }

    @Test
    @DisplayName("""
            Find all bookings by user id
            """)
    @Sql(scripts = {
            "classpath:database/bookings/add-bookings"
                    + "-to-bookings-table.sql",
            "classpath:database/accommodations/add-accommodations-to-accommodations-table.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(scripts =
            "classpath:database/accommodations/remove-accommodations-from-accommodations-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Sql(scripts =
            "classpath:database/bookings/remove-bookings-from-bookings-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findAllByUserId_ValidUserId_ReturnsABooking() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> actual = bookingRepository.findAllByUserId(1L, pageable);
        assertEquals(1, actual.size());
        assertEquals(LocalDate.of(2024, 3, 18), actual.get(0).getCheckInDate());
    }
}
