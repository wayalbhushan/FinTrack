package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.SavingsGoalRequest;
import com.personalfinance.manager.dto.SavingsGoalResponse;
import com.personalfinance.manager.entity.SavingsGoal;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.SavingsGoalRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private static final Logger log = LoggerFactory.getLogger(SavingsGoalService.class);

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, TransactionRepository transactionRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Creates a savings goal for the user.
     */
    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request, User user) {
        log.info("User {} attempting to create savings goal {}", user.getId(), request.getGoalName());

        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(request.getStartDate())
                .build();

        if (goal.getStartDate() == null) {
            goal.setStartDate(LocalDate.now());
        }

        SavingsGoal saved = savingsGoalRepository.save(goal);
        log.info("User {} created savings goal {} (ID: {})", user.getId(), saved.getGoalName(), saved.getId());
        return toResponse(saved);
    }

    /**
     * Retrieves all savings goals for the user.
     */
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getGoals(User user) {
        log.info("User {} fetching all savings goals", user.getId());
        List<SavingsGoal> goals = savingsGoalRepository.findByUser(user);
        return goals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single savings goal for the user.
     */
    @Transactional(readOnly = true)
    public SavingsGoalResponse getGoal(Long id, User user) {
        log.info("User {} fetching savings goal {}", user.getId(), id);
        SavingsGoal goal = savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Savings goal {} not found or access denied for user {}", id, user.getId());
                    return new ResourceNotFoundException("Savings goal not found");
                });
        return toResponse(goal);
    }

    /**
     * Updates an existing savings goal.
     */
    @Transactional
    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request, User user) {
        log.info("User {} attempting to update savings goal {}", user.getId(), id);

        SavingsGoal goal = savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Savings goal {} not found or access denied for user {}", id, user.getId());
                    return new ResourceNotFoundException("Savings goal not found");
                });

        if (request.getGoalName() != null) {
            goal.setGoalName(request.getGoalName());
        }
        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Target amount must be positive");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            if (request.getTargetDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }
        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }

        SavingsGoal updated = savingsGoalRepository.save(goal);
        log.info("User {} successfully updated savings goal {}", user.getId(), updated.getId());
        return toResponse(updated);
    }

    /**
     * Deletes a savings goal.
     */
    @Transactional
    public void deleteGoal(Long id, User user) {
        log.info("User {} attempting to delete savings goal {}", user.getId(), id);

        SavingsGoal goal = savingsGoalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Savings goal {} not found or access denied for user {}", id, user.getId());
                    return new ResourceNotFoundException("Savings goal not found");
                });

        savingsGoalRepository.delete(java.util.Objects.requireNonNull(goal));
        log.info("User {} deleted savings goal {}", user.getId(), id);
    }

    /**
     * Dynamic helper to map SavingsGoal entity to SavingsGoalResponse DTO.
     * Calculates currentProgress, progressPercentage, and remainingAmount on the fly.
     */
    private SavingsGoalResponse toResponse(SavingsGoal goal) {
        BigDecimal currentProgress = transactionRepository.calculateNetSavings(
                goal.getUser(),
                goal.getStartDate(),
                LocalDate.now()
        );

        if (currentProgress == null) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal targetAmount = goal.getTargetAmount();
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progressForCalc = currentProgress.compareTo(BigDecimal.ZERO) < 0
                    ? BigDecimal.ZERO
                    : currentProgress;
            progressPercentage = progressForCalc
                    .divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            if (progressPercentage.scale() < 1) {
                progressPercentage = progressPercentage.setScale(1, java.math.RoundingMode.HALF_UP);
            }
        }

        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getGoalName(),
                formatAmount(targetAmount),
                goal.getTargetDate(),
                goal.getStartDate(),
                formatAmount(currentProgress),
                progressPercentage,
                formatAmount(remainingAmount)
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
