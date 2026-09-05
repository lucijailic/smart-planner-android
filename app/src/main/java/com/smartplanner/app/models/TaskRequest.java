package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;

public class TaskRequest {

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("category_id")
    private final String categoryId;

    @SerializedName("title")
    private final String title;

    @SerializedName("description")
    private final String description;

    @SerializedName("priority")
    private final TaskPriority priority;

    @SerializedName("status")
    private final TaskStatus status;

    @SerializedName("deadline")
    private final String deadline;

    @SerializedName("estimated_duration")
    private final Integer estimatedDuration;

    @SerializedName("is_important")
    private final boolean important;

    @SerializedName("reminder_type")
    private final ReminderType reminderType;

    @SerializedName("completed_at")
    private final String completedAt;

    public TaskRequest(
            String userId,
            String categoryId,
            String title,
            String description,
            TaskPriority priority,
            TaskStatus status,
            String deadline,
            Integer estimatedDuration,
            boolean important,
            ReminderType reminderType,
            String completedAt
    ) {

        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.estimatedDuration = estimatedDuration;
        this.important = important;
        this.reminderType = reminderType;
        this.completedAt = completedAt;
    }
}