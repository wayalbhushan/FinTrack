package com.personalfinance.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class TransactionResponse {

    private Long id;
    private String category;
    private String type;
    private BigDecimal amount;

    @JsonProperty("date")
    private LocalDate date;

    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, String category, String type, BigDecimal amount, LocalDate date, String description, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
