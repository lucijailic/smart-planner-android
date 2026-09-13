package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UserAvailability;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public class SmartPlanInput {

    private final List<Task> tasks;
    private final List<Event> events;
    private final List<UserAvailability> availability;
    private final PlanningPreferences preferences;
    private final List<SmartPlanItem> existingItems;

    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    private final ZonedDateTime now;

    public SmartPlanInput(
            List<Task> tasks,
            List<Event> events,
            List<UserAvailability> availability,
            PlanningPreferences preferences,
            List<SmartPlanItem> existingItems,
            LocalDate periodStart,
            LocalDate periodEnd,
            ZonedDateTime now
    ) {
        this.tasks = tasks;
        this.events = events;
        this.availability = availability;
        this.preferences = preferences;
        this.existingItems = existingItems;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.now = now;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<UserAvailability> getAvailability() {
        return availability;
    }

    public PlanningPreferences getPreferences() {
        return preferences;
    }

    public List<SmartPlanItem> getExistingItems() {
        return existingItems;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public ZonedDateTime getNow() {
        return now;
    }
}