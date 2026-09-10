package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.ReminderType;

public class EventRequest {

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("category_id")
    private final String categoryId;

    @SerializedName("title")
    private final String title;

    @SerializedName("description")
    private final String description;

    @SerializedName("start_at")
    private final String startAt;

    @SerializedName("end_at")
    private final String endAt;

    @SerializedName("location")
    private final String location;

    @SerializedName("is_important")
    private final boolean important;

    @SerializedName("reminder_type")
    private final ReminderType reminderType;

    public EventRequest(
            String userId,
            String categoryId,
            String title,
            String description,
            String startAt,
            String endAt,
            String location,
            boolean important,
            ReminderType reminderType
    ) {

        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.location = location;
        this.important = important;
        this.reminderType = reminderType;
    }
}