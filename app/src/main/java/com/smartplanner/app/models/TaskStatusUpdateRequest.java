package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.TaskStatus;

public class TaskStatusUpdateRequest {

    @SerializedName("status")
    private final TaskStatus status;

    @SerializedName("completed_at")
    private final String completedAt;

    public TaskStatusUpdateRequest(
            TaskStatus status,
            String completedAt
    ) {
        this.status = status;
        this.completedAt = completedAt;
    }
}