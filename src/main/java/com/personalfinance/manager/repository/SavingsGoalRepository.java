package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.personalfinance.manager.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

    /**
     * Finds all savings goals for the user.
     */
    List<SavingsGoal> findByUser(User user);

    /**
     * Finds a savings goal by ID and user.
     */
    Optional<SavingsGoal> findByIdAndUser(UUID id, User user);
}
