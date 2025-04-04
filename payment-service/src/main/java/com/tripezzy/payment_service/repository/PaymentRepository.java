package com.tripezzy.payment_service.repository;

import com.tripezzy.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySession(String sessionId);

    List<Payment> findByUser(Long userId);
}
