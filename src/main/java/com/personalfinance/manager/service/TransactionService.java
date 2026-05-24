package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.TransactionRequest;
import com.personalfinance.manager.dto.TransactionResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, EntityManager entityManager) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }

    /**
     * Creates a transaction.
     * Looks up Category by name (must belong to the user OR be a default category).
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        log.info("User {} attempting to create transaction", user.getId());

        Category category = categoryRepository.findByUserAndName(user, request.getCategory())
                .or(() -> categoryRepository.findByUserAndName(null, request.getCategory()))
                .orElseThrow(() -> {
                    log.warn("Category {} not found for user {}", request.getCategory(), user.getId());
                    return new IllegalArgumentException("Category not found: " + request.getCategory());
                });

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .transactionDate(request.getDate())
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
    public List<TransactionResponse> getTransactions(User user, LocalDate startDate, LocalDate endDate, String categoryName) {
        log.info("User {} fetching filtered transactions: startDate={}, endDate={}, categoryName={}",
                user.getId(), startDate, endDate, categoryName);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        Root<Transaction> root = query.from(Transaction.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("user"), user));

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
        }
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("category").get("name"), categoryName));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("transactionDate")), cb.desc(root.get("createdAt")));

        List<Transaction> transactions = entityManager.createQuery(query).getResultList();
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
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, User user) {
        log.info("User {} attempting to update transaction {}", user.getId(), id);

        Transaction transaction = transactionRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Anti-IDOR Check
        if (!transaction.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted unauthorized access to update transaction {}", user.getId(), id);
            throw new ResourceNotFoundException("Transaction not found");
        }



        if (request.getCategory() != null) {
            Category category = categoryRepository.findByUserAndName(user, request.getCategory())
                    .or(() -> categoryRepository.findByUserAndName(null, request.getCategory()))
                    .orElseThrow(() -> {
                        log.warn("Category {} not found for user {}", request.getCategory(), user.getId());
                        return new IllegalArgumentException("Category not found: " + request.getCategory());
                    });
            transaction.setCategory(category);
        }

        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            transaction.setAmount(request.getAmount());
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction updated = transactionRepository.save(transaction);
        log.info("User {} updated transaction {}", user.getId(), updated.getId());
        return toResponse(updated);
    }

    /**
     * Deletes a transaction. Enforces tenant isolation.
     */
    @Transactional
    public void deleteTransaction(Long id, User user) {
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
        return new TransactionResponse(
                t.getId(),
                t.getCategory().getName(),
                t.getCategory().getType().name(),
                formatAmount(t.getAmount()),
                t.getTransactionDate(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getUpdatedAt()
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
