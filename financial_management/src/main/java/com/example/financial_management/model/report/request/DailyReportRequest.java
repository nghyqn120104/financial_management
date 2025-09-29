package com.example.financial_management.model.report.request;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class DailyReportRequest {
    private LocalDateTime month;
    private UUID accountId;
}
