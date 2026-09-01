package com.smartplanner.app.models.auth;

public class ForgotPasswordRequest {

    private final String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}