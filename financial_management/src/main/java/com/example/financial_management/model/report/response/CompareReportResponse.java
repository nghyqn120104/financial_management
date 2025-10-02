package com.example.financial_management.model.report.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CompareReportResponse {
    private String month; // tháng hiện tại (format MM-yyyy)

    // Tháng này
    private BigDecimal incomeThisMonth;
    private BigDecimal expenseThisMonth;
    private BigDecimal netThisMonth;

    // Tháng trước
    private BigDecimal incomeLastMonth;
    private BigDecimal expenseLastMonth;
    private BigDecimal netLastMonth;

    // % thay đổi
    private Double incomeChangePercent;
    private Double expenseChangePercent;
    private Double netChangePercent;
}
