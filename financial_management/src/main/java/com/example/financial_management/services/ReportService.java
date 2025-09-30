package com.example.financial_management.services;

import java.math.BigDecimal;
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

import com.example.financial_management.constant.Status;
import com.example.financial_management.constant.TransactionType;
import com.example.financial_management.entity.User;
import com.example.financial_management.model.auth.Auth;
import com.example.financial_management.model.report.request.DailyReportRequest;
import com.example.financial_management.model.report.request.SummaryReportRequest;
import com.example.financial_management.model.report.response.DailyReportResponse;
import com.example.financial_management.model.report.response.DailyReportResponseItem;
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
