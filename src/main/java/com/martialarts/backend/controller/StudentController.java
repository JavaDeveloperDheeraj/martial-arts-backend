package com.martialarts.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import com.martialarts.backend.entity.Student;
import com.martialarts.backend.repository.StudentRepository;
import com.martialarts.backend.service.OtpService;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentRepository repo;
    
    @Autowired
    private OtpService otpService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("userOtp") String userOtp,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("signature") MultipartFile signature,
            @ModelAttribute Student student) {
        
        try {
            boolean isOtpValid = otpService.validateOtp(student.getMobile(), userOtp);
            if (!isOtpValid) {
                return ResponseEntity.badRequest().body("Invalid or Expired OTP!");
            }

            if (repo.findByNameAndMobile(student.getName(), student.getMobile()).isPresent()) {
                return ResponseEntity.badRequest().body("Student already registered!");
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String safeName = student.getName().replaceAll("\\s+", "_");
            String fileNameBase = safeName + "_" + student.getMobile() + "_" + timeStamp;

            String rootPath = "C:/karate_uploads/";
            String photoDir = rootPath + "photos/";
            String signDir = rootPath + "signatures/";

            new File(photoDir).mkdirs();
            new File(signDir).mkdirs();

            String photoPath = photoDir + fileNameBase + "_photo.jpg";
            String signPath = signDir + fileNameBase + "_sign.png";

            photo.transferTo(new File(photoPath));
            signature.transferTo(new File(signPath));

            student.setPhotoPath(photoPath);
            student.setSignaturePath(signPath);
            student.setStatus("PENDING");
            student.setCreatedAt(LocalDateTime.now());
            student.setUpdatedAt(null);
            
            repo.save(student);

            return ResponseEntity.ok("Registration successful! Please wait for admin approval.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration Error: " + e.getMessage());
        }
    }
    
 // Update the login method in StudentController.java
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String mobile, @RequestParam String otp) {
        
        // Verify OTP
        if (!otpService.validateOtp(mobile, otp)) {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP!");
        }

        Optional<Student> student = repo.findByMobile(mobile);

        if (student.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (!"APPROVED".equals(student.get().getStatus())) {  // Changed from "ACTIVE" to "APPROVED"
            return ResponseEntity.badRequest().body("Not approved yet");
        }

        return ResponseEntity.ok(student.get());
    }
    
 // Add this method to StudentController.java
    @GetMapping("/details/{id}")
    public ResponseEntity<?> getStudentDetails(@PathVariable Long id) {
        Optional<Student> student = repo.findById(id);
        if (student.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }
        return ResponseEntity.ok(student.get());
    }
    
    
}