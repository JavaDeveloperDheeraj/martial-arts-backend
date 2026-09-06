package com.martialarts.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.martialarts.backend.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	
	
	List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);
    List<Payment> findByStatusOrderByPaymentDateDesc(String status);
    
    
}