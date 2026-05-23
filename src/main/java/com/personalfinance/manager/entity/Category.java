package com.personalfinance.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_categories_user_name", columnNames = {"user_id", "name"})
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Category name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Category type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;

    @NotNull(message = "isCustom flag is required")
    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public Category() {
    }

    public Category(UUID id, User user, String name, CategoryType type, Boolean isCustom, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // Builder pattern
    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public static class CategoryBuilder {
        private UUID id;
        private User user;
        private String name;
        private CategoryType type;
        private Boolean isCustom;
        private Instant createdAt;

        public CategoryBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder user(User user) {
            this.user = user;
            return this;
        }

        public CategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CategoryBuilder type(CategoryType type) {
            this.type = type;
            return this;
        }

        public CategoryBuilder isCustom(Boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        public CategoryBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @org.springframework.lang.NonNull
        public Category build() {
            return new Category(id, user, name, type, isCustom, createdAt);
        }
    }
}
