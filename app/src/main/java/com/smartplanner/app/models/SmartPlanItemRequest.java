package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;

public class SmartPlanItemRequest {

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


    public SmartPlanItemRequest(
            String smartPlanId,
            String userId,
            String taskId,
            String plannedStart,
            String plannedEnd,
            int plannedDuration,
            SmartPlanItemStatus status,
            boolean manuallyAdjusted
    ) {
        this.smartPlanId = smartPlanId;
        this.userId = userId;
        this.taskId = taskId;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
        this.plannedDuration = plannedDuration;
        this.status = status;
        this.manuallyAdjusted = manuallyAdjusted;
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
}