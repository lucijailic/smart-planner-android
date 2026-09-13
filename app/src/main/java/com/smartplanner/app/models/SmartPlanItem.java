package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;

public class SmartPlanItem {

    @SerializedName("id")
    private String id;

    @SerializedName("smart_plan_id")
    private String smartPlanId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("planned_start")
    private String plannedStart;

    @SerializedName("planned_end")
    private String plannedEnd;

    @SerializedName("planned_duration")
    private int plannedDuration;

    @SerializedName("status")
    private SmartPlanItemStatus status;

    @SerializedName("is_manually_adjusted")
    private boolean manuallyAdjusted;

    @SerializedName("completed_at")
    private String completedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;


    public String getId() {
        return id;
    }

    public String getSmartPlanId() {
        return smartPlanId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getPlannedStart() {
        return plannedStart;
    }

    public String getPlannedEnd() {
        return plannedEnd;
    }

    public int getPlannedDuration() {
        return plannedDuration;
    }

    public SmartPlanItemStatus getStatus() {
        return status;
    }

    public boolean isManuallyAdjusted() {
        return manuallyAdjusted;
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