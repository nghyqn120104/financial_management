package com.example.financial_management.model.report.request;

import java.time.LocalDate;
import java.util.UUID;

import com.example.financial_management.exception.FlexibleLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
public class SummaryReportRequest {
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate from;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate to;
    private UUID accountId;
}
