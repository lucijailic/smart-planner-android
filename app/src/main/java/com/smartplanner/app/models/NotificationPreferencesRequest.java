package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class NotificationPreferencesRequest {

    @SerializedName("notifications_enabled")
    private final boolean notificationsEnabled;

    @SerializedName("default_task_reminder")
    private final String defaultTaskReminder;

    @SerializedName("default_event_reminder")
    private final String defaultEventReminder;

    public NotificationPreferencesRequest(
            boolean notificationsEnabled,
            String defaultTaskReminder,
            String defaultEventReminder
    ) {
        this.notificationsEnabled = notificationsEnabled;
        this.defaultTaskReminder = defaultTaskReminder;
        this.defaultEventReminder = defaultEventReminder;
    }
}