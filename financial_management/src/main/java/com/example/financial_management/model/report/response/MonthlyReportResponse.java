package com.example.financial_management.model.report.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class MonthlyReportResponse {
    private int year;
    private BigDecimal net;
    private List<MonthlyReportResponseItem> items;
}
