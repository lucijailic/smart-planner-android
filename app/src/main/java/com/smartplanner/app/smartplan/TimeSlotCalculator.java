package com.smartplanner.app.smartplan;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.utils.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TimeSlotCalculator {

    private TimeSlotCalculator() {

    }

    public static List<TimeSlot> calculateFreeSlots(
            LocalDate date,
            UserAvailability availability,
            List<Event> events,
            ZonedDateTime now
    ) {

        List<TimeSlot> freeSlots = new ArrayList<>();

        if (date == null
                || availability == null
                || now == null) {

            return freeSlots;
        }

        if (!availability.isEnabled()) {
            return freeSlots;
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
                || availabilityEnd == null
                || !availabilityEnd.isAfter(availabilityStart)) {

            return freeSlots;
        }

        ZoneId zoneId = ZoneId.systemDefault();

        ZonedDateTime availableStart =
                date.atTime(availabilityStart)
                        .atZone(zoneId);

        ZonedDateTime availableEnd =
                date.atTime(availabilityEnd)
                        .atZone(zoneId);


        if (date.equals(now.toLocalDate())) {

            if (!availableEnd.isAfter(now)) {
                return freeSlots;
            }

            if (availableStart.isBefore(now)) {
                availableStart = now;
            }
        }

        freeSlots.add(
                new TimeSlot(
                        availableStart,
                        availableEnd
                )
        );

        if (events == null || events.isEmpty()) {
            return removeInvalidSlots(freeSlots);
        }

        List<EventInterval> eventIntervals =
                prepareEventIntervals(
                        events,
                        availableStart,
                        availableEnd
                );

        for (EventInterval eventInterval : eventIntervals) {

            freeSlots =
                    subtractInterval(
                            freeSlots,
                            eventInterval.start,
                            eventInterval.end
                    );
        }

        return removeInvalidSlots(freeSlots);
    }

    private static List<EventInterval> prepareEventIntervals(
            List<Event> events,
            ZonedDateTime availableStart,
            ZonedDateTime availableEnd
    ) {

        List<EventInterval> result =
                new ArrayList<>();

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
                    || eventEnd == null
                    || !eventEnd.isAfter(eventStart)) {

                continue;
            }


            if (!overlaps(
                    eventStart,
                    eventEnd,
                    availableStart,
                    availableEnd
            )) {
                continue;
            }


            ZonedDateTime blockedStart =
                    eventStart.isBefore(availableStart)
                            ? availableStart
                            : eventStart;

            ZonedDateTime blockedEnd =
                    eventEnd.isAfter(availableEnd)
                            ? availableEnd
                            : eventEnd;

            result.add(
                    new EventInterval(
                            blockedStart,
                            blockedEnd
                    )
            );
        }

        result.sort(
                Comparator.comparing(
                        interval -> interval.start
                )
        );

        return result;
    }

    private static List<TimeSlot> subtractInterval(
            List<TimeSlot> slots,
            ZonedDateTime blockedStart,
            ZonedDateTime blockedEnd
    ) {

        List<TimeSlot> result =
                new ArrayList<>();

        for (TimeSlot slot : slots) {

            if (slot == null || !slot.isValid()) {
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

                result.add(slot);
                continue;
            }

            if (blockedStart.isAfter(slotStart)) {

                result.add(
                        new TimeSlot(
                                slotStart,
                                blockedStart
                        )
                );
            }

            if (blockedEnd.isBefore(slotEnd)) {

                result.add(
                        new TimeSlot(
                                blockedEnd,
                                slotEnd
                        )
                );
            }
        }

        return result;
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

    private static List<TimeSlot> removeInvalidSlots(
            List<TimeSlot> slots
    ) {

        List<TimeSlot> result =
                new ArrayList<>();

        for (TimeSlot slot : slots) {

            if (slot != null && slot.isValid()) {
                result.add(slot);
            }
        }

        result.sort(
                Comparator.comparing(
                        TimeSlot::getStart
                )
        );

        return result;
    }

    private static class EventInterval {

        private final ZonedDateTime start;
        private final ZonedDateTime end;

        private EventInterval(
                ZonedDateTime start,
                ZonedDateTime end
        ) {
            this.start = start;
            this.end = end;
        }
    }
}