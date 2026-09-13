package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.enums.SmartPlanItemStatus;

import java.time.ZonedDateTime;

public class GeneratedSmartPlanItem {

    private final String taskId;
    private final ZonedDateTime plannedStart;
    private final ZonedDateTime plannedEnd;
    private final int plannedDuration;
    private final SmartPlanItemStatus status;

    public GeneratedSmartPlanItem(
            String taskId,
            ZonedDateTime plannedStart,
            ZonedDateTime plannedEnd,
            int plannedDuration
    ) {
        this.taskId = taskId;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
        this.plannedDuration = plannedDuration;
        this.status = SmartPlanItemStatus.PLANNED;
    }

    public String getTaskId() {
        return taskId;
    }

    public ZonedDateTime getPlannedStart() {
        return plannedStart;
    }

    public ZonedDateTime getPlannedEnd() {
        return plannedEnd;
    }

    public int getPlannedDuration() {
        return plannedDuration;
    }

    public SmartPlanItemStatus getStatus() {
        return status;
    }
}