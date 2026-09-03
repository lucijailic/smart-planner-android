package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class UserProfile {

    @SerializedName("id")
    private String id;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public UserProfile() {
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getFullName() {

        String first = firstName == null
                ? ""
                : firstName.trim();

        String last = lastName == null
                ? ""
                : lastName.trim();

        return (first + " " + last).trim();
    }

    public String getInitials() {

        StringBuilder initials = new StringBuilder();

        if (firstName != null
                && !firstName.trim().isEmpty()) {

            initials.append(
                    Character.toUpperCase(
                            firstName.trim().charAt(0)
                    )
            );
        }

        if (lastName != null
                && !lastName.trim().isEmpty()) {

            initials.append(
                    Character.toUpperCase(
                            lastName.trim().charAt(0)
                    )
            );
        }

        return initials.length() > 0
                ? initials.toString()
                : "?";
    }
}