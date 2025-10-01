package com.example.financial_management.services;

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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.financial_management.constant.Category;
import com.example.financial_management.constant.Status;
import com.example.financial_management.constant.TransactionType;
import com.example.financial_management.entity.User;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.report.request.CategoryReportRequest;
import com.example.financial_management.model.report.request.DailyReportRequest;
import com.example.financial_management.model.report.request.MonthlyReportRequest;
import com.example.financial_management.model.report.request.SummaryReportRequest;
import com.example.financial_management.model.report.response.CategoryReportItem;
import com.example.financial_management.model.report.response.CategoryReportResponse;
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

        public DailyReportResponse getDailyReport(DailyReportRequest request, Auth auth) {
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
