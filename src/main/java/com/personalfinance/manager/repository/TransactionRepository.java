package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.dto.CategoryReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions for a user, ordered by transaction date in descending order (most recent first).
     */
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    /**
     * Checks if a category is referenced by any transaction belonging to the user.
     */
    boolean existsByUserAndCategory(User user, Category category);

    /**
     * Filters transactions based on optional parameters (startDate, endDate, categoryName) for the authenticated user,
     * ordered by transaction date descending, then creation date descending.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:categoryName IS NULL OR t.category.name = :categoryName) " +
           "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<Transaction> filterTransactions(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("categoryName") String categoryName
    );

    /**
     * Calculates the net savings (Total Income - Total Expenses) generated strictly between the given dates for the user.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
           "FROM Transaction t WHERE t.user = :user " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate <= :endDate")
    BigDecimal calculateNetSavings(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Sums the total income for a user in a given date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = 'INCOME' " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumIncomeByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Sums the total expenses for a user in a given date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = 'EXPENSE' " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumExpenseByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Aggregates amounts grouped by category name and category type for a user in a date range.
     */
    @Query("SELECT new com.personalfinance.manager.dto.CategoryReportDetail(t.category.name, t.category.type, SUM(t.amount)) " +
           "FROM Transaction t " +
           "WHERE t.user = :user " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
           "GROUP BY t.category.name, t.category.type")
    List<CategoryReportDetail> getCategoryBreakdownByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
