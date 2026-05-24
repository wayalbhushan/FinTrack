package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.SavingsGoalRequest;
import com.personalfinance.manager.dto.SavingsGoalResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.UserRepository;
import com.personalfinance.manager.security.CustomUserDetails;
import com.personalfinance.manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
@Validated
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserRepository userRepository;

    public SavingsGoalController(SavingsGoalService savingsGoalService, UserRepository userRepository) {
        this.savingsGoalService = savingsGoalService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        User user = getAuthenticatedUser();
        SavingsGoalResponse response = savingsGoalService.createGoal(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<com.personalfinance.manager.dto.SavingsGoalsListResponse> getGoals() {
        User user = getAuthenticatedUser();
        List<SavingsGoalResponse> goals = savingsGoalService.getGoals(user);
        return ResponseEntity.ok(new com.personalfinance.manager.dto.SavingsGoalsListResponse(goals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        SavingsGoalResponse goal = savingsGoalService.getGoal(id, user);
        return ResponseEntity.ok(goal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @RequestBody SavingsGoalRequest request) {
        User user = getAuthenticatedUser();
        SavingsGoalResponse response = savingsGoalService.updateGoal(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        savingsGoalService.deleteGoal(id, user);
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
