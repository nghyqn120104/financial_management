package com.example.financial_management.model.report.request;

import java.util.UUID;

import lombok.Data;

@Data
public class ReportRequest {
    private String month;
    private UUID accountId;
}
