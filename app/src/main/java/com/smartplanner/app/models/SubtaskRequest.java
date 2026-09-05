package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class SubtaskRequest {

    @SerializedName("task_id")
    private final String taskId;

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("title")
    private final String title;

    @SerializedName("is_completed")
    private final boolean completed;

    public SubtaskRequest(
            String taskId,
            String userId,
            String title,
            boolean completed
    ) {

        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.completed = completed;
    }
}