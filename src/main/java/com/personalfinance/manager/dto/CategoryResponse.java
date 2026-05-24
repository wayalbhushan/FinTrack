package com.personalfinance.manager.dto;

import com.personalfinance.manager.entity.CategoryType;
import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private String name;
    private CategoryType type;
    private Boolean custom;

    public CategoryResponse() {
    }

    public CategoryResponse(UUID id, String name, CategoryType type, Boolean custom) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.custom = custom;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public Boolean getCustom() {
        return custom;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }
}
