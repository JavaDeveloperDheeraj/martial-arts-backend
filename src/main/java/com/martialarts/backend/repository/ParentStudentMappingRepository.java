package com.martialarts.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.martialarts.backend.entity.ParentStudentMapping;

public interface ParentStudentMappingRepository extends JpaRepository<ParentStudentMapping, Long> {
    List<ParentStudentMapping> findByParentMobile(String parentMobile);
    List<ParentStudentMapping> findByStudentId(Long studentId);
    boolean existsByParentMobileAndStudentId(String parentMobile, Long studentId);
    void deleteByStudentId(Long studentId);
}