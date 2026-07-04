package com.martialarts.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martialarts.backend.entity.FeeDue;
import com.martialarts.backend.entity.Payment;
import com.martialarts.backend.entity.StudentFeePlan;
import com.martialarts.backend.enums.FeeType;
import com.martialarts.backend.repository.FeeDueRepository;
import com.martialarts.backend.repository.PaymentRepository;
import com.martialarts.backend.repository.StudentFeePlanRepository;
import com.martialarts.backend.util.DateUtil;
import java.time.LocalDate;

import jakarta.transaction.Transactional;

@Service
public class FeesService {

    @Autowired
    private FeeDueRepository feeRepo;

    @Autowired
    private PaymentRepository paymentRepo;
    
    @Autowired 
    private StudentFeePlanRepository planRepo;

    @Transactional
    public void setupFees(Long studentId,
                          double monthly,
                          double admission,
                          LocalDate effectiveFrom) {

        // 1. Save plan
        StudentFeePlan plan = new StudentFeePlan();
        plan.setStudentId(studentId);
        plan.setMonthlyFee(monthly);
        plan.setAdmissionFee(admission);
        plan.setEffectiveFrom(effectiveFrom);
        planRepo.save(plan);

        // 2. Admission (only once)
        if (admission > 0) {
            boolean exists = feeRepo.findByStudentIdOrderByYearAscMonthAsc(studentId)
                    .stream()
                    .anyMatch(f -> f.getFeeType() == FeeType.ADMISSION);

            if (!exists) {
                FeeDue ad = new FeeDue();
                ad.setStudentId(studentId);
                ad.setFeeType(FeeType.ADMISSION);
                ad.setTotalAmount(admission);
                ad.setPaidAmount(0.0);
                feeRepo.save(ad);
            }
        }

        // 3. Monthly generate (12 months)
        for (int i = 0; i < 12; i++) {

            LocalDate d = effectiveFrom.plusMonths(i);
            final LocalDate currentDate = d; // ✅ Fix: Create final variable

            boolean exists = feeRepo.findByStudentIdOrderByYearAscMonthAsc(studentId)
                    .stream()
                    .anyMatch(f ->
                            f.getFeeType() == FeeType.MONTHLY &&
                            f.getMonth() == currentDate.getMonthValue() &&
                            f.getYear() == currentDate.getYear()
                    );

            if (!exists) {
                FeeDue f = new FeeDue();
                f.setStudentId(studentId);
                f.setFeeType(FeeType.MONTHLY);
                f.setMonth(currentDate.getMonthValue());
                f.setYear(currentDate.getYear());
                f.setDueDate(currentDate);
                
                // Check if this month should have old fee or new fee
                double feeAmount = getApplicableMonthlyFee(studentId, currentDate);
                f.setTotalAmount(feeAmount);
                f.setPaidAmount(0.0);
                f.setLateFee(0.0);

                feeRepo.save(f);
            }
        }
    }
    
    // Helper method to get applicable fee for a given date
    private double getApplicableMonthlyFee(Long studentId, LocalDate forDate) {
        List<StudentFeePlan> plans = planRepo.findByStudentIdOrderByEffectiveFromDesc(studentId);
        
        if (plans.isEmpty()) {
            return 0.0;
        }
        
        // Find the latest plan that is effective on or before this date
        for (StudentFeePlan plan : plans) {
            if (!forDate.isBefore(plan.getEffectiveFrom())) {
                return plan.getMonthlyFee();
            }
        }
        
        // If no plan applies, return the oldest plan's fee
        return plans.get(plans.size() - 1).getMonthlyFee();
    }

