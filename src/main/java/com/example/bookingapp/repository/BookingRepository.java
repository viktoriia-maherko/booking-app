package com.example.bookingapp.repository;

import com.example.bookingapp.model.Booking;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {
    Page<Booking> findByUserIdAndStatus(Long userId, Booking.Status status, Pageable pageable);

    @Query("SELECT b "
            + "FROM Booking b "
            + "WHERE (b.checkInDate BETWEEN :checkInDate AND :checkOutDate "
            + "OR b.checkOutDate BETWEEN :checkInDate AND :checkOutDate) "
            + "AND b.accommodation.id = :accommodationId")
    List<Booking> findAllBetweenCheckInDateAndCheckOutDate(
            LocalDate checkInDate, LocalDate checkOutDate, Long accommodationId
    );

    @Query("FROM Booking b WHERE b.user.id = :id")
    List<Booking> findAllByUserId(Long id, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.checkOutDate <= :tomorrow")
    List<Booking> findExpiredBookings(@Param("tomorrow") LocalDate tomorrow);
}
