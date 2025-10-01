package com.example.financial_management.model.report.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CategoryReportItem {
    private int category;
    private BigDecimal expense;
    private BigDecimal income;
    private double expensePercentage;
    private double incomePercentage;
    private String categoryName;
    private String transactionTypeName;
    private int type;

    public CategoryReportItem(int category,
            BigDecimal expense, BigDecimal income) {
        this.category = category;
        this.expense = expense != null ? expense : BigDecimal.ZERO;
        this.income = income != null ? income : BigDecimal.ZERO;
    }
}
