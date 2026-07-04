package com.martialarts.backend.controller;

import com.martialarts.backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @GetMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestParam String mobile, @RequestParam String role) {
        
        // For PARENT/STAFF login, check if mobile is authorized
        if ("PARENT".equals(role) || "STAFF".equals(role)) {
            if (!otpService.isAuthorizedMobile(mobile)) {
                return ResponseEntity.badRequest().body("Unregistered mobile number. Please contact admin to register.");
            }
        }
        
        // For STUDENT login, check if student exists and is approved
        if ("STUDENT".equals(role)) {
            // Student validation will be done in student login controller
            // Just generate OTP for now
        }
        
        String otp = otpService.generateOtp(mobile);
        System.err.println("****** OTP for " + mobile + " is: " + otp + " (Valid for 3 mins) ******");
        return ResponseEntity.ok("OTP Sent successfully");
    }
}