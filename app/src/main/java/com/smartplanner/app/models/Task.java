package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;

public class Task {

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

    @SerializedName("priority")
    private TaskPriority priority;

    @SerializedName("status")
    private TaskStatus status;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("estimated_duration")
    private Integer estimatedDuration;

    @SerializedName("is_important")
    private boolean important;

    @SerializedName("reminder_type")
    private ReminderType reminderType;

    @SerializedName("completed_at")
    private String completedAt;

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

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDeadline() {
        return deadline;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public boolean isImportant() {
        return important;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}