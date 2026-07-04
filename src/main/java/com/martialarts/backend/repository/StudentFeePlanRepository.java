package com.martialarts.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.martialarts.backend.entity.StudentFeePlan;

public interface StudentFeePlanRepository
extends JpaRepository<StudentFeePlan, Long> {

List<StudentFeePlan> findByStudentIdOrderByEffectiveFromDesc(Long studentId);
StudentFeePlan findFirstByStudentIdOrderByEffectiveFromDesc(Long studentId);

}