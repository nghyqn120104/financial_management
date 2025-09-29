package com.example.financial_management.model.report.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class DailyReportResponse {
    @JsonFormat(pattern = "MM-yyyy")
    private String month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal net;
    private List<DailyReportResponseItem> items;
}
