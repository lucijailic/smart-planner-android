package com.smartplanner.app.smartplan;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TimeSlot {

    private final ZonedDateTime start;
    private final ZonedDateTime end;

    public TimeSlot(
            ZonedDateTime start,
            ZonedDateTime end
    ) {
        this.start = start;
        this.end = end;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public long getDurationMinutes() {
        return Duration.between(start, end).toMinutes();
    }

    public boolean isValid() {
        return start != null
                && end != null
                && end.isAfter(start);
    }

    public boolean canFit(int minutes) {
        return minutes > 0
                && getDurationMinutes() >= minutes;
    }
}