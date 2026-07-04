package com.martialarts.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.martialarts.backend.entity.FeeDue;

public interface FeeDueRepository extends JpaRepository<FeeDue, Long> {

    @Query("""
        SELECT f FROM FeeDue f
        WHERE f.studentId = :studentId
        AND (f.totalAmount + f.lateFee - f.paidAmount) > 0
        ORDER BY 
        CASE WHEN f.feeType = 'ADMISSION' THEN 0 ELSE 1 END,
        f.year ASC NULLS FIRST,
        f.month ASC NULLS FIRST
    """)
    List<FeeDue> findPending(Long studentId);

    List<FeeDue> findByStudentIdOrderByYearAscMonthAsc(Long studentId);
}