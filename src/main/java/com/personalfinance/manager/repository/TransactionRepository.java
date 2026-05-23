package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Finds all transactions for a user, ordered by transaction date in descending order (most recent first).
     */
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
}
