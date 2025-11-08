package com.example.invoice_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private String invoiceNumber;
    private String transactionId;
    private LocalDateTime invoiceDate;
    private Dealer dealer;
    private Vehicle vehicle;
    private String customerName;
    private double basePrice;
    private double taxAmount;
    private double taxPercentage;
    private double totalAmount;
}
