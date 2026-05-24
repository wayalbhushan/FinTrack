package com.personalfinance.manager.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionResponse {

    private UUID id;
    private CategoryResponse category;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public TransactionResponse() {
    }

    public TransactionResponse(UUID id, CategoryResponse category, BigDecimal amount, LocalDate transactionDate, String description, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