    // UPDATE MONTHLY FEE WITH EFFECTIVE DATE - FIXED VERSION
    @Transactional
    public void updateMonthlyFeeWithEffectiveDate(Long studentId, LocalDate effectiveFrom, double newFee) {
        
        // 1. Save new plan
        StudentFeePlan newPlan = new StudentFeePlan();
        newPlan.setStudentId(studentId);
        newPlan.setMonthlyFee(newFee);
        
        // Get existing admission fee from latest plan
        StudentFeePlan latestPlan = planRepo.findFirstByStudentIdOrderByEffectiveFromDesc(studentId);
        if (latestPlan != null) {
            newPlan.setAdmissionFee(latestPlan.getAdmissionFee());
        } else {
            newPlan.setAdmissionFee(0.0);
        }
        
        newPlan.setEffectiveFrom(effectiveFrom);
        planRepo.save(newPlan);
        
        // 2. Update future fee dues
        List<FeeDue> allFees = feeRepo.findByStudentIdOrderByYearAscMonthAsc(studentId);
        
        for (FeeDue fee : allFees) {
            if (fee.getFeeType() == FeeType.MONTHLY && fee.getDueDate() != null) {
                // If this fee is for a month on or after effective date
                if (!fee.getDueDate().isBefore(effectiveFrom)) {
                    // Update the total amount to new fee
                    fee.setTotalAmount(newFee);
                    feeRepo.save(fee);
                }
            }
        }
        
        // 3. Generate future months if needed (next 12 months from effective date)
        generateFutureMonths(studentId, effectiveFrom);
    }
    
    // Generate missing future months
    private void generateFutureMonths(Long studentId, LocalDate fromDate) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(12);
        
        for (LocalDate d = fromDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
            final LocalDate currentDate = d; // ✅ Fix: Create final variable
            
            boolean exists = feeRepo.findByStudentIdOrderByYearAscMonthAsc(studentId)
                    .stream()
                    .anyMatch(f ->
                            f.getFeeType() == FeeType.MONTHLY &&
                            f.getMonth() == currentDate.getMonthValue() &&
                            f.getYear() == currentDate.getYear()
                    );
            
            if (!exists) {
                FeeDue newFee = new FeeDue();
                newFee.setStudentId(studentId);
                newFee.setFeeType(FeeType.MONTHLY);
                newFee.setMonth(currentDate.getMonthValue());
                newFee.setYear(currentDate.getYear());
                newFee.setDueDate(currentDate);
                newFee.setTotalAmount(getApplicableMonthlyFee(studentId, currentDate));
                newFee.setPaidAmount(0.0);
                newFee.setLateFee(0.0);
                feeRepo.save(newFee);
            }
        }
    }

    // UPDATE MONTHLY FROM (existing method)
    public void updateMonthlyFrom(Long studentId, int fromMonth, int year, double newFee) {
        updateMonthlyFeeWithEffectiveDate(studentId, LocalDate.of(year, fromMonth, 1), newFee);
    }

    // PAYMENT + LATE FEE
    @Transactional
    public void makePayment(Long studentId, double amount, String mode,
                            String txn, double lateFee) {

        // Save payment
        Payment p = new Payment();
        p.setStudentId(studentId);
        p.setAmount(amount);
        p.setPaymentMode(mode);
        p.setTransactionId(txn);
        paymentRepo.save(p);

        double remaining = amount;

        List<FeeDue> dues = feeRepo.findPending(studentId);

        for (FeeDue d : dues) {

            // Admin late fee apply kare ya na kare
            if (lateFee > 0 && d.getLateFee() == 0) {
                d.setLateFee(lateFee);
            }

            double pending = d.getTotalAmount() + d.getLateFee() - d.getPaidAmount();

            if (remaining <= 0) break;

            double used = Math.min(remaining, pending);

            d.setPaidAmount(d.getPaidAmount() + used);
            remaining -= used;

            feeRepo.save(d);
        }
    }

    public Map<String, Object> getSummary(Long studentId) {

        List<FeeDue> list = feeRepo.findByStudentIdOrderByYearAscMonthAsc(studentId);

        double totalPending = 0;

        for (FeeDue f : list) {

            double total = f.getTotalAmount() != null ? f.getTotalAmount() : 0;
            double paid = f.getPaidAmount() != null ? f.getPaidAmount() : 0;
            double late = f.getLateFee() != null ? f.getLateFee() : 0;

            totalPending += (total + late - paid);
        }

        return Map.of(
                "totalPending", totalPending,
                "data", list
        );
    }
}