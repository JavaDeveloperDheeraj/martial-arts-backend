package com.martialarts.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.martialarts.backend.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByMobile(String mobile);

    Optional<Student> findByNameAndMobile(String name, String mobile);

    List<Student> findByStatus(String status);
}   