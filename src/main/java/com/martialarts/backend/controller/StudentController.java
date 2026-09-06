package com.martialarts.backend.controller;

import com.martialarts.backend.entity.Student;
import com.martialarts.backend.repository.StudentRepository;
import com.martialarts.backend.service.FileStorageService;
import com.martialarts.backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentRepository repo;

    @Autowired
    private OtpService otpService;
    
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("userOtp") String userOtp,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("signature") MultipartFile signature,
            @ModelAttribute Student student) {

        try {

            // ========================================================
            // 🛡️ 1. BACKEND OTP VALIDATION
            // ========================================================
            boolean isOtpValid = otpService.validateOtp(
                    student.getMobile(),
                    userOtp
            );

            if (!isOtpValid) {
                return ResponseEntity.badRequest()
                        .body("Security Alert: Invalid or Expired OTP!");
            }


            // ========================================================
            // 🛡️ 2. MANDATORY FIELDS VALIDATION
            // ========================================================
            if (student.getName() == null ||
                student.getName().trim().isEmpty() ||

                student.getMobile() == null ||
                !student.getMobile().matches("^[0-9]{10}$") ||

                student.getFatherName() == null ||
                student.getFatherName().trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("Mandatory fields (Name, Mobile, Father Name) are missing or invalid!");
            }


            // ========================================================
            // 🛡️ 3. DUPLICATE RECORD CHECK
            // ========================================================
            if (repo.findByNameAndMobile(
                    student.getName().trim(),
                    student.getMobile().trim()
            ).isPresent()) {

                return ResponseEntity.badRequest().body(
                        "Student '" +
                        student.getName() +
                        "' is already registered with mobile number " +
                        student.getMobile() +
                        "!"
                );
            }


            // ========================================================
            // 🛡️ 4. FILE CHECKS
            // ========================================================
            if (photo == null || photo.isEmpty() ||
                signature == null || signature.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("Both Photo and Signature files are mandatory!");
            }


            // ========================================================
            // 📁 5. FILE VALIDATION & STORAGE
            // ========================================================
            try {

                // -----------------------------
                // 📸 PHOTO
                // -----------------------------
                fileStorageService.validateFile(photo, "IMAGE");

                String photoPath =
                        fileStorageService.storeFile(photo, "photo");

                student.setPhotoPath(photoPath);


                // -----------------------------
                // ✍️ SIGNATURE
                // -----------------------------
                fileStorageService.validateFile(signature, "IMAGE");

                String signPath =
                        fileStorageService.storeFile(signature, "sign");

                student.setSignaturePath(signPath);

            } catch (RuntimeException e) {

                // File validation/storage error
                return ResponseEntity.badRequest()
                        .body(e.getMessage());
            }


            // ========================================================
            // 💾 6. ENTITY SETUP & SAVE
            // ========================================================
            student.setStatus("PENDING");
            student.setCreatedAt(LocalDateTime.now());
            student.setUpdatedAt(null);

            repo.save(student);


            // ========================================================
            // ✅ 7. SUCCESS RESPONSE
            // ========================================================
            return ResponseEntity.ok(
                    "Registration successful! Application submitted for Admin Approval."
            );


        } catch (Exception e) {

            // Backend unexpected error
            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body("Registration Failed: " + e.getMessage());
        }
    }

    
    
 // StudentController.java के अंदर:

    @GetMapping("/details/{id}")
    public ResponseEntity<?> getStudentDetails(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🎯 सेफ़्टी के लिए सीधे ID वाला एंडपॉइंट भी सपोर्ट रखें
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}