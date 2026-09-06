package com.martialarts.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.martialarts.backend.dto.FeeLedgerDTO;
import com.martialarts.backend.entity.FeeAllocation;
import com.martialarts.backend.entity.FeeDue;
import com.martialarts.backend.entity.Payment;
import com.martialarts.backend.entity.Student;
import com.martialarts.backend.entity.StudentFeePlan;
import com.martialarts.backend.enums.FeeType;
import com.martialarts.backend.repository.FeeAllocationRepository;
import com.martialarts.backend.repository.FeeDueRepository;
import com.martialarts.backend.repository.PaymentRepository;
import com.martialarts.backend.repository.StudentFeePlanRepository;
import com.martialarts.backend.repository.StudentRepository;

import jakarta.transaction.Transactional;

@Service
public class FeesService {

    @Autowired
    private FeeDueRepository feeRepo;

    @Autowired
    private PaymentRepository paymentRepo;
    
    @Autowired 
    private StudentFeePlanRepository planRepo;
    
    @Autowired
    private StudentRepository studentRepo;
    
    @Autowired
    private FeeDueRepository feeDueRepo;

    @Autowired
    private FeeAllocationRepository allocationRepo;
   

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
    
    
    
    
    
    
    public FeeLedgerDTO getComprehensiveLedger(Long studentId) {

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // =========================================================
        // 1. STUDENT FEE PLAN
        // =========================================================
        StudentFeePlan plan = planRepo.findFirstByStudentIdOrderByEffectiveFromDesc(studentId);

        double monthlyRate = (plan != null && plan.getMonthlyFee() != null)
                ? plan.getMonthlyFee()
                : 0.0;


        // =========================================================
        // 2. APPROVED PAYMENTS ONLY
        // =========================================================
        List<Payment> approvedPayments = paymentRepo
                .findByStudentIdOrderByPaymentDateDesc(studentId)
                .stream()
                .filter(p -> "APPROVED".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        double totalPaidSum = approvedPayments.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();


        // =========================================================
        // 3. ALL FEE DUES
        // =========================================================
        List<FeeDue> dues = feeDueRepo
                .findByStudentIdOrderByYearAscMonthAsc(studentId);


        // =========================================================
        // 4. TOTAL BILLED
        // =========================================================
        double totalBilledSum = dues.stream()
                .mapToDouble(d ->
                        (d.getTotalAmount() != null ? d.getTotalAmount() : 0.0)
                                + (d.getLateFee() != null ? d.getLateFee() : 0.0)
                )
                .sum();


        // =========================================================
        // 5. BASIC LEDGER DTO
        // =========================================================
        FeeLedgerDTO ledger = new FeeLedgerDTO();

        ledger.setStudentId(studentId);
        ledger.setStudentName(student.getName());
        ledger.setMonthlyFeeRate(monthlyRate);
        ledger.setTotalBilled(totalBilledSum);
        ledger.setTotalPaid(totalPaidSum);


        // =========================================================
        // 6. CURRENT DATE
        // =========================================================
        LocalDate today = LocalDate.now();

        int currentMonthVal = today.getMonthValue();
        int currentYearVal = today.getYear();


        // =========================================================
        // 7. STUDENT ADMISSION DATE
        // =========================================================
        if (student.getCreatedAt() != null) {
            ledger.setAdmissionDate(
                    student.getCreatedAt()
                            .toLocalDate()
                            .toString()
            );
        } else {
            ledger.setAdmissionDate("N/A");
        }


        // =========================================================
        // 8. ADVANCE vs OUTSTANDING CALCULATION
        // =========================================================
        if (totalPaidSum >= totalBilledSum) {

            double extraAdvance = totalPaidSum - totalBilledSum;

            ledger.setNetOutstanding(0.0);
            ledger.setAdvanceBalance(extraAdvance);


            // कितने पूरे महीने advance में cover हैं
            int monthsCoveredAhead = monthlyRate > 0
                    ? (int) (extraAdvance / monthlyRate)
                    : 0;


            LocalDate coverageDate = today.plusMonths(monthsCoveredAhead);


            if (extraAdvance > 0 && monthsCoveredAhead > 0) {

                ledger.setAdvanceCoveredUpto(
                        "Covered upto "
                                + coverageDate.getMonth()
                                    .getDisplayName(
                                            TextStyle.FULL,
                                            Locale.ENGLISH
                                    )
                                + " "
                                + coverageDate.getYear()
                );

                ledger.setNextDueDate(
                        coverageDate
                                .plusMonths(1)
                                .withDayOfMonth(5)
                );

            } else if (extraAdvance > 0) {

                ledger.setAdvanceCoveredUpto(
                        "₹" + extraAdvance + " Advance in Wallet"
                );

                ledger.setNextDueDate(
                        today
                                .plusMonths(1)
                                .withDayOfMonth(5)
                );

            } else {

                ledger.setAdvanceCoveredUpto(
                        "No Advance (Up to date)"
                );

                ledger.setNextDueDate(
                        today
                                .plusMonths(1)
                                .withDayOfMonth(5)
                );
            }

            ledger.setNextDueAmount(monthlyRate);


        } else {

            double pending = totalBilledSum - totalPaidSum;

            ledger.setNetOutstanding(pending);
            ledger.setAdvanceBalance(0.0);
            ledger.setAdvanceCoveredUpto(
                    "None (Payment Pending)"
            );

            ledger.setNextDueDate(
                    today.withDayOfMonth(5)
            );

            ledger.setNextDueAmount(pending);
        }


        // =========================================================
        // 9. CURRENT MONTH DUE
        // =========================================================
        FeeDue currentDue = dues.stream()
                .filter(d ->
                        d.getMonth() != null
                                && d.getMonth() == currentMonthVal
                                && d.getYear() != null
                                && d.getYear() == currentYearVal
                )
                .findFirst()
                .orElse(null);


        if (currentDue != null) {

            // Current month due date
            ledger.setCurrentMonthDueDate(
                    currentDue.getDueDate()
            );


            double curTotal = currentDue.getTotalAmount() != null
                    ? currentDue.getTotalAmount()
                    : 0.0;

            double curPaid = currentDue.getPaidAmount() != null
                    ? currentDue.getPaidAmount()
                    : 0.0;


            double curBal = curTotal - curPaid;


            // Current month remaining amount
            ledger.setCurrentMonthDueAmount(
                    curBal > 0 ? curBal : 0.0
            );


            // Current month payment date
            if (curPaid > 0 && currentDue.getPaymentDate() != null) {

                ledger.setCurrentMonthPaidDate(
                        currentDue.getPaymentDate()
                                .toLocalDate()
                                .toString()
                );

            } else {

                ledger.setCurrentMonthPaidDate("Pending");
            }


            // Current month status
            if (curBal <= 0) {

                ledger.setCurrentMonthStatus("PAID");

            } else if (curPaid > 0) {

                ledger.setCurrentMonthStatus("PARTIAL");

            } else {

                ledger.setCurrentMonthStatus("UNPAID");
            }


        } else {

            // =====================================================
            // Current month FeeDue record doesn't exist
            // =====================================================

            ledger.setCurrentMonthDueDate(
                    today.withDayOfMonth(5)
            );


            // अगर advance available है तो current month covered
            if (ledger.getAdvanceBalance() > 0) {

                ledger.setCurrentMonthDueAmount(0.0);

                ledger.setCurrentMonthPaidDate(
                        "Covered by Advance"
                );

                ledger.setCurrentMonthStatus("PAID");

            } else {

                ledger.setCurrentMonthDueAmount(
                        monthlyRate
                );

                ledger.setCurrentMonthPaidDate(
                        "Pending"
                );

                ledger.setCurrentMonthStatus(
                        "UNPAID"
                );
            }
        }


        // =========================================================
        // 10. MONTH-WISE STATEMENTS
        // =========================================================
        List<FeeLedgerDTO.MonthlyStatementItem> statements =
                dues.stream()
                        .map(d -> {

                            FeeLedgerDTO.MonthlyStatementItem item =
                                    new FeeLedgerDTO.MonthlyStatementItem();


                            // -------------------------------------
                            // Month Name
                            // -------------------------------------
                            String mName;

                            if (d.getMonth() != null
                                    && d.getMonth() >= 1
                                    && d.getMonth() <= 12) {

                                mName = Month.of(d.getMonth())
                                        .getDisplayName(
                                                TextStyle.FULL,
                                                Locale.ENGLISH
                                        )
                                        + " "
                                        + d.getYear();

                            } else {

                                mName = d.getFeeType() != null
                                        ? d.getFeeType().toString()
                                        : "FEE";
                            }


                            item.setMonthName(mName);

                            item.setMonth(d.getMonth());
                            item.setYear(d.getYear());


                            // -------------------------------------
                            // Fee Type
                            // -------------------------------------
                            item.setFeeType(
                                    d.getFeeType() != null
                                            ? d.getFeeType().toString()
                                            : "MONTHLY"
                            );


                            // -------------------------------------
                            // Amounts
                            // -------------------------------------
                            double totalAmount =
                                    d.getTotalAmount() != null
                                            ? d.getTotalAmount()
                                            : 0.0;

                            double paidAmount =
                                    d.getPaidAmount() != null
                                            ? d.getPaidAmount()
                                            : 0.0;


                            item.setTotalAmount(
                                    d.getTotalAmount()
                            );

                            item.setPaidAmount(
                                    paidAmount
                            );


                            // -------------------------------------
                            // Balance
                            // -------------------------------------
                            double bal =
                                    totalAmount - paidAmount;


                            item.setDueBalance(
                                    bal > 0 ? bal : 0.0
                            );


                            // -------------------------------------
                            // Status
                            // -------------------------------------
                            if (bal <= 0) {

                                item.setStatus("PAID");

                            } else if (paidAmount > 0) {

                                item.setStatus("PARTIAL");

                            } else {

                                item.setStatus("UNPAID");
                            }


                            // -------------------------------------
                            // Due Date
                            // -------------------------------------
                            item.setDueDate(
                                    d.getDueDate()
                            );


                            // -------------------------------------
                            // Payment Date
                            // -------------------------------------
                            if (d.getPaymentDate() != null) {

                                item.setPaymentDate(
                                        d.getPaymentDate()
                                                .toLocalDate()
                                                .toString()
                                );

                            } else {

                                item.setPaymentDate(null);
                            }


                            return item;

                        })
                        .collect(Collectors.toList());


        // =========================================================
        // 11. SET MONTHLY STATEMENTS
        // =========================================================
        ledger.setMonthlyStatements(statements);


        // =========================================================
        // 12. RETURN COMPLETE LEDGER
        // =========================================================
        return ledger;
    }

    
    
    @Transactional
    public void processPaymentSettlement(Payment payment) {
        double remainingAmount = payment.getAmount();
        Long studentId = payment.getStudentId();

        // छात्र के सभी अनपेड या पार्टियल ड्यूज को पुराने से नए क्रम (FIFO) में लाएं
        List<FeeDue> unpaidDues = feeDueRepo.findByStudentIdOrderByYearAscMonthAsc(studentId);

        for (FeeDue due : unpaidDues) {
            if (remainingAmount <= 0) break;

            double dueTotal = (due.getTotalAmount() != null ? due.getTotalAmount() : 0.0)
                            + (due.getLateFee() != null ? due.getLateFee() : 0.0);
            double alreadyPaid = due.getPaidAmount() != null ? due.getPaidAmount() : 0.0;
            double pendingOnThisMonth = dueTotal - alreadyPaid;

            if (pendingOnThisMonth > 0) {
                double allocate = Math.min(remainingAmount, pendingOnThisMonth);
                
                // 1. Fee Due को अपडेट करें
                due.setPaidAmount(alreadyPaid + allocate);
                if ((alreadyPaid + allocate) >= dueTotal) {
                    due.setStatus("PAID");
                } else {
                    due.setStatus("PARTIAL");
                }
                due.setPaymentDate(LocalDateTime.now());
                feeDueRepo.save(due);

                // 2. Fee Allocation रिकॉर्ड बनाएं
                FeeAllocation alloc = new FeeAllocation();
                alloc.setPaymentId(payment.getId());
                alloc.setFeeDueId(due.getId());
                alloc.setAmount(allocate);
                allocationRepo.save(alloc);

                remainingAmount -= allocate;
            }
        }

        // अगर पैसा ड्यूज से ज्यादा है (remainingAmount > 0), 
        // तो वह getComprehensiveLedger() द्वारा अपने आप Advance Wallet में दिखेगा।
    }
    
}