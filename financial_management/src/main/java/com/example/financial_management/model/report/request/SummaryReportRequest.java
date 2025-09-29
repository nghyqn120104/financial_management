package com.example.financial_management.model.report.request;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class SummaryReportRequest {
    private LocalDate from;
    private LocalDate to;
    private UUID accountId;
}
