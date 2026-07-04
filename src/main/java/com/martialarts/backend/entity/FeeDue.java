package com.martialarts.backend.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.martialarts.backend.enums.FeeType;
@Entity
@Table(name = "fee_due")
@Data
public class FeeDue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @Enumerated(EnumType.STRING)
    private FeeType feeType;

    private Integer month;
    private Integer year;

    private Double totalAmount;
    private Double paidAmount = 0.0;

    private Double lateFee = 0.0;
    private LocalDate dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}