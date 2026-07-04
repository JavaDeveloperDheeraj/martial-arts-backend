package com.martialarts.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "student")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name, fatherName, dob, gender, qualification, schoolName, occupation;
    private String currentAddress, permanentAddress, mobile, email;
    private String emergencyContactName, emergencyContactPhone, emergencyRelation;
    private String weight, bloodGroup, alignments;
    private String photoPath, signaturePath;

    private String status; // PENDING / APPROVED / REJECTED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}