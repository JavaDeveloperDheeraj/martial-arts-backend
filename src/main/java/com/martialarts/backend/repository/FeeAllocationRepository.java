package com.martialarts.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.martialarts.backend.entity.FeeAllocation;

public interface FeeAllocationRepository extends JpaRepository<FeeAllocation, Long> {
}