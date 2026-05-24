package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.TransactionRequest;
import com.personalfinance.manager.dto.TransactionResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.UserRepository;
import com.personalfinance.manager.security.CustomUserDetails;
import com.personalfinance.manager.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        User user = getAuthenticatedUser();
        TransactionResponse response = transactionService.createTransaction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<com.personalfinance.manager.dto.TransactionsListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {
        User user = getAuthenticatedUser();
        List<TransactionResponse> transactions = transactionService.getTransactions(user, startDate, endDate, category);
        return ResponseEntity.ok(new com.personalfinance.manager.dto.TransactionsListResponse(transactions));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequest request) {
        User user = getAuthenticatedUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findById(java.util.Objects.requireNonNull(userDetails.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
