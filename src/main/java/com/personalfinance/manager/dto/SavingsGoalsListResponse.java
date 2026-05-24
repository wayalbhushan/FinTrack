package com.personalfinance.manager.dto;

import java.util.List;

public class SavingsGoalsListResponse {

    private List<SavingsGoalResponse> goals;

    public SavingsGoalsListResponse() {
    }

    public SavingsGoalsListResponse(List<SavingsGoalResponse> goals) {
        this.goals = goals;
    }

    public List<SavingsGoalResponse> getGoals() {
        return goals;
    }

    public void setGoals(List<SavingsGoalResponse> goals) {
        this.goals = goals;
    }
}
