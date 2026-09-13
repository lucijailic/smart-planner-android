package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.Task;

public class UnscheduledTask {

    private final Task task;
    private final UnscheduledReason reason;
    private final int remainingMinutes;

    public UnscheduledTask(
            Task task,
            UnscheduledReason reason,
            int remainingMinutes
    ) {
        this.task = task;
        this.reason = reason;
        this.remainingMinutes = remainingMinutes;
    }

    public Task getTask() {
        return task;
    }

    public UnscheduledReason getReason() {
        return reason;
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }
}