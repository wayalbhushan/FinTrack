package com.personalfinance.manager.repository;

import com.personalfinance.manager.entity.Category;
import com.personalfinance.manager.entity.CategoryType;
import com.personalfinance.manager.entity.SavingsGoal;
import com.personalfinance.manager.entity.Transaction;
import com.personalfinance.manager.entity.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class RepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Test
    void testUserAuditingAndPersistence() {
        // Arrange
        User user = User.builder()
                .username("john.doe@example.com")
                .password("securePassword123")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();

        // Act
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testUserValidationInvalidEmail() {
        // Arrange
        User user = User.builder()
                .username("invalid-email")
                .password("securePassword123")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> {
            userRepository.save(user);
            entityManager.flush();
        });
    }

    @Test
    void testCategoryUniqueConstraint() {
        // Arrange
        User user = User.builder()
                .username("jane.doe@example.com")
                .password("securePassword")
                .fullName("Jane Doe")
                .phoneNumber("+1987654321")
                .build();
        userRepository.save(user);

        Category category1 = Category.builder()
                .user(user)
                .name("Groceries")
                .type(CategoryType.EXPENSE)
                .isCustom(true)
                .build();
        categoryRepository.save(category1);
        entityManager.flush();

        Category category2 = Category.builder()
                .user(user)
                .name("Groceries") // Duplicate name for same user
                .type(CategoryType.EXPENSE)
                .isCustom(true)
                .build();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            categoryRepository.save(category2);
            entityManager.flush();
        });
        assertThat(exception.getClass().getSimpleName()).contains("ViolationException");
    }

    @Test
    void testCategoryCustomQueries() {
        // Arrange
        User user = User.builder()
                .username("test.user@example.com")
                .password("password")
                .fullName("Test User")
                .phoneNumber("+1000000000")
                .build();
        userRepository.save(user);

        // Global default category (user_id is null)
        Category globalCategory = Category.builder()
                .user(null)
                .name("Salary")
                .type(CategoryType.INCOME)
                .isCustom(false)
                .build();
        categoryRepository.save(globalCategory);

        // Custom category for our user
        Category customCategory = Category.builder()
                .user(user)
                .name("Entertainment")
                .type(CategoryType.EXPENSE)
                .isCustom(true)
                .build();
        categoryRepository.save(customCategory);

        // Custom category for a different user
        User otherUser = User.builder()
                .username("other@example.com")
                .password("password")
                .fullName("Other User")
                .phoneNumber("+2000000000")
                .build();
        userRepository.save(otherUser);

        Category otherCustomCategory = Category.builder()
                .user(otherUser)
                .name("Business Travel")
                .type(CategoryType.EXPENSE)
                .isCustom(true)
                .build();
        categoryRepository.save(otherCustomCategory);

        entityManager.flush();

        // Act & Assert: Test findByUserAndName
        Optional<Category> foundCategoryOpt = categoryRepository.findByUserAndName(user, "Entertainment");
        assertThat(foundCategoryOpt).isPresent();
        assertThat(foundCategoryOpt.get().getName()).isEqualTo("Entertainment");

        // Act & Assert: Test findAllDefaultAndCustomByUser
        List<Category> allCategories = categoryRepository.findAllDefaultAndCustomByUser(user);
        assertThat(allCategories)
                .hasSize(2)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Salary", "Entertainment");
    }

    @Test
    void testTransactionValidationAndQueries() {
        // Arrange
        User user = User.builder()
                .username("user.tx@example.com")
                .password("password")
                .fullName("Tx User")
                .phoneNumber("+3000000000")
                .build();
        userRepository.save(user);

        Category category = Category.builder()
                .user(user)
                .name("Dining")
                .type(CategoryType.EXPENSE)
                .isCustom(true)
                .build();
        categoryRepository.save(category);

        Transaction tx1 = Transaction.builder()
                .user(user)
                .category(category)
                .amount(new BigDecimal("25.50"))
                .transactionDate(LocalDate.now().minusDays(2))
                .description("Lunch")
                .build();

        Transaction tx2 = Transaction.builder()
                .user(user)
                .category(category)
                .amount(new BigDecimal("150.00"))
                .transactionDate(LocalDate.now())
                .description("Dinner")
                .build();

        transactionRepository.save(tx1);
        transactionRepository.save(tx2);
        entityManager.flush();

        // Act & Assert: Test findByUserOrderByTransactionDateDesc
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        assertThat(transactions).hasSize(2);
        // Diner (today) should be first, Lunch (2 days ago) second
        assertThat(transactions.get(0).getDescription()).isEqualTo("Dinner");
        assertThat(transactions.get(1).getDescription()).isEqualTo("Lunch");

        // Act & Assert: Test Positive validation
        Transaction invalidTx = Transaction.builder()
                .user(user)
                .category(category)
                .amount(new BigDecimal("-10.00")) // Negative amount
                .transactionDate(LocalDate.now())
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            transactionRepository.save(invalidTx);
            entityManager.flush();
        });
    }

    @Test
    void testSavingsGoalValidation() {
        // Arrange
        User user = User.builder()
                .username("user.goal@example.com")
                .password("password")
                .fullName("Goal User")
                .phoneNumber("+4000000000")
                .build();
        userRepository.save(user);

        // Act & Assert: Test Future date validation (targetDate must be in the future)
        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .goalName("Car Fund")
                .targetAmount(new BigDecimal("20000.00"))
                .startDate(LocalDate.now())
                .targetDate(LocalDate.now().minusDays(1)) // Past date (invalid)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            savingsGoalRepository.save(goal);
            entityManager.flush();
        });
    }
}
