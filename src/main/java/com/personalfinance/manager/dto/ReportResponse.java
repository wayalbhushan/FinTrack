package com.personalfinance.manager.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private List<CategoryReportDetail> categoryBreakdown;

    public ReportResponse() {
    }

    public ReportResponse(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal netSavings, List<CategoryReportDetail> categoryBreakdown) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netSavings = netSavings;
        this.categoryBreakdown = categoryBreakdown;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }

    public List<CategoryReportDetail> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategoryReportDetail> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }
}
