package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Finds a category by its associated user and name.
     * Since global default categories have a null user, this method can also find them by passing null.
     */
    Optional<Category> findByUserAndName(User user, String name);

    /**
     * Fetches all default categories (where user is null) as well as
     * the custom categories defined by the specified user.
     */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user")
    List<Category> findAllDefaultAndCustomByUser(@Param("user") User user);
}
