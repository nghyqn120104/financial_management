package com.example.financial_management.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.financial_management.constant.Category;
import com.example.financial_management.constant.Status;
import com.example.financial_management.constant.TransactionType;
import com.example.financial_management.entity.User;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.report.request.CategoryReportRequest;
import com.example.financial_management.model.report.request.ReportRequest;
import com.example.financial_management.model.report.request.MonthlyReportRequest;
import com.example.financial_management.model.report.request.SummaryReportRequest;
import com.example.financial_management.model.report.response.CategoryReportItem;
import com.example.financial_management.model.report.response.CategoryReportResponse;
import com.example.financial_management.model.report.response.CompareReportResponse;
import com.example.financial_management.model.report.response.DailyReportResponse;
import com.example.financial_management.model.report.response.DailyReportResponseItem;
import com.example.financial_management.model.report.response.MonthlyReportResponse;
import com.example.financial_management.model.report.response.MonthlyReportResponseItem;
import com.example.financial_management.model.report.response.SummaryReportResponse;
import com.example.financial_management.repository.TransactionRepository;
import com.example.financial_management.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;

        public SummaryReportResponse getSummary(SummaryReportRequest request, Auth auth) {
                User user = getUser(auth);

                validateAccountAccess(auth, request.getAccountId());

                // Convert LocalDate -> LocalDateTime
                LocalDateTime fromDateTime = request.getFrom() != null
                                ? request.getFrom().atStartOfDay()
                                : LocalDate.MIN.atStartOfDay();

                LocalDateTime toDateTime = request.getTo() != null
                                ? request.getTo().atTime(LocalTime.MAX)
                                : LocalDateTime.now();

                BigDecimal income = transactionRepository
                                .sumAmount(user.getId(), fromDateTime, toDateTime, request.getAccountId(),
                                                TransactionType.INCOME)
                                .orElse(BigDecimal.ZERO);

                BigDecimal expense = transactionRepository
                                .sumAmount(user.getId(), fromDateTime, toDateTime, request.getAccountId(),
                                                TransactionType.EXPENSE)
                                .orElse(BigDecimal.ZERO);

                BigDecimal netBalance = income.subtract(expense);

                SummaryReportResponse response = new SummaryReportResponse();
                response.setIncome(income);
                response.setExpense(expense);
                response.setNetBalance(netBalance);

                return response;
        }

        public DailyReportResponse getDailyReport(ReportRequest request, Auth auth) {
                User user = getUser(auth);

                validateAccountAccess(auth, request.getAccountId());

                YearMonth month = parseMonth(request.getMonth());
                String monthFormatted = month.format(DateTimeFormatter.ofPattern("MM-yyyy"));
                LocalDateTime start = month.atDay(1).atStartOfDay();
                LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);

                List<Object[]> rows = transactionRepository.sumDaily(
                                user.getId(), start, end, request.getAccountId());

                List<DailyReportResponseItem> items = rows.stream()
                                .map(row -> new DailyReportResponseItem(
                                                ((java.sql.Date) row[0]).toLocalDate(),
                                                UUID.fromString((String) row[1]),
                                                (BigDecimal) row[2],
                                                (BigDecimal) row[3]))
                                .toList();

                // Tính tổng income và expense
                BigDecimal totalIncome = items.stream()
                                .map(DailyReportResponseItem::getIncome)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalExpense = items.stream()
                                .map(DailyReportResponseItem::getExpense)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal net = totalIncome.subtract(totalExpense);

                DailyReportResponse response = new DailyReportResponse();
                response.setMonth(monthFormatted);
                response.setTotalIncome(totalIncome);
                response.setTotalExpense(totalExpense);
                response.setNet(net);
                response.setItems(items);

                return response;
        }

        public MonthlyReportResponse getMonthlyReport(MonthlyReportRequest request, Auth auth) {
                User user = getUser(auth);

                validateAccountAccess(auth, request.getAccountId());

                List<MonthlyReportResponseItem> items = transactionRepository.sumMonthly(
                                user.getId(), request.getYear(), request.getAccountId());

                BigDecimal income = items.stream().map(MonthlyReportResponseItem::getIncome).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal expense = items.stream().map(MonthlyReportResponseItem::getExpense).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal net = income.subtract(expense);

                MonthlyReportResponse response = new MonthlyReportResponse();
                response.setYear(request.getYear());
                response.setNet(net);
                response.setItems(items);

                return response;
        }

        public CategoryReportResponse getCategoryReport(CategoryReportRequest request, Auth auth) {
                User user = getUser(auth);

                validateAccountAccess(auth, request.getAccountId());

                // Query
                List<CategoryReportItem> items = transactionRepository.sumByCategory(
                                user.getId(),
                                request.getAccountId(),
                                request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null,
                                request.getToDate() != null ? request.getToDate().atTime(LocalTime.MAX) : null);

                // Tổng chi / thu
                BigDecimal totalExpense = items.stream()
                                .map(CategoryReportItem::getExpense)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalIncome = items.stream()
                                .map(CategoryReportItem::getIncome)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Tính % trên chi tiêu
                for (CategoryReportItem item : items) {
                        if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                                double expensePercentage = item.getExpense()
                                                .divide(totalExpense, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                                .doubleValue();
                                item.setExpensePercentage(expensePercentage);
                        } else {
                                item.setExpensePercentage(0.0);
                        }

                        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                                double incomePercent = item.getIncome()
                                                .divide(totalIncome, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                                .doubleValue();
                                item.setIncomePercentage(incomePercent);
                        } else {
                                item.setIncomePercentage(0.0);
                        }

                        // Xác định transaction type dựa vào giá trị
                        if (item.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                                item.setTransactionTypeName("Expense");
                        } else if (item.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                                item.setTransactionTypeName("Income");
                        } else {
                                item.setTransactionTypeName("Unknown");
                        }

                        // Gán tên category
                        item.setCategoryName(Category.getName(item.getCategory()));
                }

                // Response
                CategoryReportResponse response = new CategoryReportResponse();
                response.setItems(items);
                response.setTotalExpense(totalExpense);
                response.setTotalIncome(totalIncome);
                return response;
        }

        public CompareReportResponse getCompareReport(ReportRequest request, Auth auth) {
                User user = getUser(auth);
                validateAccountAccess(auth, request.getAccountId());

                YearMonth thisMonth = parseMonth(request.getMonth());
                String monthFormatted = thisMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));
                YearMonth lastMonth = thisMonth.minusMonths(1);

                // Range tháng này
                LocalDateTime startThisMonth = thisMonth.atDay(1).atStartOfDay();
                LocalDateTime endThisMonth = thisMonth.atEndOfMonth().atTime(LocalTime.MAX);

                // Range tháng trước
                LocalDateTime startLastMonth = lastMonth.atDay(1).atStartOfDay();
                LocalDateTime endLastMonth = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);

                // Query
                BigDecimal incomeThis = transactionRepository.sumAmount(user.getId(), startThisMonth, endThisMonth,
                                request.getAccountId(), TransactionType.INCOME).orElse(BigDecimal.ZERO);

                BigDecimal expenseThis = transactionRepository.sumAmount(user.getId(), startThisMonth, endThisMonth,
                                request.getAccountId(), TransactionType.EXPENSE).orElse(BigDecimal.ZERO);

                BigDecimal incomeLast = transactionRepository.sumAmount(user.getId(), startLastMonth, endLastMonth,
                                request.getAccountId(), TransactionType.INCOME).orElse(BigDecimal.ZERO);

                BigDecimal expenseLast = transactionRepository.sumAmount(user.getId(), startLastMonth, endLastMonth,
                                request.getAccountId(), TransactionType.EXPENSE).orElse(BigDecimal.ZERO);

                // Net
                BigDecimal netThis = incomeThis.subtract(expenseThis);
                BigDecimal netLast = incomeLast.subtract(expenseLast);

                // % thay đổi
                Double incomeChange = calcPercentChange(incomeThis, incomeLast);
                Double expenseChange = calcPercentChange(expenseThis, expenseLast);
                Double netChange = calcPercentChange(netThis, netLast);

                // Response
                CompareReportResponse response = new CompareReportResponse();
                response.setMonth(monthFormatted);

                response.setIncomeThisMonth(incomeThis);
                response.setExpenseThisMonth(expenseThis);
                response.setNetThisMonth(netThis);

                response.setIncomeLastMonth(incomeLast);
                response.setExpenseLastMonth(expenseLast);
                response.setNetLastMonth(netLast);

                response.setIncomeChangePercent(incomeChange);
                response.setExpenseChangePercent(expenseChange);
                response.setNetChangePercent(netChange);

                return response;
        }

        public ResponseEntity<byte[]> exportMonthlyReportByMonthPDF(ReportRequest request, Auth auth) {
                byte[] pdf = buildMonthlyReportByMonthPDF(request, auth);
                return buildPdfResponse(pdf, "report-" + request.getMonth() + ".pdf");
        }

        public ResponseEntity<byte[]> exportMonthlyReportByYearPDF(MonthlyReportRequest request, Auth auth) {
                byte[] pdf = buildMonthlyReportByYearPDF(request, auth);
                return buildPdfResponse(pdf, "report-" + request.getYear() + ".pdf");
        }

        // --- Private helper để tạo file PDF ---
        private byte[] buildMonthlyReportByMonthPDF(ReportRequest request, Auth auth) {
                DailyReportResponse data = getDailyReport(request, auth);

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        Document doc = new Document(PageSize.A4);
                        PdfWriter.getInstance(doc, out);
                        doc.open();

                        // --- Tiêu đề ---
                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                        Paragraph title = new Paragraph("Monthly Report - " + request.getMonth(), titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        doc.add(title);
                        doc.add(Chunk.NEWLINE);

                        // --- Bảng dữ liệu ---
                        PdfPTable table = new PdfPTable(3);
                        table.setWidthPercentage(100);
                        addTableHeader(table, new String[] { "Date", "Income", "Expense" });

                        for (DailyReportResponseItem item : data.getItems()) {
                                table.addCell(item.getDate().toString());
                                table.addCell(item.getIncome().toString());
                                table.addCell(item.getExpense().toString());
                        }

                        doc.add(table);
                        doc.add(Chunk.NEWLINE);

                        // --- Tổng kết ---
                        Font summaryFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                        Paragraph summaryTitle = new Paragraph("Summary", summaryFont);
                        summaryTitle.setAlignment(Element.ALIGN_LEFT);
                        doc.add(summaryTitle);
                        doc.add(Chunk.NEWLINE);

                        PdfPTable summaryTable = new PdfPTable(2);
                        summaryTable.setWidthPercentage(60);
                        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                        summaryTable.setWidths(new float[] { 3, 2 });

                        addSummaryRow(summaryTable, "Total Income:", data.getTotalIncome().toString());
                        addSummaryRow(summaryTable, "Total Expense:", data.getTotalExpense().toString());
                        addSummaryRow(summaryTable, "Net:", data.getNet().toString());

                        doc.add(summaryTable);

                        doc.close();
                        return out.toByteArray();
                } catch (Exception e) {
                        throw new RuntimeException("Error generating monthly PDF", e);
                }
        }

        private byte[] buildMonthlyReportByYearPDF(MonthlyReportRequest request, Auth auth) {
                MonthlyReportResponse data = getMonthlyReport(request, auth);

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        Document doc = new Document(PageSize.A4);
                        PdfWriter.getInstance(doc, out);
                        doc.open();

                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                        Paragraph title = new Paragraph("Yearly Report - " + request.getYear(), titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        doc.add(title);
                        doc.add(Chunk.NEWLINE);

                        PdfPTable table = new PdfPTable(4);
                        table.setWidthPercentage(100);
                        addTableHeader(table, new String[] { "Month", "Income", "Expense", "Net" });

                        // Biến cộng dồn
                        BigDecimal totalIncome = BigDecimal.ZERO;
                        BigDecimal totalExpense = BigDecimal.ZERO;
                        BigDecimal totalNet = BigDecimal.ZERO;

                        for (MonthlyReportResponseItem item : data.getItems()) {
                                table.addCell(item.getMonth());
                                table.addCell(item.getIncome().toString());
                                table.addCell(item.getExpense().toString());
                                table.addCell(item.getNet().toString());

                                totalIncome = totalIncome.add(item.getIncome());
                                totalExpense = totalExpense.add(item.getExpense());
                                totalNet = totalNet.add(item.getNet());
                        }

                        // Thêm dòng tổng cuối bảng
                        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", boldFont));
                        totalCell.setColspan(1);
                        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(totalCell);
                        table.addCell(new Phrase(totalIncome.toString(), boldFont));
                        table.addCell(new Phrase(totalExpense.toString(), boldFont));
                        table.addCell(new Phrase(totalNet.toString(), boldFont));

                        doc.add(table);
                        doc.close();
                        return out.toByteArray();

                } catch (Exception e) {
                        throw new RuntimeException("Error generating yearly PDF", e);
                }
        }

        // Helper method: thêm dòng tổng kết
        private void addSummaryRow(PdfPTable table, String label, String value) {
                PdfPCell labelCell = new PdfPCell(new Phrase(label));
                labelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(labelCell);

                PdfPCell valueCell = new PdfPCell(new Phrase(value));
                valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(valueCell);
        }

        private void addTableHeader(PdfPTable table, String[] headers) {
                for (String header : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(header));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);
                }
        }

        // --- Build ResponseEntity ---
        private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(
                                ContentDisposition.attachment().filename(filename).build());
                return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        }

        private Double calcPercentChange(BigDecimal current, BigDecimal previous) {
                if (previous.compareTo(BigDecimal.ZERO) == 0) {
                        return null; // hoặc 100% nếu bạn muốn coi là tăng toàn bộ
                }
                return current.subtract(previous)
                                .divide(previous, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
        }

        private User getUser(Auth auth) {
                return userRepository.findByIdAndStatus(UUID.fromString(auth.getId()), Status.ACTIVE)
                                .orElseThrow(() -> new RuntimeException("User not found"));
        }

        private void validateAccountAccess(Auth auth, UUID accountId) {
                if (accountId == null) {
                        // Nếu null, cho phép xem tất cả account của user
                        return;
                }

                boolean hasAccess = auth.getAccounts().stream()
                                .anyMatch(acc -> acc.getId().equals(accountId));

                if (!hasAccess) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this account");
                }
        }

        private YearMonth parseMonth(String monthStr) {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                .appendPattern("[MM-yyyy][M-yyyy]") // có thể parse MM-yyyy hoặc M-yyyy
                                .toFormatter();

                try {
                        return YearMonth.parse(monthStr, formatter);
                } catch (DateTimeParseException e) {
                        throw new RuntimeException("Invalid month format, expected MM-yyyy or M-yyyy");
                }
        }

}
