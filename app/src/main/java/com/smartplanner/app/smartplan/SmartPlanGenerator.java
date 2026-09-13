package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.utils.DateTimeUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SmartPlanGenerator {

    private SmartPlanGenerator() {
        // Prevent instantiation
    }

    public static SmartPlanResult generate(
            SmartPlanInput input
    ) {

        SmartPlanResult result =
                new SmartPlanResult();

        if (!SmartPlanValidator.isInputValid(input)) {

            result.addWarning(
                    "Smart Plan input is invalid."
            );

            return result;
        }

        List<TaskCandidate> candidates =
                prepareTaskCandidates(
                        input,
                        result
                );

        /*
         * No eligible Tasks is a valid result.
         */
        if (candidates.isEmpty()) {
            return result;
        }

        scheduleCandidates(
                input,
                candidates,
                result
        );

        addDeadlineWarnings(
                candidates,
                result
        );

        addRemainingTasksAsUnscheduled(
                input,
                candidates,
                result
        );

        return result;
    }

    private static void scheduleCandidates(
            SmartPlanInput input,
            List<TaskCandidate> candidates,
            SmartPlanResult result
    ) {

        PlanningPreferences preferences =
                input.getPreferences();

        Map<LocalDate, Integer> plannedMinutesByDay =
                new HashMap<>();

        Map<LocalDate, List<TimeSlot>> freeSlotsByDay =
                buildFreeSlots(
                        input
                );

        /*
         * Stores breaks reserved after generated sessions.
         *
         * Breaks are soft preferences, so they can later
         * be shortened if they prevent otherwise valid
         * scheduling.
         */
        List<ReservedBreak> reservedBreaks =
                new ArrayList<>();

        boolean progressMade;

        do {

            progressMade = false;

            for (TaskCandidate candidate : candidates) {

                if (candidate
                        .getRemainingPlanningMinutes() <= 0) {

                    continue;
                }

                GeneratedSmartPlanItem generatedItem =
                        tryScheduleOneSession(
                                input,
                                candidate,
                                freeSlotsByDay,
                                plannedMinutesByDay,
                                preferences
                        );

                /*
                 * If no session fits while full breaks are
                 * reserved, try shortening/removing one of
                 * those soft breaks and try again.
                 */
                if (generatedItem == null
                        && preferences.getBreakMinutes() > 0) {

                    boolean breakShortened =
                            restoreReservedBreakForCandidate(
                                    input,
                                    candidate,
                                    freeSlotsByDay,
                                    plannedMinutesByDay,
                                    preferences,
                                    reservedBreaks
                            );

                    if (breakShortened) {

                        addWarningIfMissing(
                                result,
                                "BREAK_SHORTENED"
                        );

                        generatedItem =
                                tryScheduleOneSession(
                                        input,
                                        candidate,
                                        freeSlotsByDay,
                                        plannedMinutesByDay,
                                        preferences
                                );
                    }
                }

                if (generatedItem == null) {
                    continue;
                }

                result.addPlannedItem(
                        generatedItem
                );

                candidate.subtractPlanningMinutes(
                        generatedItem.getPlannedDuration()
                );

                LocalDate sessionDate =
                        generatedItem
                                .getPlannedStart()
                                .toLocalDate();

                int currentDailyMinutes =
                        plannedMinutesByDay.getOrDefault(
                                sessionDate,
                                0
                        );

                plannedMinutesByDay.put(
                        sessionDate,
                        currentDailyMinutes
                                + generatedItem.getPlannedDuration()
                );

                ReservedBreak reservedBreak =
                        reserveGeneratedSession(
                                freeSlotsByDay,
                                generatedItem,
                                preferences.getBreakMinutes()
                        );

                if (reservedBreak != null) {
                    reservedBreaks.add(
                            reservedBreak
                    );
                }

                progressMade = true;
            }

        } while (
                progressMade
                        && hasRemainingCandidates(
                        candidates
                )
        );
    }

    private static GeneratedSmartPlanItem tryScheduleOneSession(
            SmartPlanInput input,
            TaskCandidate candidate,
            Map<LocalDate, List<TimeSlot>> freeSlotsByDay,
            Map<LocalDate, Integer> plannedMinutesByDay,
            PlanningPreferences preferences
    ) {

        ZonedDateTime deadline =
                parseDeadline(
                        candidate.getTask()
                );

        /*
         * First pass:
         * try before deadline.
         */
        if (deadline != null
                && deadline.isAfter(
                input.getNow()
        )) {

            GeneratedSmartPlanItem beforeDeadline =
                    findSession(
                            input,
                            candidate,
                            freeSlotsByDay,
                            plannedMinutesByDay,
                            preferences,
                            deadline,
                            true
                    );

            if (beforeDeadline != null) {
                return beforeDeadline;
            }
        }

        /*
         * Second pass:
         * any valid slot inside planning horizon.
         */
        return findSession(
                input,
                candidate,
                freeSlotsByDay,
                plannedMinutesByDay,
                preferences,
                deadline,
                false
        );
    }

    private static GeneratedSmartPlanItem findSession(
            SmartPlanInput input,
            TaskCandidate candidate,
            Map<LocalDate, List<TimeSlot>> freeSlotsByDay,
            Map<LocalDate, Integer> plannedMinutesByDay,
            PlanningPreferences preferences,
            ZonedDateTime deadline,
            boolean requireBeforeDeadline
    ) {

        LocalDate date =
                input.getPeriodStart();

        while (!date.isAfter(
                input.getPeriodEnd()
        )) {

            List<TimeSlot> slots =
                    freeSlotsByDay.get(
                            date
                    );

            if (slots == null
                    || slots.isEmpty()) {

                date =
                        date.plusDays(1);

                continue;
            }

            int alreadyPlannedToday =
                    plannedMinutesByDay.getOrDefault(
                            date,
                            0
                    );

            int dailyRemaining =
                    preferences.getMaxDailyMinutes()
                            - alreadyPlannedToday;

            if (dailyRemaining
                    < SmartPlanConstants.GRID_MINUTES) {

                date =
                        date.plusDays(1);

                continue;
            }

            for (TimeSlot slot : slots) {

                if (slot == null
                        || !slot.isValid()) {

                    continue;
                }

                ZonedDateTime sessionStart =
                        roundUpToGrid(
                                slot.getStart()
                        );

                if (sessionStart == null
                        || !sessionStart.isBefore(
                        slot.getEnd()
                )) {
                    continue;
                }

                int slotMinutes =
                        (int) Duration.between(
                                sessionStart,
                                slot.getEnd()
                        ).toMinutes();

                if (slotMinutes <= 0) {
                    continue;
                }

                int sessionMinutes =
                        determineSessionMinutes(
                                candidate,
                                slotMinutes,
                                dailyRemaining,
                                preferences
                        );

                if (sessionMinutes <= 0) {
                    continue;
                }

                ZonedDateTime sessionEnd =
                        sessionStart.plusMinutes(
                                sessionMinutes
                        );

                if (requireBeforeDeadline
                        && deadline != null
                        && sessionEnd.isAfter(
                        deadline
                )) {

                    int minutesUntilDeadline =
                            (int) Duration.between(
                                    sessionStart,
                                    deadline
                            ).toMinutes();

                    minutesUntilDeadline =
                            floorToGrid(
                                    minutesUntilDeadline
                            );

                    sessionMinutes =
                            determineSessionMinutes(
                                    candidate,
                                    minutesUntilDeadline,
                                    dailyRemaining,
                                    preferences
                            );

                    if (sessionMinutes <= 0) {
                        continue;
                    }

                    sessionEnd =
                            sessionStart.plusMinutes(
                                    sessionMinutes
                            );
                }

                if (requireBeforeDeadline
                        && deadline != null
                        && sessionEnd.isAfter(
                        deadline
                )) {

                    continue;
                }

                if (!SmartPlanValidator.isSessionValid(
                        sessionStart,
                        sessionEnd,
                        input.getNow(),
                        input.getPeriodStart(),
                        input.getPeriodEnd(),
                        input.getAvailability(),
                        input.getEvents(),
                        input.getExistingItems(),
                        alreadyPlannedToday,
                        preferences.getMaxDailyMinutes()
                )) {

                    continue;
                }

                return new GeneratedSmartPlanItem(
                        candidate
                                .getTask()
                                .getId(),
                        sessionStart,
                        sessionEnd,
                        sessionMinutes
                );
            }

            date =
                    date.plusDays(1);
        }

        return null;
    }

    private static int determineSessionMinutes(
            TaskCandidate candidate,
            int slotMinutes,
            int dailyRemaining,
            PlanningPreferences preferences
    ) {

        if (candidate == null
                || preferences == null) {

            return 0;
        }

        int remaining =
                candidate
                        .getRemainingPlanningMinutes();

        if (remaining <= 0
                || slotMinutes <= 0
                || dailyRemaining <= 0) {

            return 0;
        }

        int maximumPossible =
                Math.min(
                        remaining,
                        Math.min(
                                slotMinutes,
                                dailyRemaining
                        )
                );

        maximumPossible =
                floorToGrid(
                        maximumPossible
                );

        if (maximumPossible <= 0) {
            return 0;
        }

        /*
         * Tasks whose entire remaining duration
         * is shorter than the normal minimum
         * session may still be scheduled.
         */
        if (remaining
                < SmartPlanConstants.MIN_SESSION_MINUTES) {

            return maximumPossible >= remaining
                    ? remaining
                    : 0;
        }

        if (maximumPossible
                < SmartPlanConstants.MIN_SESSION_MINUTES) {

            return 0;
        }

        int preferred =
                roundUpToGrid(
                        preferences
                                .getPreferredSessionMinutes()
                );

        int sessionMinutes =
                Math.min(
                        preferred,
                        maximumPossible
                );

        /*
         * Avoid creating a tiny remainder.
         *
         * Example:
         * 75 remaining, preferred 60
         * -> 45 + 30 instead of 60 + 15.
         */
        int remainingAfterSession =
                remaining
                        - sessionMinutes;

        if (remainingAfterSession > 0
                && remainingAfterSession
                < SmartPlanConstants.MIN_SESSION_MINUTES) {

            int adjusted =
                    remaining
                            - SmartPlanConstants.MIN_SESSION_MINUTES;

            adjusted =
                    floorToGrid(
                            adjusted
                    );

            if (adjusted
                    >= SmartPlanConstants.MIN_SESSION_MINUTES
                    && adjusted <= maximumPossible) {

                sessionMinutes =
                        adjusted;
            }
        }

        return sessionMinutes;
    }

    private static Map<LocalDate, List<TimeSlot>> buildFreeSlots(
            SmartPlanInput input
    ) {

        Map<LocalDate, List<TimeSlot>> result =
                new HashMap<>();

        LocalDate date =
                input.getPeriodStart();

        while (!date.isAfter(
                input.getPeriodEnd()
        )) {

            UserAvailability availability =
                    findAvailabilityForDate(
                            input.getAvailability(),
                            date
                    );

            if (availability == null
                    || !availability.isEnabled()) {

                result.put(
                        date,
                        new ArrayList<>()
                );

                date =
                        date.plusDays(1);

                continue;
            }

            List<TimeSlot> slots =
                    TimeSlotCalculator.calculateFreeSlots(
                            date,
                            availability,
                            input.getEvents(),
                            input.getNow()
                    );

            result.put(
                    date,
                    new ArrayList<>(
                            slots
                    )
            );

            date =
                    date.plusDays(1);
        }

        return result;
    }

    private static UserAvailability findAvailabilityForDate(
            List<UserAvailability> availabilityList,
            LocalDate date
    ) {

        if (availabilityList == null
                || date == null) {

            return null;
        }

        String dayName =
                date
                        .getDayOfWeek()
                        .name();

        for (UserAvailability availability
                : availabilityList) {

            if (availability == null) {
                continue;
            }

            if (dayName.equals(
                    availability.getDayOfWeek()
            )) {

                return availability;
            }
        }

        return null;
    }

    /*
     * Reserves the generated session and, where
     * possible, the configured break after it.
     *
     * Returns the actual reserved break interval.
     * The interval may later be restored because
     * breaks are a soft preference.
     */
    private static ReservedBreak reserveGeneratedSession(
            Map<LocalDate, List<TimeSlot>> freeSlotsByDay,
            GeneratedSmartPlanItem item,
            int preferredBreakMinutes
    ) {

        if (item == null
                || item.getPlannedStart() == null
                || item.getPlannedEnd() == null) {

            return null;
        }

        LocalDate date =
                item
                        .getPlannedStart()
                        .toLocalDate();

        List<TimeSlot> slots =
                freeSlotsByDay.get(
                        date
                );

        if (slots == null
                || slots.isEmpty()) {

            return null;
        }

        ZonedDateTime sessionStart =
                item.getPlannedStart();

        ZonedDateTime sessionEnd =
                item.getPlannedEnd();

        int breakMinutes =
                Math.max(
                        0,
                        preferredBreakMinutes
                );

        /*
         * Determine how much of the requested
         * break actually lies inside the free
         * slot containing the session.
         */
        ZonedDateTime actualBreakEnd =
                sessionEnd;

        if (breakMinutes > 0) {

            for (TimeSlot slot : slots) {

                if (slot == null
                        || !slot.isValid()) {

                    continue;
                }

                boolean containsSession =
                        !sessionStart.isBefore(
                                slot.getStart()
                        )
                                && !sessionEnd.isAfter(
                                slot.getEnd()
                        );

                if (!containsSession) {
                    continue;
                }

                ZonedDateTime preferredBreakEnd =
                        sessionEnd.plusMinutes(
                                breakMinutes
                        );

                actualBreakEnd =
                        preferredBreakEnd.isBefore(
                                slot.getEnd()
                        )
                                ? preferredBreakEnd
                                : slot.getEnd();

                break;
            }
        }

        ZonedDateTime blockedEnd =
                actualBreakEnd.isAfter(
                        sessionEnd
                )
                        ? actualBreakEnd
                        : sessionEnd;

        subtractBlockedInterval(
                freeSlotsByDay,
                sessionStart,
                blockedEnd
        );

        if (actualBreakEnd.isAfter(
                sessionEnd
        )) {

            return new ReservedBreak(
                    sessionEnd,
                    actualBreakEnd
            );
        }

        return null;
    }

    /*
     * Break fallback:
     *
     * Restore a previously reserved break only when
     * doing so creates a valid session for the Task.
     *
     * We inspect later breaks first so earlier
     * scheduling decisions remain as stable as possible.
     */
    private static boolean restoreReservedBreakForCandidate(
            SmartPlanInput input,
            TaskCandidate candidate,
            Map<LocalDate, List<TimeSlot>> freeSlotsByDay,
            Map<LocalDate, Integer> plannedMinutesByDay,
            PlanningPreferences preferences,
            List<ReservedBreak> reservedBreaks
    ) {

        if (reservedBreaks == null
                || reservedBreaks.isEmpty()) {

            return false;
        }

        for (int i = reservedBreaks.size() - 1;
             i >= 0;
             i--) {

            ReservedBreak reservedBreak =
                    reservedBreaks.get(i);

            if (reservedBreak == null
                    || reservedBreak.isRestored()) {

                continue;
            }

            addFreeInterval(
                    freeSlotsByDay,
                    reservedBreak.getStart(),
                    reservedBreak.getEnd()
            );

            GeneratedSmartPlanItem possibleSession =
                    tryScheduleOneSession(
                            input,
                            candidate,
                            freeSlotsByDay,
                            plannedMinutesByDay,
                            preferences
                    );

            if (possibleSession != null) {

                reservedBreak.markRestored();

                return true;
            }

            /*
             * Restoring this break did not help,
             * so reserve it again.
             */
            subtractBlockedInterval(
                    freeSlotsByDay,
                    reservedBreak.getStart(),
                    reservedBreak.getEnd()
            );
        }

        return false;
    }

    private static void addFreeInterval(
            Map<LocalDate, List<TimeSlot>> slotsByDay,
            ZonedDateTime freeStart,
            ZonedDateTime freeEnd
    ) {

        if (slotsByDay == null
                || freeStart == null
                || freeEnd == null
                || !freeEnd.isAfter(
                freeStart
        )) {

            return;
        }

        LocalDate date =
                freeStart.toLocalDate();

        List<TimeSlot> slots =
                slotsByDay.get(
                        date
                );

        if (slots == null) {

            slots =
                    new ArrayList<>();

            slotsByDay.put(
                    date,
                    slots
            );
        }

        slots.add(
                new TimeSlot(
                        freeStart,
                        freeEnd
                )
        );

        mergeFreeSlots(
                slots
        );
    }

    private static void mergeFreeSlots(
            List<TimeSlot> slots
    ) {

        if (slots == null
                || slots.size() <= 1) {

            return;
        }

        slots.sort(
                Comparator.comparing(
                        TimeSlot::getStart
                )
        );

        List<TimeSlot> merged =
                new ArrayList<>();

        for (TimeSlot slot : slots) {

            if (slot == null
                    || !slot.isValid()) {

                continue;
            }

            if (merged.isEmpty()) {

                merged.add(
                        slot
                );

                continue;
            }

            TimeSlot last =
                    merged.get(
                            merged.size() - 1
                    );

            /*
             * Merge overlapping or directly
             * adjacent free intervals.
             */
            if (!slot.getStart().isAfter(
                    last.getEnd()
            )) {

                ZonedDateTime mergedEnd =
                        slot.getEnd().isAfter(
                                last.getEnd()
                        )
                                ? slot.getEnd()
                                : last.getEnd();

                merged.set(
                        merged.size() - 1,
                        new TimeSlot(
                                last.getStart(),
                                mergedEnd
                        )
                );

            } else {

                merged.add(
                        slot
                );
            }
        }

        slots.clear();

        slots.addAll(
                merged
        );
    }

    private static void addWarningIfMissing(
            SmartPlanResult result,
            String warning
    ) {

        if (result == null
                || warning == null
                || warning.trim().isEmpty()) {

            return;
        }

        if (result.getWarnings() != null
                && result.getWarnings().contains(
                warning
        )) {

            return;
        }

        result.addWarning(
                warning
        );
    }

    private static void addDeadlineWarnings(
            List<TaskCandidate> candidates,
            SmartPlanResult result
    ) {

        if (candidates == null
                || result == null
                || result.getPlannedItems() == null) {

            return;
        }

        boolean deadlineConflictExists =
                false;

        for (TaskCandidate candidate
                : candidates) {

            if (candidate == null
                    || candidate.getTask() == null) {

                continue;
            }

            ZonedDateTime deadline =
                    parseDeadline(
                            candidate.getTask()
                    );

            if (deadline == null) {
                continue;
            }

            String taskId =
                    candidate
                            .getTask()
                            .getId();

            if (taskId == null) {
                continue;
            }

            boolean taskWasScheduled =
                    false;

            boolean scheduledAfterDeadline =
                    false;

            for (GeneratedSmartPlanItem item
                    : result.getPlannedItems()) {

                if (item == null
                        || !taskId.equals(
                        item.getTaskId()
                )) {

                    continue;
                }

                taskWasScheduled =
                        true;

                if (item.getPlannedEnd() != null
                        && item
                        .getPlannedEnd()
                        .isAfter(
                                deadline
                        )) {

                    scheduledAfterDeadline =
                            true;

                    break;
                }
            }

            /*
             * A Task can also have a deadline
             * conflict if some of its work could
             * not be scheduled at all before the
             * deadline.
             */
            if (candidate
                    .getRemainingPlanningMinutes() > 0) {

                scheduledAfterDeadline =
                        true;
            }

            if ((taskWasScheduled
                    || candidate
                    .getRemainingPlanningMinutes() > 0)
                    && scheduledAfterDeadline) {

                deadlineConflictExists =
                        true;

                break;
            }
        }

        if (deadlineConflictExists) {

            addWarningIfMissing(
                    result,
                    "DEADLINE_CONFLICT"
            );
        }
    }

    private static void addRemainingTasksAsUnscheduled(
            SmartPlanInput input,
            List<TaskCandidate> candidates,
            SmartPlanResult result
    ) {

        Map<LocalDate, List<TimeSlot>> originalFreeSlots =
                buildFreeSlots(
                        input
                );

        Map<LocalDate, List<TimeSlot>> remainingPhysicalSlots =
                buildRemainingPhysicalSlots(
                        input,
                        result
                );

        Map<LocalDate, Integer> plannedMinutesByDay =
                calculateGeneratedMinutesByDay(
                        result
                );

        for (TaskCandidate candidate : candidates) {

            if (candidate
                    .getRemainingPlanningMinutes() <= 0) {

                continue;
            }

            UnscheduledReason reason;

            if (!hasUsablePhysicalSlot(
                    candidate,
                    originalFreeSlots,
                    input.getPreferences()
            )) {

                reason =
                        UnscheduledReason.NO_AVAILABLE_TIME;

            } else if (isBlockedByDailyLimit(
                    candidate,
                    remainingPhysicalSlots,
                    plannedMinutesByDay,
                    input.getPreferences()
            )) {

                reason =
                        UnscheduledReason.DAILY_LIMIT_REACHED;

            } else {

                reason =
                        UnscheduledReason.PLANNING_HORIZON_FULL;
            }

            result.addUnscheduledTask(
                    new UnscheduledTask(
                            candidate.getTask(),
                            reason,
                            candidate
                                    .getRemainingPlanningMinutes()
                    )
            );
        }
    }

    private static Map<LocalDate, Integer>
    calculateGeneratedMinutesByDay(
            SmartPlanResult result
    ) {

        Map<LocalDate, Integer> minutesByDay =
                new HashMap<>();

        if (result == null
                || result.getPlannedItems() == null) {

            return minutesByDay;
        }

        for (GeneratedSmartPlanItem item
                : result.getPlannedItems()) {

            if (item == null
                    || item.getPlannedStart() == null) {

                continue;
            }

            LocalDate date =
                    item
                            .getPlannedStart()
                            .toLocalDate();

            int current =
                    minutesByDay.getOrDefault(
                            date,
                            0
                    );

            minutesByDay.put(
                    date,
                    current
                            + item.getPlannedDuration()
            );
        }

        return minutesByDay;
    }

    private static boolean hasUsablePhysicalSlot(
            TaskCandidate candidate,
            Map<LocalDate, List<TimeSlot>> slotsByDay,
            PlanningPreferences preferences
    ) {

        if (candidate == null
                || slotsByDay == null
                || preferences == null) {

            return false;
        }

        for (List<TimeSlot> slots
                : slotsByDay.values()) {

            if (slots == null) {
                continue;
            }

            for (TimeSlot slot : slots) {

                if (slot == null
                        || !slot.isValid()) {

                    continue;
                }

                ZonedDateTime start =
                        roundUpToGrid(
                                slot.getStart()
                        );

                if (start == null
                        || !start.isBefore(
                        slot.getEnd()
                )) {

                    continue;
                }

                int availableMinutes =
                        (int) Duration.between(
                                start,
                                slot.getEnd()
                        ).toMinutes();

                int sessionMinutes =
                        determineSessionMinutes(
                                candidate,
                                availableMinutes,
                                Integer.MAX_VALUE,
                                preferences
                        );

                if (sessionMinutes > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isBlockedByDailyLimit(
            TaskCandidate candidate,
            Map<LocalDate, List<TimeSlot>> slotsByDay,
            Map<LocalDate, Integer> plannedMinutesByDay,
            PlanningPreferences preferences
    ) {

        if (candidate == null
                || slotsByDay == null
                || preferences == null) {

            return false;
        }

        boolean physicalTimeExists =
                false;

        for (Map.Entry<LocalDate, List<TimeSlot>> entry
                : slotsByDay.entrySet()) {

            LocalDate date =
                    entry.getKey();

            List<TimeSlot> slots =
                    entry.getValue();

            if (slots == null
                    || slots.isEmpty()) {

                continue;
            }

            int alreadyPlanned =
                    plannedMinutesByDay.getOrDefault(
                            date,
                            0
                    );

            int dailyRemaining =
                    preferences.getMaxDailyMinutes()
                            - alreadyPlanned;

            for (TimeSlot slot : slots) {

                if (slot == null
                        || !slot.isValid()) {

                    continue;
                }

                ZonedDateTime start =
                        roundUpToGrid(
                                slot.getStart()
                        );

                if (start == null
                        || !start.isBefore(
                        slot.getEnd()
                )) {

                    continue;
                }

                int slotMinutes =
                        (int) Duration.between(
                                start,
                                slot.getEnd()
                        ).toMinutes();

                int withoutDailyLimit =
                        determineSessionMinutes(
                                candidate,
                                slotMinutes,
                                Integer.MAX_VALUE,
                                preferences
                        );

                if (withoutDailyLimit <= 0) {
                    continue;
                }

                physicalTimeExists =
                        true;

                int withDailyLimit =
                        determineSessionMinutes(
                                candidate,
                                slotMinutes,
                                dailyRemaining,
                                preferences
                        );

                if (withDailyLimit > 0) {
                    return false;
                }
            }
        }

        return physicalTimeExists;
    }

    private static Map<LocalDate, List<TimeSlot>>
    buildRemainingPhysicalSlots(
            SmartPlanInput input,
            SmartPlanResult result
    ) {

        Map<LocalDate, List<TimeSlot>> slotsByDay =
                buildFreeSlots(
                        input
                );

        /*
         * Existing PLANNED sessions occupy
         * physical time.
         */
        if (input.getExistingItems() != null) {

            for (SmartPlanItem item
                    : input.getExistingItems()) {

                if (item == null
                        || item.getStatus()
                        != SmartPlanItemStatus.PLANNED) {

                    continue;
                }

                ZonedDateTime start =
                        DateTimeUtils
                                .parseTimestampToLocalZone(
                                        item.getPlannedStart()
                                );

                ZonedDateTime end =
                        DateTimeUtils
                                .parseTimestampToLocalZone(
                                        item.getPlannedEnd()
                                );

                if (start == null
                        || end == null
                        || !end.isAfter(
                        start
                )) {

                    continue;
                }

                subtractBlockedInterval(
                        slotsByDay,
                        start,
                        end
                );
            }
        }

        /*
         * Generated sessions also occupy time.
         * Breaks are intentionally not included
         * because they are soft preferences.
         */
        if (result != null
                && result.getPlannedItems() != null) {

            for (GeneratedSmartPlanItem item
                    : result.getPlannedItems()) {

                if (item == null
                        || item.getPlannedStart() == null
                        || item.getPlannedEnd() == null) {

                    continue;
                }

                subtractBlockedInterval(
                        slotsByDay,
                        item.getPlannedStart(),
                        item.getPlannedEnd()
                );
            }
        }

        return slotsByDay;
    }

    private static void subtractBlockedInterval(
            Map<LocalDate, List<TimeSlot>> slotsByDay,
            ZonedDateTime blockedStart,
            ZonedDateTime blockedEnd
    ) {

        if (slotsByDay == null
                || blockedStart == null
                || blockedEnd == null
                || !blockedEnd.isAfter(
                blockedStart
        )) {

            return;
        }

        LocalDate date =
                blockedStart.toLocalDate();

        List<TimeSlot> slots =
                slotsByDay.get(
                        date
                );

        if (slots == null
                || slots.isEmpty()) {

            return;
        }

        List<TimeSlot> updated =
                new ArrayList<>();

        for (TimeSlot slot : slots) {

            if (slot == null
                    || !slot.isValid()) {

                continue;
            }

            ZonedDateTime slotStart =
                    slot.getStart();

            ZonedDateTime slotEnd =
                    slot.getEnd();

            if (!overlaps(
                    slotStart,
                    slotEnd,
                    blockedStart,
                    blockedEnd
            )) {

                updated.add(
                        slot
                );

                continue;
            }

            if (blockedStart.isAfter(
                    slotStart
            )) {

                updated.add(
                        new TimeSlot(
                                slotStart,
                                blockedStart
                        )
                );
            }

            if (blockedEnd.isBefore(
                    slotEnd
            )) {

                updated.add(
                        new TimeSlot(
                                blockedEnd,
                                slotEnd
                        )
                );
            }
        }

        updated.sort(
                Comparator.comparing(
                        TimeSlot::getStart
                )
        );

        slotsByDay.put(
                date,
                updated
        );
    }

    private static boolean overlaps(
            ZonedDateTime firstStart,
            ZonedDateTime firstEnd,
            ZonedDateTime secondStart,
            ZonedDateTime secondEnd
    ) {

        return firstStart.isBefore(
                secondEnd
        )
                && firstEnd.isAfter(
                secondStart
        );
    }

    private static boolean hasRemainingCandidates(
            List<TaskCandidate> candidates
    ) {

        for (TaskCandidate candidate : candidates) {

            if (candidate
                    .getRemainingPlanningMinutes() > 0) {

                return true;
            }
        }

        return false;
    }

    private static List<TaskCandidate> prepareTaskCandidates(
            SmartPlanInput input,
            SmartPlanResult result
    ) {

        List<TaskCandidate> candidates =
                new ArrayList<>();

        if (input.getTasks() == null
                || input.getTasks().isEmpty()) {

            return candidates;
        }

        Map<String, Integer> completedMinutesByTask =
                calculateCompletedMinutes(
                        input.getExistingItems()
                );

        for (Task task : input.getTasks()) {

            if (!isEligibleTask(
                    task
            )) {

                continue;
            }

            Integer estimatedDuration =
                    task.getEstimatedDuration();

            if (estimatedDuration == null
                    || estimatedDuration <= 0) {

                result.addUnscheduledTask(
                        new UnscheduledTask(
                                task,
                                UnscheduledReason.MISSING_DURATION,
                                0
                        )
                );

                continue;
            }

            int completedMinutes =
                    completedMinutesByTask.getOrDefault(
                            task.getId(),
                            0
                    );

            int remainingRealMinutes =
                    estimatedDuration
                            - completedMinutes;

            if (remainingRealMinutes <= 0) {
                continue;
            }

            int planningMinutes =
                    roundUpToGrid(
                            remainingRealMinutes
                    );

            int score =
                    TaskScorer.calculateScore(
                            task,
                            input.getNow()
                    );

            candidates.add(
                    new TaskCandidate(
                            task,
                            remainingRealMinutes,
                            planningMinutes,
                            score
                    )
            );
        }

        sortCandidates(
                candidates
        );

        return candidates;
    }

    private static boolean isEligibleTask(
            Task task
    ) {

        if (task == null) {
            return false;
        }

        TaskStatus status =
                task.getStatus();

        return status == TaskStatus.TO_DO
                || status == TaskStatus.IN_PROGRESS;
    }

    private static Map<String, Integer> calculateCompletedMinutes(
            List<SmartPlanItem> existingItems
    ) {

        Map<String, Integer> completedMinutes =
                new HashMap<>();

        if (existingItems == null
                || existingItems.isEmpty()) {

            return completedMinutes;
        }

        for (SmartPlanItem item : existingItems) {

            if (item == null) {
                continue;
            }

            /*
             * Only COMPLETED sessions reduce
             * remaining Task duration.
             */
            if (item.getStatus()
                    != SmartPlanItemStatus.COMPLETED) {

                continue;
            }

            String taskId =
                    item.getTaskId();

            if (taskId == null
                    || taskId.trim().isEmpty()) {

                continue;
            }

            int duration =
                    item.getPlannedDuration();

            if (duration <= 0) {
                continue;
            }

            int current =
                    completedMinutes.getOrDefault(
                            taskId,
                            0
                    );

            completedMinutes.put(
                    taskId,
                    current + duration
            );
        }

        return completedMinutes;
    }

    private static int roundUpToGrid(
            int minutes
    ) {

        if (minutes <= 0) {
            return 0;
        }

        int grid =
                SmartPlanConstants.GRID_MINUTES;

        int remainder =
                minutes % grid;

        if (remainder == 0) {
            return minutes;
        }

        return minutes
                + (grid - remainder);
    }

    private static int floorToGrid(
            int minutes
    ) {

        if (minutes <= 0) {
            return 0;
        }

        int grid =
                SmartPlanConstants.GRID_MINUTES;

        return minutes
                - (minutes % grid);
    }

    private static ZonedDateTime roundUpToGrid(
            ZonedDateTime value
    ) {

        if (value == null) {
            return null;
        }

        ZonedDateTime clean =
                value.withSecond(0)
                        .withNano(0);

        int minute =
                clean.getMinute();

        int remainder =
                minute
                        % SmartPlanConstants.GRID_MINUTES;

        if (remainder == 0
                && value.getSecond() == 0
                && value.getNano() == 0) {

            return clean;
        }

        int minutesToAdd =
                SmartPlanConstants.GRID_MINUTES
                        - remainder;

        if (remainder == 0) {

            minutesToAdd =
                    SmartPlanConstants.GRID_MINUTES;
        }

        return clean.plusMinutes(
                minutesToAdd
        );
    }

    private static void sortCandidates(
            List<TaskCandidate> candidates
    ) {

        Comparator<TaskCandidate> scoreComparator =
                Comparator.comparingInt(
                        (TaskCandidate candidate) ->
                                candidate.getScore()
                ).reversed();

        Comparator<TaskCandidate> deadlineComparator =
                Comparator.comparing(
                        (TaskCandidate candidate) ->
                                parseDeadline(
                                        candidate.getTask()
                                ),
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                );

        Comparator<TaskCandidate> priorityComparator =
                Comparator.comparingInt(
                        (TaskCandidate candidate) ->
                                priorityRank(
                                        candidate
                                                .getTask()
                                                .getPriority()
                                )
                ).reversed();

        Comparator<TaskCandidate> importantComparator =
                Comparator.comparing(
                        (TaskCandidate candidate) ->
                                !candidate
                                        .getTask()
                                        .isImportant()
                );

        Comparator<TaskCandidate> createdAtComparator =
                Comparator.comparing(
                        (TaskCandidate candidate) ->
                                parseCreatedAt(
                                        candidate.getTask()
                                ),
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                );

        Comparator<TaskCandidate> idComparator =
                Comparator.comparing(
                        (TaskCandidate candidate) -> {

                            String id =
                                    candidate
                                            .getTask()
                                            .getId();

                            return id == null
                                    ? ""
                                    : id;
                        }
                );

        candidates.sort(
                scoreComparator
                        .thenComparing(
                                deadlineComparator
                        )
                        .thenComparing(
                                priorityComparator
                        )
                        .thenComparing(
                                importantComparator
                        )
                        .thenComparing(
                                createdAtComparator
                        )
                        .thenComparing(
                                idComparator
                        )
        );
    }

    private static ZonedDateTime parseDeadline(
            Task task
    ) {

        if (task == null) {
            return null;
        }

        return DateTimeUtils
                .parseTimestampToLocalZone(
                        task.getDeadline()
                );
    }

    private static ZonedDateTime parseCreatedAt(
            Task task
    ) {

        if (task == null) {
            return null;
        }

        return DateTimeUtils
                .parseTimestampToLocalZone(
                        task.getCreatedAt()
                );
    }

    private static int priorityRank(
            TaskPriority priority
    ) {

        if (priority == null) {
            return 0;
        }

        switch (priority) {

            case HIGH:
                return 3;

            case MEDIUM:
                return 2;

            case LOW:
                return 1;

            default:
                return 0;
        }
    }

    /*
     * Internal scheduling representation.
     */
    private static class TaskCandidate {

        private final Task task;

        private final int remainingRealMinutes;

        private int remainingPlanningMinutes;

        private final int score;

        private TaskCandidate(
                Task task,
                int remainingRealMinutes,
                int remainingPlanningMinutes,
                int score
        ) {

            this.task =
                    task;

            this.remainingRealMinutes =
                    remainingRealMinutes;

            this.remainingPlanningMinutes =
                    remainingPlanningMinutes;

            this.score =
                    score;
        }

        public Task getTask() {
            return task;
        }

        public int getRemainingRealMinutes() {
            return remainingRealMinutes;
        }

        public int getRemainingPlanningMinutes() {
            return remainingPlanningMinutes;
        }

        public int getScore() {
            return score;
        }

        public void subtractPlanningMinutes(
                int minutes
        ) {

            remainingPlanningMinutes =
                    Math.max(
                            0,
                            remainingPlanningMinutes
                                    - minutes
                    );
        }
    }

    /*
     * Internal representation of a break that
     * was reserved as a soft preference.
     */
    private static class ReservedBreak {

        private final ZonedDateTime start;

        private final ZonedDateTime end;

        private boolean restored;

        private ReservedBreak(
                ZonedDateTime start,
                ZonedDateTime end
        ) {

            this.start =
                    start;

            this.end =
                    end;

            this.restored =
                    false;
        }

        public ZonedDateTime getStart() {
            return start;
        }

        public ZonedDateTime getEnd() {
            return end;
        }

        public boolean isRestored() {
            return restored;
        }

        public void markRestored() {
            restored =
                    true;
        }
    }
}