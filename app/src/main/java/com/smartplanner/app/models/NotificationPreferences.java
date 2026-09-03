package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class NotificationPreferences {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("notifications_enabled")
    private boolean notificationsEnabled;

    @SerializedName("default_task_reminder")
    private String defaultTaskReminder;

    @SerializedName("default_event_reminder")
    private String defaultEventReminder;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public String getUserId() {
        return userId;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public String getDefaultTaskReminder() {
        return defaultTaskReminder;
    }

    public String getDefaultEventReminder() {
        return defaultEventReminder;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}