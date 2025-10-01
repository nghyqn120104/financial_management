package com.example.financial_management.model.report.request;

import java.time.LocalDate;
import java.util.UUID;

import com.example.financial_management.exception.FlexibleLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
public class CategoryReportRequest {
    private UUID accountId;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate fromDate;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate toDate;

    public void setAccountId(UUID accountId) {
        if (accountId != null && accountId.toString().trim().isEmpty()) {
            this.accountId = null;
        } else {
            this.accountId = accountId;
        }
    }
}
