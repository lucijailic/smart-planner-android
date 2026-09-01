package com.smartplanner.app.utils;

import android.util.Patterns;

public final class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private ValidationUtils() {
        // Prevent instantiation
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmedEmail = email.trim();

        return !trimmedEmail.isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidName(String name) {
        return name != null
                && !name.trim().isEmpty()
                && name.trim().length() >= 2;
    }

    public static boolean doPasswordsMatch(
            String password,
            String confirmPassword
    ) {
        return password != null
                && password.equals(confirmPassword);
    }
}