package com.martialarts.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.martialarts.backend.entity.AuthorizedUser;
import com.martialarts.backend.entity.ParentStudentMapping;
import com.martialarts.backend.repository.AuthorizedUserRepository;
import com.martialarts.backend.repository.ParentStudentMappingRepository;
import com.martialarts.backend.repository.StudentRepository;
import com.martialarts.backend.service.OtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;
    
    @Autowired
    private AuthorizedUserRepository authUserRepo;
    
    @Autowired
    private ParentStudentMappingRepository mappingRepo;
    
    @Autowired
    private StudentRepository studentRepo;
    
    @GetMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String mobile) {
        
        // Check if mobile exists in authorized_user table
        AuthorizedUser user = authUserRepo.findByMobile(mobile).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Mobile not registered. Please contact admin.");
        }
        
        // Check if user is active
        if (!user.isActive()) {
            return ResponseEntity.badRequest().body("Account is deactivated. Contact admin.");
        }
        
        String otp = otpService.generateOtp(mobile);
        System.err.println("===== OTP for " + mobile + " (" + user.getRole() + ") is: " + otp + " =====");
        return ResponseEntity.ok("OTP sent successfully");
    }
    
    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestBody Map<String, String> request) {
        
        String mobile = request.get("mobile");
        String otp = request.get("otp");
        
        if (!otpService.validateOtp(mobile, otp)) {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
        
        // Get user from database
        AuthorizedUser user = authUserRepo.findByMobile(mobile).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("role", user.getRole());
        response.put("name", user.getName());
        response.put("mobile", mobile);
        
        // If PARENT, also fetch their students
        if ("PARENT".equals(user.getRole())) {
            List<ParentStudentMapping> mappings = mappingRepo.findByParentMobile(mobile);
            
            List<Map<String, Object>> students = mappings.stream()
                .filter(m -> "APPROVED".equals(m.getStudentStatus()))
                .map(m -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", m.getStudentId());
                    studentMap.put("name", m.getStudentName());
                    studentMap.put("status", m.getStudentStatus());
                    
                    // Get additional student details
                    studentRepo.findById(m.getStudentId()).ifPresent(s -> {
                        studentMap.put("photoPath", s.getPhotoPath());
                        studentMap.put("fatherName", s.getFatherName());
                    });
                    return studentMap;
                }).collect(Collectors.toList());
            
            response.put("students", students);
        }
        
        return ResponseEntity.ok(response);
    }
}