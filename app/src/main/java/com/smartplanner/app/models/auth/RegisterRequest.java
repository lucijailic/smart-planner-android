package com.smartplanner.app.models.auth;

public class RegisterRequest {

    private final String email;
    private final String password;
    private final UserMetadata data;

    public RegisterRequest(
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        this.email = email;
        this.password = password;
        this.data = new UserMetadata(firstName, lastName);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserMetadata getData() {
        return data;
    }

    public static class UserMetadata {

        private final String first_name;
        private final String last_name;

        public UserMetadata(String firstName, String lastName) {
            this.first_name = firstName;
            this.last_name = lastName;
        }

        public String getFirst_name() {
            return first_name;
        }

        public String getLast_name() {
            return last_name;
        }
    }
}