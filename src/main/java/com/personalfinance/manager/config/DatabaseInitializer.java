package com.personalfinance.manager.config;

import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.CategoryType;
import com.personalfinance.manager.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final CategoryRepository categoryRepository;

    public DatabaseInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        initializeDefaultCategory("Salary", CategoryType.INCOME);
        initializeDefaultCategory("Food", CategoryType.EXPENSE);
        initializeDefaultCategory("Rent", CategoryType.EXPENSE);
    }

    private void initializeDefaultCategory(String name, CategoryType type) {
        if (categoryRepository.findByUserAndName(null, name).isEmpty()) {
            Category category = Category.builder()
                    .name(name)
                    .type(type)
                    .isCustom(false)
                    .user(null)
                    .build();
            categoryRepository.save(category);
            log.info("Initialized default category: {} ({})", name, type);
        }
    }
}
