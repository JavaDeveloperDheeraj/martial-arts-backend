package com.martialarts.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.martialarts.backend.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}