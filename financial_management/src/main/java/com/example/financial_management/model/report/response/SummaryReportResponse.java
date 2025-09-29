package com.example.financial_management.model.report.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SummaryReportResponse {
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal netBalance;
}
