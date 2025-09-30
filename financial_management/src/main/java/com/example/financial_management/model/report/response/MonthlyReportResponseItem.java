package com.example.financial_management.model.report.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MonthlyReportResponseItem {
    private String month; 
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net; 

    public MonthlyReportResponseItem(Integer month, BigDecimal income, BigDecimal expense, Integer year) {
        this.month = String.format("%02d-%d", month, year);
        this.income = income != null ? income : BigDecimal.ZERO;
        this.expense = expense != null ? expense : BigDecimal.ZERO;
        this.net = this.income.subtract(this.expense);
    }
}
