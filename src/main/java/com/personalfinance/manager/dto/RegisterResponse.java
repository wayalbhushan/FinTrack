package com.personalfinance.manager.dto;

public class RegisterResponse {

    private String message;
    private String userId;

    // Constructors
    public RegisterResponse() {
    }

    public RegisterResponse(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
