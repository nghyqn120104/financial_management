package com.example.financial_management.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.financial_management.model.AbstractResponse;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.report.request.CategoryReportRequest;
import com.example.financial_management.model.report.request.DailyReportRequest;
import com.example.financial_management.model.report.request.MonthlyReportRequest;
import com.example.financial_management.model.report.request.SummaryReportRequest;
import com.example.financial_management.model.report.response.CategoryReportResponse;
import com.example.financial_management.model.report.response.DailyReportResponse;
import com.example.financial_management.model.report.response.MonthlyReportResponse;
import com.example.financial_management.model.report.response.SummaryReportResponse;
import com.example.financial_management.services.ReportService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "Report and Statistic")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/summary")
    public ResponseEntity<AbstractResponse<SummaryReportResponse>> getSummary(@RequestBody SummaryReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<SummaryReportResponse>().withData(() -> reportService.getSummary(request, auth));
    }

    @PostMapping("/daily")
    public ResponseEntity<AbstractResponse<DailyReportResponse>> getDailyReport(@RequestBody DailyReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<DailyReportResponse>().withData(() -> reportService.getDailyReport(request, auth));
    }

    @PostMapping("/monthly")
    public ResponseEntity<AbstractResponse<MonthlyReportResponse>> getMonthlyReport(
            @RequestBody MonthlyReportRequest request, @Parameter(hidden = true) @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<MonthlyReportResponse>()
                .withData(() -> reportService.getMonthlyReport(request, auth));
    }

    @PostMapping("/category")
    public ResponseEntity<AbstractResponse<CategoryReportResponse>> getCategoryReport(
            @RequestBody CategoryReportRequest request, @Parameter(hidden = true) @AuthenticationPrincipal Auth auth) {
        return new AbstractResponse<CategoryReportResponse>()
                .withData(() -> reportService.getCategoryReport(request, auth));
    }

}
