package com.personalfinance.manager.service;

import com.personalfinance.manager.dto.CategoryRequest;
import com.personalfinance.manager.dto.CategoryResponse;
import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ConflictException;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.CategoryRepository;
import com.personalfinance.manager.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves all default categories (user is null) and the user's custom categories.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(User user) {
        log.info("Fetching categories for user {}", user.getId());
        List<Category> categories = categoryRepository.findAllDefaultAndCustomByUser(user);
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new custom category for the user.
     * Ensures uniqueness of category name per user (cannot shadow default or user's own categories).
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        log.info("User {} attempting to create category: {}", user.getId(), request.getName());

        Optional<Category> existingDefault = categoryRepository.findByUserAndName(null, request.getName());
        Optional<Category> existingCustom = categoryRepository.findByUserAndName(user, request.getName());

        if (existingDefault.isPresent() || existingCustom.isPresent()) {
            log.warn("Category with name {} already exists for user {} or as default", request.getName(), user.getId());
            throw new ConflictException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .isCustom(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("User {} successfully created custom category: {} (ID: {})", user.getId(), saved.getName(), saved.getId());
        return toResponse(saved);
    }

    /**
     * Deletes a custom category by name.
     * Business rules:
     * - Cannot delete default categories.
     * - Cannot delete custom categories if referenced by existing transactions.
     */
    @Transactional
    public void deleteCategory(String name, User user) {
        log.info("User {} attempting to delete category: {}", user.getId(), name);

        // 1. Business Rule: Cannot delete default categories
        if (categoryRepository.findByUserAndName(null, name).isPresent()) {
            log.warn("User {} attempted to delete default category {}", user.getId(), name);
            throw new IllegalArgumentException("Cannot delete default categories");
        }

        // 2. Retrieve custom category
        Category category = categoryRepository.findByUserAndName(user, name)
                .orElseThrow(() -> {
                    log.warn("Category not found or access denied for deletion: {} by user {}", name, user.getId());
                    return new ResourceNotFoundException("Custom category '" + name + "' not found");
                });

        // 3. Business Rule: Cannot delete custom categories if referenced by existing transactions
        if (transactionRepository.existsByUserAndCategory(user, category)) {
            log.warn("Custom category {} is in use by transactions for user {}", name, user.getId());
            throw new ConflictException("Category '" + name + "' is currently referenced by transactions and cannot be deleted");
        }

        categoryRepository.delete(category);
        log.info("User {} successfully deleted custom category: {}", user.getId(), name);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIsCustom()
        );
    }
}
