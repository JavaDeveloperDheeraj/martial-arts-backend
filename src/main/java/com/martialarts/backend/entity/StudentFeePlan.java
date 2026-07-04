package com.martialarts.backend.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "student_fee_plan")
@Data
public class StudentFeePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Double monthlyFee;
    private Double admissionFee;

    private LocalDate effectiveFrom;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}