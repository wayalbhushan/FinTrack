package com.personalfinance.manager.dto;

import com.personalfinance.manager.entity.CategoryType;
import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private String name;
    private CategoryType type;
    private Boolean isCustom;

    public CategoryResponse() {
    }

    public CategoryResponse(UUID id, String name, CategoryType type, Boolean isCustom) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
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

    public Boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(Boolean isCustom) {
        this.isCustom = isCustom;
    }
}
