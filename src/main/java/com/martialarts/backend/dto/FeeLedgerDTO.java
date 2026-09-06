package com.martialarts.backend.dto;


import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class FeeLedgerDTO {
    private Long studentId;
    private String studentName;
    private Double monthlyFeeRate;
    
    
    
 // Current Cycle Details (At a glance)
    private String admissionDate;       // e.g. "2026-01-15"
    private LocalDate currentMonthDueDate; // e.g. "2026-09-05"
    private String currentMonthPaidDate;   // e.g. "2026-09-03" or "Not Paid"
    private Double currentMonthDueAmount;  // Current month's balance
    private String currentMonthStatus;     // PAID, PARTIAL, UNPAID, ADVANCE
    
    // Key Metrics
    private Double totalBilled;      // कुल बनी फीस
    private Double totalPaid;        // कुल भरी गई फीस
    private Double netOutstanding;   // अगर पॉजिटिव है तो बकाया (Pending Due)
    private Double advanceBalance;    // अगर एक्स्ट्रा है तो एडवांस
    private String advanceCoveredUpto; // जैसे "Paid upto October 2026"
    private LocalDate nextDueDate;    // अगली फीस भरने की तारीख
    private Double nextDueAmount;     // अगले महीने कितना देना है

    // महीने-वार ब्रेकअप
    private List<MonthlyStatementItem> monthlyStatements;

    @Data
    public static class MonthlyStatementItem {
        private String monthName; // e.g. "July 2026"
        private Integer month;
        private Integer year;
        private String feeType;   // MONTHLY / ADMISSION
        private Double totalAmount;
        private Double paidAmount;
        private Double dueBalance;
        private String status;    // PAID, PARTIAL, UNPAID, ADVANCE
        private LocalDate dueDate;
        private String paymentDate;
    }
}