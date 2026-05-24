package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.CategoryResponse;
import com.personalfinance.manager.dto.TransactionRequest;
import com.personalfinance.manager.dto.TransactionResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a transaction.
     * Validates that the referenced category belongs to the user or is a default category.
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        log.info("User {} attempting to create transaction", user.getId());

        Category category = categoryRepository.findById(java.util.Objects.requireNonNull(request.getCategoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Validate Category belongs to user or is default (user is null)
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to use category {} which belongs to another user", user.getId(), category.getId());
            throw new IllegalArgumentException("Referenced category does not belong to the user");
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("User {} created transaction {} in category {}", user.getId(), saved.getId(), category.getName());
        return toResponse(saved);
    }

    /**
     * Filters transactions for the user.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(User user, LocalDate startDate, LocalDate endDate, UUID categoryId) {
        log.info("User {} fetching filtered transactions: startDate={}, endDate={}, categoryId={}",
                user.getId(), startDate, endDate, categoryId);

        List<Transaction> transactions = transactionRepository.filterTransactions(user, startDate, endDate, categoryId);
        return transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates a transaction.
     * Business rules:
     * - Cannot update the transactionDate.
     * - Category must belong to the user or be a default.
     * - Tenant isolation is enforced.
     */
    @Transactional
    public TransactionResponse updateTransaction(UUID id, TransactionRequest request, User user) {
        log.info("User {} attempting to update transaction {}", user.getId(), id);

        Transaction transaction = transactionRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Anti-IDOR Check
        if (!transaction.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted unauthorized access to update transaction {}", user.getId(), id);
            throw new ResourceNotFoundException("Transaction not found");
        }

        // Business Rule: Cannot update the transaction date
        if (request.getTransactionDate() != null && !request.getTransactionDate().equals(transaction.getTransactionDate())) {
            log.warn("User {} attempted to modify date of transaction {}", user.getId(), id);
            throw new IllegalArgumentException("Cannot update the transaction date");
        }

        Category category = categoryRepository.findById(java.util.Objects.requireNonNull(request.getCategoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Validate Category ownership
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to associate transaction {} with unauthorized category {}", user.getId(), id, category.getId());
            throw new IllegalArgumentException("Referenced category does not belong to the user");
        }

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());

        Transaction updated = transactionRepository.save(transaction);
        log.info("User {} updated transaction {}", user.getId(), updated.getId());
        return toResponse(updated);
    }

    /**
     * Deletes a transaction. Enforces tenant isolation.
     */
    @Transactional
    public void deleteTransaction(UUID id, User user) {
        log.info("User {} attempting to delete transaction {}", user.getId(), id);

        Transaction transaction = transactionRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Anti-IDOR Check
        if (!transaction.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted unauthorized access to delete transaction {}", user.getId(), id);
            throw new ResourceNotFoundException("Transaction not found");
        }

        transactionRepository.delete(java.util.Objects.requireNonNull(transaction));
        log.info("User {} deleted transaction {}", user.getId(), id);
    }

    private TransactionResponse toResponse(Transaction t) {
        CategoryResponse categoryResponse = new CategoryResponse(
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getCategory().getType(),
                t.getCategory().getIsCustom()
        );
        return new TransactionResponse(
                t.getId(),
                categoryResponse,
                t.getAmount(),
                t.getTransactionDate(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
