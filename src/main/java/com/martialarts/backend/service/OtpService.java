package com.martialarts.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.martialarts.backend.repository.AuthorizedUserRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private AuthorizedUserRepository authUserRepo;

    // Helper class to store OTP and Expiry
    private static class OtpDetails {
        String otp;
        long expiryTime;
        OtpDetails(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<String, OtpDetails> otpStore = new ConcurrentHashMap<>();

    // Generate and Store OTP (3 Minutes Expiry)
    public String generateOtp(String mobile) {
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        long expiry = System.currentTimeMillis() + (3 * 60 * 1000); // 3 mins
        otpStore.put(mobile, new OtpDetails(otp, expiry));
        return otp;
    }
    
    // Check if mobile is authorized for login
    public boolean isAuthorizedMobile(String mobile) {
        return authUserRepo.existsByMobile(mobile);
    }

    // Validate OTP
    public boolean validateOtp(String mobile, String userOtp) {
        OtpDetails stored = otpStore.get(mobile);
        if (stored == null) return false;

        // Check Expiry
        if (System.currentTimeMillis() > stored.expiryTime) {
            otpStore.remove(mobile);
            return false;
        }

        // Match OTP
        boolean isValid = stored.otp.equals(userOtp);
        if (isValid) {
            otpStore.remove(mobile); // Ek baar verify hone par delete kar do
        }
        return isValid;
    }
}