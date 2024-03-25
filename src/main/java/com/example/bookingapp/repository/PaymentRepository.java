package com.example.bookingapp.repository;

import com.example.bookingapp.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment> {

    @Query("FROM Payment p JOIN FETCH p.booking b "
            + "JOIN FETCH b.user u "
            + "WHERE u.id = :userId")
    List<Payment> findAllByUserId(Long userId);

    @Query("SELECT p FROM Payment p WHERE p.sessionId = :sessionId")
    Optional<Payment> findBySessionId(String sessionId);
}
