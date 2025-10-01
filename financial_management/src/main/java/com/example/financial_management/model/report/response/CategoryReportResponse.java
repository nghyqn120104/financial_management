package com.example.financial_management.model.report.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CategoryReportResponse {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private List<CategoryReportItem> items;
}
