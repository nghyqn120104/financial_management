package com.example.financial_management.model.report.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MonthlyReportRequest {
    private int year;
    private UUID accountId;
}
