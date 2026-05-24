package com.personalfinance.manager.controller;

import com.personalfinance.manager.dto.ReportResponse;
import com.personalfinance.manager.entity.User;
import com.personalfinance.manager.exception.ResourceNotFoundException;
import com.personalfinance.manager.repository.UserRepository;
import com.personalfinance.manager.security.CustomUserDetails;
import com.personalfinance.manager.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        User user = getAuthenticatedUser();
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        ReportResponse report = reportService.getMonthlyReport(user, year, month);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<ReportResponse> getYearlyReport(@PathVariable int year) {
        User user = getAuthenticatedUser();
        ReportResponse report = reportService.getYearlyReport(user, year);
        return ResponseEntity.ok(report);
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
