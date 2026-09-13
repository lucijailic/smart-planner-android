package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.enums.TaskPriority;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public final class TaskScorer {

    private static final int DEADLINE_OVERDUE = 100;
    private static final int DEADLINE_TODAY = 80;
    private static final int DEADLINE_TOMORROW = 60;
    private static final int DEADLINE_WITHIN_3_DAYS = 40;
    private static final int DEADLINE_WITHIN_7_DAYS = 20;
    private static final int DEADLINE_LATER = 10;
    private static final int DEADLINE_NONE = 0;

    private static final int PRIORITY_HIGH = 30;
    private static final int PRIORITY_MEDIUM = 20;
    private static final int PRIORITY_LOW = 10;

    private static final int IMPORTANT_BONUS = 10;

    private TaskScorer() {
        // Prevent instantiation
    }

    public static int calculateScore(
            Task task,
            ZonedDateTime now
    ) {

        if (task == null || now == null) {
            return 0;
        }

        int score = 0;

        score += calculateDeadlineScore(
                task.getDeadline(),
                now
        );

        score += calculatePriorityScore(
                task.getPriority()
        );

        if (task.isImportant()) {
            score += IMPORTANT_BONUS;
        }

        return score;
    }

    private static int calculateDeadlineScore(
            String deadlineValue,
            ZonedDateTime now
    ) {

        if (deadlineValue == null
                || deadlineValue.trim().isEmpty()) {

            return DEADLINE_NONE;
        }

        try {

            ZonedDateTime deadline =
                    ZonedDateTime.parse(deadlineValue);

            if (deadline.isBefore(now)) {
                return DEADLINE_OVERDUE;
            }

            LocalDate today =
                    now.toLocalDate();

            LocalDate deadlineDate =
                    deadline.toLocalDate();

            long daysUntilDeadline =
                    ChronoUnit.DAYS.between(
                            today,
                            deadlineDate
                    );

            if (daysUntilDeadline == 0) {
                return DEADLINE_TODAY;
            }

            if (daysUntilDeadline == 1) {
                return DEADLINE_TOMORROW;
            }

            if (daysUntilDeadline <= 3) {
                return DEADLINE_WITHIN_3_DAYS;
            }

            if (daysUntilDeadline <= 7) {
                return DEADLINE_WITHIN_7_DAYS;
            }

            return DEADLINE_LATER;

        } catch (Exception ignored) {

            return DEADLINE_NONE;
        }
    }

    private static int calculatePriorityScore(
            TaskPriority priority
    ) {

        if (priority == null) {
            return 0;
        }

        switch (priority) {

            case HIGH:
                return PRIORITY_HIGH;

            case MEDIUM:
                return PRIORITY_MEDIUM;

            case LOW:
                return PRIORITY_LOW;

            default:
                return 0;
        }
    }
}