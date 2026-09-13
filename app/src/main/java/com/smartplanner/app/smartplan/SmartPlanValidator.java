package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;
import com.smartplanner.app.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public final class SmartPlanValidator {

    private SmartPlanValidator() {
        // Prevent instantiation
    }

    public static boolean isInputValid(
            SmartPlanInput input
    ) {

        if (input == null) {
            return false;
        }

        if (input.getPreferences() == null) {
            return false;
        }

        if (input.getAvailability() == null
                || input.getAvailability().isEmpty()) {

            return false;
        }

        if (input.getNow() == null
                || input.getPeriodStart() == null
                || input.getPeriodEnd() == null) {

            return false;
        }

        if (!isPlanningPeriodValid(
                input.getPeriodStart(),
                input.getPeriodEnd()
        )) {
            return false;
        }

        if (!arePreferencesValid(
                input.getPreferences()
        )) {
            return false;
        }

        if (!isAvailabilityValid(
                input.getAvailability()
        )) {
            return false;
        }

        return areEventsValid(
                input.getEvents()
        );
    }

    public static boolean arePreferencesValid(
            PlanningPreferences preferences
    ) {

        if (preferences == null) {
            return false;
        }

        return preferences.getMaxDailyMinutes() > 0
                && preferences.getPreferredSessionMinutes() > 0
                && preferences.getBreakMinutes() >= 0;
    }

    public static boolean isAvailabilityValid(
            List<UserAvailability> availabilityList
    ) {

        if (availabilityList == null
                || availabilityList.isEmpty()) {

            return false;
        }

        for (UserAvailability availability
                : availabilityList) {

            if (availability == null) {
                return false;
            }

            /*
             * Disabled days are valid.
             * Their times are not used for scheduling.
             */
            if (!availability.isEnabled()) {
                continue;
            }

            LocalTime start =
                    DateTimeUtils.parseTime(
                            availability.getStartTime()
                    );

            LocalTime end =
                    DateTimeUtils.parseTime(
                            availability.getEndTime()
                    );

            if (start == null
                    || end == null
                    || !end.isAfter(start)) {

                return false;
            }
        }

        return true;
    }

    public static boolean areEventsValid(
            List<Event> events
    ) {

        if (events == null) {
            return true;
        }

        for (Event event : events) {

            if (event == null) {
                continue;
            }

            ZonedDateTime start =
                    DateTimeUtils.parseTimestampToLocalZone(
                            event.getStartAt()
                    );

            ZonedDateTime end =
                    DateTimeUtils.parseTimestampToLocalZone(
                            event.getEndAt()
                    );

            if (start == null
                    || end == null
                    || !end.isAfter(start)) {

                return false;
            }
        }

        return true;
    }

    public static boolean isPlanningPeriodValid(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        if (periodStart == null
                || periodEnd == null) {

            return false;
        }

        return periodEnd.equals(
                periodStart.plusDays(
                        SmartPlanConstants.PLANNING_HORIZON_DAYS - 1L
                )
        );
    }

    public static boolean isSessionValid(
            ZonedDateTime start,
            ZonedDateTime end,
            ZonedDateTime now,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<UserAvailability> availability,
            List<Event> events,
            List<SmartPlanItem> existingItems,
            int plannedMinutesForDay,
            int maxDailyMinutes
    ) {

        if (start == null
                || end == null
                || now == null
                || periodStart == null
                || periodEnd == null) {

            return false;
        }

        if (!end.isAfter(start)) {
            return false;
        }

        if (start.isBefore(now)) {
            return false;
        }

        if (!isOnGrid(start)
                || !isOnGrid(end)) {

            return false;
        }

        if (!isInsidePlanningHorizon(
                start,
                end,
                periodStart,
                periodEnd
        )) {
            return false;
        }

        if (!isInsideAvailability(
                start,
                end,
                availability
        )) {
            return false;
        }

        if (overlapsAnyEvent(
                start,
                end,
                events
        )) {
            return false;
        }

        if (overlapsExistingItem(
                start,
                end,
                existingItems
        )) {
            return false;
        }

        int sessionMinutes =
                (int) java.time.Duration.between(
                        start,
                        end
                ).toMinutes();

        return plannedMinutesForDay
                + sessionMinutes
                <= maxDailyMinutes;
    }

    public static boolean isOnGrid(
            ZonedDateTime value
    ) {

        if (value == null) {
            return false;
        }

        return value.getSecond() == 0
                && value.getNano() == 0
                && value.getMinute()
                % SmartPlanConstants.GRID_MINUTES == 0;
    }

    private static boolean isInsidePlanningHorizon(
            ZonedDateTime start,
            ZonedDateTime end,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        LocalDate startDate =
                start.toLocalDate();

        LocalDate endDate =
                end.toLocalDate();

        if (startDate.isBefore(periodStart)) {
            return false;
        }

        if (endDate.isAfter(periodEnd)) {
            return false;
        }

        return true;
    }

    private static boolean isInsideAvailability(
            ZonedDateTime start,
            ZonedDateTime end,
            List<UserAvailability> availabilityList
    ) {

        if (availabilityList == null
                || availabilityList.isEmpty()) {

            return false;
        }

        String dayName =
                start.getDayOfWeek().name();

        for (UserAvailability availability
                : availabilityList) {

            if (availability == null
                    || !availability.isEnabled()) {

                continue;
            }

            if (!dayName.equals(
                    availability.getDayOfWeek()
            )) {
                continue;
            }

            LocalTime availabilityStart =
                    DateTimeUtils.parseTime(
                            availability.getStartTime()
                    );

            LocalTime availabilityEnd =
                    DateTimeUtils.parseTime(
                            availability.getEndTime()
                    );

            if (availabilityStart == null
                    || availabilityEnd == null) {

                return false;
            }

            ZonedDateTime allowedStart =
                    start.toLocalDate()
                            .atTime(availabilityStart)
                            .atZone(start.getZone());

            ZonedDateTime allowedEnd =
                    start.toLocalDate()
                            .atTime(availabilityEnd)
                            .atZone(start.getZone());

            return !start.isBefore(allowedStart)
                    && !end.isAfter(allowedEnd);
        }

        return false;
    }

    private static boolean overlapsAnyEvent(
            ZonedDateTime start,
            ZonedDateTime end,
            List<Event> events
    ) {

        if (events == null) {
            return false;
        }

        for (Event event : events) {

            if (event == null) {
                continue;
            }

            ZonedDateTime eventStart =
                    DateTimeUtils.parseTimestampToLocalZone(
                            event.getStartAt()
                    );

            ZonedDateTime eventEnd =
                    DateTimeUtils.parseTimestampToLocalZone(
                            event.getEndAt()
                    );

            if (eventStart == null
                    || eventEnd == null) {

                continue;
            }

            if (overlaps(
                    start,
                    end,
                    eventStart,
                    eventEnd
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean overlapsExistingItem(
            ZonedDateTime start,
            ZonedDateTime end,
            List<SmartPlanItem> existingItems
    ) {

        if (existingItems == null) {
            return false;
        }

        for (SmartPlanItem item : existingItems) {

            if (item == null) {
                continue;
            }

            if (item.getStatus()
                    != SmartPlanItemStatus.PLANNED) {

                continue;
            }

            ZonedDateTime itemStart =
                    DateTimeUtils.parseTimestampToLocalZone(
                            item.getPlannedStart()
                    );

            ZonedDateTime itemEnd =
                    DateTimeUtils.parseTimestampToLocalZone(
                            item.getPlannedEnd()
                    );

            if (itemStart == null
                    || itemEnd == null) {

                continue;
            }

            if (overlaps(
                    start,
                    end,
                    itemStart,
                    itemEnd
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean overlaps(
            ZonedDateTime firstStart,
            ZonedDateTime firstEnd,
            ZonedDateTime secondStart,
            ZonedDateTime secondEnd
    ) {

        return firstStart.isBefore(secondEnd)
                && firstEnd.isAfter(secondStart);
    }
}