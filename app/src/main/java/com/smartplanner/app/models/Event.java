package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.ReminderType;

public class Event {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("start_at")
    private String startAt;

    @SerializedName("end_at")
    private String endAt;

    @SerializedName("location")
    private String location;

    @SerializedName("is_important")
    private boolean important;

    @SerializedName("reminder_type")
    private ReminderType reminderType;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public String getLocation() {
        return location;
    }

    public boolean isImportant() {
        return important;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}