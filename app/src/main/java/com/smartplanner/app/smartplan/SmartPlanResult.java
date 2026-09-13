package com.smartplanner.app.smartplan;

import java.util.ArrayList;
import java.util.List;

public class SmartPlanResult {

    private final List<GeneratedSmartPlanItem> plannedItems;
    private final List<UnscheduledTask> unscheduledTasks;
    private final List<String> warnings;

    public SmartPlanResult() {
        this.plannedItems = new ArrayList<>();
        this.unscheduledTasks = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public List<GeneratedSmartPlanItem> getPlannedItems() {
        return plannedItems;
    }

    public List<UnscheduledTask> getUnscheduledTasks() {
        return unscheduledTasks;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addPlannedItem(
            GeneratedSmartPlanItem item
    ) {
        if (item != null) {
            plannedItems.add(item);
        }
    }

    public void addUnscheduledTask(
            UnscheduledTask task
    ) {
        if (task != null) {
            unscheduledTasks.add(task);
        }
    }

    public void addWarning(
            String warning
    ) {
        if (warning != null
                && !warning.trim().isEmpty()) {

            warnings.add(warning);
        }
    }

    public boolean hasPlannedItems() {
        return !plannedItems.isEmpty();
    }

    public boolean hasUnscheduledTasks() {
        return !unscheduledTasks.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}