package com.example.financial_management.model.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class TransactionFilterRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int category;
    private int type;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private int page = 1;
    private int size = 20;
}
