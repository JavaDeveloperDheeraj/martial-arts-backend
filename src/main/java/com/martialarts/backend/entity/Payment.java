package com.martialarts.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Double amount;

    private String paymentMode; // CASH / ONLINE
    private String transactionId; // optional

    @Column(name = "payment_date")
    private LocalDateTime paymentDate = LocalDateTime.now();
    
    private String screenshotPath;


    @Column(length = 50)
    private String status = "PENDING"; 
    private String rejectionReason;
    

    private String collectedBy; // "ADMIN_CASH", "ADMIN_UPI", "STUDENT_ONLINE"
    
    private String remarks;
}