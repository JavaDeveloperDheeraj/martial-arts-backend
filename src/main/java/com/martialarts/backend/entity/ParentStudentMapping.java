package com.martialarts.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "parent_student_mapping")
@Data
public class ParentStudentMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parentMobile; // Foreign key to AuthorizedUser.mobile

    private Long studentId; // Foreign key to Student.id

    private LocalDateTime createdAt = LocalDateTime.now();

    // relationship fields (optional, for easier querying)
    private String studentName;
    private String studentStatus;
}