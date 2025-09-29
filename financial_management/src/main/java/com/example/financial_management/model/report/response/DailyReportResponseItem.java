package com.example.financial_management.model.report.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class DailyReportResponseItem {
    private LocalDate date;
    private BigDecimal income;
    private BigDecimal expense;

    public DailyReportResponseItem(LocalDate date, BigDecimal income, BigDecimal expense) {
        this.date = date;
        this.income = income;
        this.expense = expense;
    }
}

