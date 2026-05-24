package com.personalfinance.manager.dto;

import com.personalfinance.manager.entity.CategoryType;
import java.math.BigDecimal;

public class CategoryReportDetail {

    private String categoryName;
    private CategoryType type;
    private BigDecimal totalAmount;

    public CategoryReportDetail() {
    }

    public CategoryReportDetail(String categoryName, CategoryType type, BigDecimal totalAmount) {
        this.categoryName = categoryName;
        this.type = type;
        this.totalAmount = totalAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
