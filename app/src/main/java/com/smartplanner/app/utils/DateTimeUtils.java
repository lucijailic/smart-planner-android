package com.smartplanner.app.utils;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private DateTimeUtils() {

    }

    public static LocalTime parseTime(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(
                    value.trim(),
                    TIME_FORMATTER
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    public static ZonedDateTime parseTimestamp(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {

            OffsetDateTime offsetDateTime =
                    OffsetDateTime.parse(value.trim());

            return offsetDateTime.toZonedDateTime();

        } catch (Exception ignored) {
            return null;
        }
    }


    public static ZonedDateTime parseTimestampToLocalZone(
            String value
    ) {

        ZonedDateTime dateTime =
                parseTimestamp(value);

        if (dateTime == null) {
            return null;
        }

        return dateTime.withZoneSameInstant(
                ZoneId.systemDefault()
        );
    }
}