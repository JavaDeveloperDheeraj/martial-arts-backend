package com.martialarts.backend.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private Long studentId;
    private Double amount;
    private String mode;
    private String transactionId;
    private Double lateFee;
}