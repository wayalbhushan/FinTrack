package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.CategoryReportDetail;
import com.personalfinance.manager.dto.ReportResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Aggregates total income, total expenses, net savings, and category breakdown for a given month.
     */
    @Transactional(readOnly = true)
    public ReportResponse getMonthlyReport(User user, int year, int month) {
        log.info("User {} requesting monthly report for {}-{}", user.getId(), year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        return generateReport(user, startDate, endDate);
    }

    /**
     * Aggregates total income, total expenses, net savings, and category breakdown for a given year.
     */
    @Transactional(readOnly = true)
    public ReportResponse getYearlyReport(User user, int year) {
        log.info("User {} requesting yearly report for {}", user.getId(), year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return generateReport(user, startDate, endDate);
    }

    private ReportResponse generateReport(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumIncomeByUserAndDateRange(user, startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.sumExpenseByUserAndDateRange(user, startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        List<CategoryReportDetail> breakdown = transactionRepository.getCategoryBreakdownByUserAndDateRange(user, startDate, endDate);
        Map<String, BigDecimal> breakdownMap = breakdown.stream()
                .collect(Collectors.toMap(
                        CategoryReportDetail::getCategoryName,
                        d -> formatAmount(d.getTotalAmount()),
                        BigDecimal::add
                ));

        log.info("Report generated for User {} from {} to {}: totalIncome={}, totalExpenses={}, netSavings={}",
                user.getId(), startDate, endDate, totalIncome, totalExpenses, netSavings);

        return new ReportResponse(
                formatAmount(totalIncome),
                formatAmount(totalExpenses),
                formatAmount(netSavings),
                breakdownMap
        );
    }

    private BigDecimal formatAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
