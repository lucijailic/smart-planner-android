package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SmartPlanMoveRepository {

    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final SmartPlanRepository smartPlanRepository;
    private final EventRepository eventRepository;
    private final UserAvailabilityRepository userAvailabilityRepository;
    private final PlanningPreferencesRepository planningPreferencesRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SmartPlanMoveRepository(
            Context context
    ) {

        Context appContext =
                context.getApplicationContext();

        smartPlanRepository =
                new SmartPlanRepository(
                        appContext
                );

        eventRepository =
                new EventRepository(
                        appContext
                );

        userAvailabilityRepository =
                new UserAvailabilityRepository(
                        appContext
                );

        planningPreferencesRepository =
                new PlanningPreferencesRepository(
                        appContext
                );
    }


    // =========================================================
    // CALLBACK
    // =========================================================

    public interface SmartPlanMoveCallback {

        void onSuccess(
                SmartPlanItem movedItem
        );

        void onError(
                String message
        );
    }


    // =========================================================
    // MOVE SESSION
    // =========================================================

    public void moveSession(
            String smartPlanItemId,
            ZonedDateTime newStart,
            SmartPlanMoveCallback callback
    ) {

        if (callback == null) {
            return;
        }


        // =====================================================
        // BASIC INPUT VALIDATION
        // =====================================================

        if (smartPlanItemId == null
                || smartPlanItemId
                .trim()
                .isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan session."
            );

            return;
        }


        if (newStart == null) {

            callback.onError(
                    "New session time is required."
            );

            return;
        }


        if (!isOnFifteenMinuteGrid(
                newStart
        )) {

            callback.onError(
                    "Session start must use 15-minute intervals."
            );

            return;
        }


        // =====================================================
        // LOAD CURRENT SMART PLAN
        // =====================================================

        smartPlanRepository.getCurrentSmartPlan(
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlan>() {

                    @Override
                    public void onSuccess(
                            SmartPlan currentPlan
                    ) {

                        if (currentPlan == null
                                || currentPlan.getId() == null
                                || currentPlan
                                .getId()
                                .trim()
                                .isEmpty()) {

                            callback.onError(
                                    "No current Smart Plan exists."
                            );

                            return;
                        }

                        loadSmartPlanItems(
                                currentPlan,
                                smartPlanItemId,
                                newStart,
                                callback
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // LOAD CURRENT PLAN ITEMS
    // =========================================================

    private void loadSmartPlanItems(
            SmartPlan currentPlan,
            String smartPlanItemId,
            ZonedDateTime newStart,
            SmartPlanMoveCallback callback
    ) {

        smartPlanRepository.getSmartPlanItems(
                currentPlan.getId(),
                new SmartPlanRepository
                        .SmartPlanCallback<List<SmartPlanItem>>() {

                    @Override
                    public void onSuccess(
                            List<SmartPlanItem> items
                    ) {

                        List<SmartPlanItem> safeItems =
                                items != null
                                        ? items
                                        : new ArrayList<>();


                        SmartPlanItem targetItem =
                                findSmartPlanItem(
                                        safeItems,
                                        smartPlanItemId
                                );


                        if (targetItem == null) {

                            callback.onError(
                                    "Smart Plan session was not found."
                            );

                            return;
                        }


                        if (targetItem.getStatus()
                                != SmartPlanItemStatus.PLANNED) {

                            callback.onError(
                                    "Only planned sessions can be moved."
                            );

                            return;
                        }


                        int duration =
                                targetItem
                                        .getPlannedDuration();


                        if (duration <= 0
                                || duration % 15 != 0) {

                            callback.onError(
                                    "Invalid session duration."
                            );

                            return;
                        }


                        ZonedDateTime newEnd =
                                newStart.plusMinutes(
                                        duration
                                );



                        if (!newStart
                                .toLocalDate()
                                .equals(
                                        newEnd.toLocalDate()
                                )) {

                            callback.onError(
                                    "A Smart Plan session cannot continue into the next day."
                            );

                            return;
                        }


                        if (!isOnFifteenMinuteGrid(
                                newEnd
                        )) {

                            callback.onError(
                                    "Session end must use 15-minute intervals."
                            );

                            return;
                        }


                        validatePlanningHorizon(
                                currentPlan,
                                targetItem,
                                safeItems,
                                newStart,
                                newEnd,
                                callback
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // VALIDATE PLANNING HORIZON
    // =========================================================

    private void validatePlanningHorizon(
            SmartPlan currentPlan,
            SmartPlanItem targetItem,
            List<SmartPlanItem> allItems,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            SmartPlanMoveCallback callback
    ) {

        LocalDate periodStart;
        LocalDate periodEnd;


        try {

            periodStart =
                    LocalDate.parse(
                            currentPlan.getPeriodStart()
                    );

            periodEnd =
                    LocalDate.parse(
                            currentPlan.getPeriodEnd()
                    );

        } catch (Exception exception) {

            callback.onError(
                    "Invalid Smart Plan period."
            );

            return;
        }


        LocalDate newDate =
                newStart.toLocalDate();


        if (newDate.isBefore(
                periodStart
        )
                || newDate.isAfter(
                periodEnd
        )) {

            callback.onError(
                    "Session must stay inside the current Smart Plan period."
            );

            return;
        }



        if (newStart.isBefore(
                ZonedDateTime.now()
        )) {

            callback.onError(
                    "Session cannot be moved into the past."
            );

            return;
        }


        // =====================================================
        // SMART PLAN SESSION OVERLAP
        // =====================================================

        if (hasSmartPlanOverlap(
                targetItem,
                allItems,
                newStart,
                newEnd
        )) {

            callback.onError(
                    "This time overlaps another Smart Plan session."
            );

            return;
        }


        loadEvents(
                currentPlan,
                targetItem,
                allItems,
                newStart,
                newEnd,
                callback
        );
    }


    // =========================================================
    // LOAD EVENTS
    // =========================================================

    private void loadEvents(
            SmartPlan currentPlan,
            SmartPlanItem targetItem,
            List<SmartPlanItem> allItems,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            SmartPlanMoveCallback callback
    ) {

        eventRepository.getEvents(
                new EventRepository
                        .EventCallback<List<Event>>() {

                    @Override
                    public void onSuccess(
                            List<Event> events
                    ) {

                        List<Event> safeEvents =
                                events != null
                                        ? events
                                        : new ArrayList<>();


                        if (hasEventOverlap(
                                safeEvents,
                                newStart,
                                newEnd
                        )) {

                            callback.onError(
                                    "This time conflicts with an event."
                            );

                            return;
                        }


                        loadAvailability(
                                currentPlan,
                                targetItem,
                                allItems,
                                safeEvents,
                                newStart,
                                newEnd,
                                callback
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // LOAD AVAILABILITY
    // =========================================================

    private void loadAvailability(
            SmartPlan currentPlan,
            SmartPlanItem targetItem,
            List<SmartPlanItem> allItems,
            List<Event> events,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            SmartPlanMoveCallback callback
    ) {

        userAvailabilityRepository.getAvailability(
                new UserAvailabilityRepository
                        .UserAvailabilityCallback<
                        List<UserAvailability>>() {

                    @Override
                    public void onSuccess(
                            List<UserAvailability> availability
                    ) {

                        List<UserAvailability> safeAvailability =
                                availability != null
                                        ? availability
                                        : new ArrayList<>();


                        if (!isInsideAvailability(
                                safeAvailability,
                                newStart,
                                newEnd
                        )) {

                            callback.onError(
                                    "This time is outside your availability."
                            );

                            return;
                        }


                        loadPlanningPreferences(
                                targetItem,
                                allItems,
                                newStart,
                                newEnd,
                                callback
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // LOAD PLANNING PREFERENCES
    // =========================================================

    private void loadPlanningPreferences(
            SmartPlanItem targetItem,
            List<SmartPlanItem> allItems,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            SmartPlanMoveCallback callback
    ) {

        planningPreferencesRepository.getCurrentPreferences(
                new PlanningPreferencesRepository
                        .PlanningPreferencesCallback<
                        PlanningPreferences>() {

                    @Override
                    public void onSuccess(
                            PlanningPreferences preferences
                    ) {

                        if (preferences == null) {

                            callback.onError(
                                    "Smart Plan planning preferences are missing."
                            );

                            return;
                        }


                        if (!respectsDailyLimit(
                                targetItem,
                                allItems,
                                newStart,
                                preferences
                        )) {

                            callback.onError(
                                    "Moving this session would exceed your daily planning limit."
                            );

                            return;
                        }


                        persistMove(
                                targetItem,
                                newStart,
                                newEnd,
                                callback
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // PERSIST MOVE
    // =========================================================

    private void persistMove(
            SmartPlanItem targetItem,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            SmartPlanMoveCallback callback
    ) {

        smartPlanRepository.moveSmartPlanItem(
                targetItem.getId(),
                newStart,
                newEnd,
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlanItem>() {

                    @Override
                    public void onSuccess(
                            SmartPlanItem result
                    ) {

                        callback.onSuccess(
                                result
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        callback.onError(
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // FIND SESSION
    // =========================================================

    private SmartPlanItem findSmartPlanItem(
            List<SmartPlanItem> items,
            String smartPlanItemId
    ) {

        if (items == null
                || smartPlanItemId == null) {

            return null;
        }


        for (SmartPlanItem item : items) {

            if (item == null
                    || item.getId() == null) {

                continue;
            }


            if (smartPlanItemId.equals(
                    item.getId()
            )) {

                return item;
            }
        }


        return null;
    }


    // =========================================================
    // SMART PLAN OVERLAP
    // =========================================================

    private boolean hasSmartPlanOverlap(
            SmartPlanItem targetItem,
            List<SmartPlanItem> items,
            ZonedDateTime newStart,
            ZonedDateTime newEnd
    ) {

        if (items == null) {
            return false;
        }


        for (SmartPlanItem item : items) {

            if (item == null) {
                continue;
            }


            if (item.getId() != null
                    && targetItem.getId() != null
                    && item.getId().equals(
                    targetItem.getId()
            )) {

                continue;
            }


            if (item.getStatus()
                    != SmartPlanItemStatus.PLANNED) {

                continue;
            }


            ZonedDateTime existingStart =
                    parseZonedDateTime(
                            item.getPlannedStart()
                    );

            ZonedDateTime existingEnd =
                    parseZonedDateTime(
                            item.getPlannedEnd()
                    );


            if (existingStart == null
                    || existingEnd == null) {

                continue;
            }


            if (intervalsOverlap(
                    newStart,
                    newEnd,
                    existingStart,
                    existingEnd
            )) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // EVENT OVERLAP
    // =========================================================

    private boolean hasEventOverlap(
            List<Event> events,
            ZonedDateTime newStart,
            ZonedDateTime newEnd
    ) {

        if (events == null) {
            return false;
        }


        for (Event event : events) {

            if (event == null) {
                continue;
            }


            ZonedDateTime eventStart =
                    parseZonedDateTime(
                            event.getStartAt()
                    );

            ZonedDateTime eventEnd =
                    parseZonedDateTime(
                            event.getEndAt()
                    );


            if (eventStart == null
                    || eventEnd == null) {

                continue;
            }


            if (intervalsOverlap(
                    newStart,
                    newEnd,
                    eventStart,
                    eventEnd
            )) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // AVAILABILITY
    // =========================================================

    private boolean isInsideAvailability(
            List<UserAvailability> availabilityList,
            ZonedDateTime newStart,
            ZonedDateTime newEnd
    ) {

        if (availabilityList == null
                || availabilityList.isEmpty()) {

            return false;
        }


        String requiredDay =
                newStart
                        .getDayOfWeek()
                        .name();


        LocalTime startTime =
                newStart.toLocalTime();

        LocalTime endTime =
                newEnd.toLocalTime();


        for (UserAvailability availability
                : availabilityList) {

            if (availability == null) {
                continue;
            }


            if (!availability.isEnabled()) {
                continue;
            }


            if (availability.getDayOfWeek()
                    == null) {

                continue;
            }


            String availabilityDay =
                    availability
                            .getDayOfWeek()
                            .toString();


            if (!requiredDay.equalsIgnoreCase(
                    availabilityDay
            )) {

                continue;
            }


            LocalTime availableStart =
                    parseLocalTime(
                            availability.getStartTime()
                    );

            LocalTime availableEnd =
                    parseLocalTime(
                            availability.getEndTime()
                    );


            if (availableStart == null
                    || availableEnd == null) {

                continue;
            }


            boolean startsInside =
                    !startTime.isBefore(
                            availableStart
                    );


            boolean endsInside =
                    !endTime.isAfter(
                            availableEnd
                    );


            if (startsInside
                    && endsInside) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // DAILY LIMIT
    // =========================================================

    private boolean respectsDailyLimit(
            SmartPlanItem targetItem,
            List<SmartPlanItem> allItems,
            ZonedDateTime newStart,
            PlanningPreferences preferences
    ) {

        int maxDailyMinutes =
                preferences.getMaxDailyMinutes();


        if (maxDailyMinutes <= 0) {
            return false;
        }


        LocalDate targetDate =
                newStart.toLocalDate();


        int usedMinutes = 0;


        if (allItems != null) {

            for (SmartPlanItem item : allItems) {

                if (item == null) {
                    continue;
                }



                if (item.getId() != null
                        && targetItem.getId() != null
                        && item.getId().equals(
                        targetItem.getId()
                )) {

                    continue;
                }

                if (item.getStatus()
                        == SmartPlanItemStatus.SKIPPED) {

                    continue;
                }


                ZonedDateTime itemStart =
                        parseZonedDateTime(
                                item.getPlannedStart()
                        );


                if (itemStart == null) {
                    continue;
                }


                if (!targetDate.equals(
                        itemStart.toLocalDate()
                )) {

                    continue;
                }


                if (item.getPlannedDuration() > 0) {

                    usedMinutes +=
                            item.getPlannedDuration();
                }
            }
        }


        int totalAfterMove =
                usedMinutes
                        + targetItem.getPlannedDuration();


        return totalAfterMove
                <= maxDailyMinutes;
    }


    // =========================================================
    // INTERVAL OVERLAP
    // =========================================================

    private boolean intervalsOverlap(
            ZonedDateTime firstStart,
            ZonedDateTime firstEnd,
            ZonedDateTime secondStart,
            ZonedDateTime secondEnd
    ) {

        return firstStart.isBefore(
                secondEnd
        )
                && secondStart.isBefore(
                firstEnd
        );
    }


    // =========================================================
    // 15-MINUTE GRID
    // =========================================================

    private boolean isOnFifteenMinuteGrid(
            ZonedDateTime dateTime
    ) {

        if (dateTime == null) {
            return false;
        }


        return dateTime.getMinute() % 15 == 0
                && dateTime.getSecond() == 0
                && dateTime.getNano() == 0;
    }


    // =========================================================
    // PARSE TIMESTAMPTZ
    // =========================================================

    private ZonedDateTime parseZonedDateTime(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }


        try {

            return ZonedDateTime.parse(
                    value
            );

        } catch (Exception exception) {

            return null;
        }
    }


    // =========================================================
    // PARSE TIME
    // =========================================================

    private LocalTime parseLocalTime(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }


        try {

            return LocalTime.parse(
                    value
            );

        } catch (Exception exception) {

            return null;
        }
    }
}