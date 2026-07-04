package com.martialarts.backend.controller;

import com.martialarts.backend.dto.PaymentRequestDTO;
import com.martialarts.backend.service.FeesService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fees")
public class FeesController {

    @Autowired
    private FeesService service;

    // ================= UTILITY METHOD =================
    private LocalDate parseDate(String input) {

        if (input == null || input.isEmpty()) {
            throw new RuntimeException("effectiveFrom is required");
        }

        try {
            // Format: 2026-07-01 OR 2026-7-1
            return LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-M-d"));
        } catch (Exception e1) {
            try {
                // Format: 01-07-2026
                return LocalDate.parse(input, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e2) {
                // Format: ISO datetime 2026-05-02T10:15:30
                return LocalDate.parse(input.substring(0, 10));
            }
        }
    }

    // ================= SETUP =================
    @PostMapping("/setup")
    public String setup(@RequestBody Map<String, Object> body) {

        LocalDate effectiveDate = parseDate(body.get("effectiveFrom").toString());

        service.setupFees(
                Long.valueOf(body.get("studentId").toString()),
                Double.parseDouble(body.get("monthlyFee").toString()),
                Double.parseDouble(body.get("admissionFee").toString()),
                effectiveDate
        );

        return "Setup Done";
    }

    // ================= UPDATE MONTHLY =================
    @PostMapping("/update-monthly")
    public String updateMonthly(@RequestBody Map<String, Object> body) {

        service.updateMonthlyFrom(
                Long.valueOf(body.get("studentId").toString()),
                Integer.parseInt(body.get("fromMonth").toString()),
                Integer.parseInt(body.get("year").toString()),
                Double.parseDouble(body.get("amount").toString())
        );

        return "Updated";
    }

    // ================= PAYMENT =================
    @PostMapping("/pay")
    public String pay(@RequestBody PaymentRequestDTO req) {

        if (req.getStudentId() == null ||
            req.getAmount() == null ||
            req.getMode() == null) {
            throw new RuntimeException("Missing required fields");
        }

        service.makePayment(
                req.getStudentId(),
                req.getAmount(),
                req.getMode(),
                req.getTransactionId(),
                req.getLateFee() != null ? req.getLateFee() : 0
        );

        return "Payment Successful";
    }

    // ================= SUMMARY =================
    @GetMapping("/summary/{studentId}")
    public Map<String, Object> summary(@PathVariable Long studentId) {
        return service.getSummary(studentId);
    }

    // ================= UPDATE FEE STRUCTURE =================
    @PostMapping("/update-fee-structure")
    public String updateFeeStructure(@RequestBody Map<String, Object> body) {

        Long studentId = Long.valueOf(body.get("studentId").toString());
        double newMonthlyFee = Double.parseDouble(body.get("monthlyFee").toString());

        LocalDate effectiveDate = parseDate(body.get("effectiveFrom").toString());

        service.updateMonthlyFeeWithEffectiveDate(
                studentId,
                effectiveDate,
                newMonthlyFee
        );

        return "Fee structure updated successfully from " + effectiveDate;
    }
}