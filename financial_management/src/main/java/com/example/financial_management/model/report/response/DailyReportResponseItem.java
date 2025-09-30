package com.example.financial_management.model.report.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class DailyReportResponseItem {
    private LocalDate date;
    private UUID accountId;
    private BigDecimal income;
    private BigDecimal expense;

    public DailyReportResponseItem(LocalDate date, UUID accountId ,BigDecimal income, BigDecimal expense) {
        this.date = date;
        this.accountId = accountId;
        this.income = income;
        this.expense = expense;
    }
}

