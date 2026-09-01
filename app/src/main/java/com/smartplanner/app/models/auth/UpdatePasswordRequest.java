package com.smartplanner.app.models.auth;

public class UpdatePasswordRequest {

    private final String password;

    public UpdatePasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}