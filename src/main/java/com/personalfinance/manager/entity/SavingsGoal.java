package com.personalfinance.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
@EntityListeners(AuditingEntityListener.class)
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @NotBlank(message = "Goal name is required")
    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be in the future")
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public SavingsGoal() {
    }

    public SavingsGoal(Long id, User user, String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.startDate = startDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
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

    // Builder pattern
    public static SavingsGoalBuilder builder() {
        return new SavingsGoalBuilder();
    }

    public static class SavingsGoalBuilder {
        private Long id;
        private User user;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private LocalDate startDate;
        private Instant createdAt;
        private Instant updatedAt;

        public SavingsGoalBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SavingsGoalBuilder user(User user) {
            this.user = user;
            return this;
        }

        public SavingsGoalBuilder goalName(String goalName) {
            this.goalName = goalName;
            return this;
        }

        public SavingsGoalBuilder targetAmount(BigDecimal targetAmount) {
            this.targetAmount = targetAmount;
            return this;
        }

        public SavingsGoalBuilder targetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
            return this;
        }

        public SavingsGoalBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public SavingsGoalBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SavingsGoalBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @org.springframework.lang.NonNull
        public SavingsGoal build() {
            return new SavingsGoal(id, user, goalName, targetAmount, targetDate, startDate, createdAt, updatedAt);
        }
    }
}
