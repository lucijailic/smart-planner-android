package com.smartplanner.app.notifications;

import android.content.Context;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.enums.ReminderType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventReminderManager {

    private EventReminderManager() {
    }

    public static void updateEventReminder(
            Context context,
            Event event
    ) {

        if (context == null
                || event == null
                || event.getId() == null
                || event.getId().trim().isEmpty()) {

            return;
        }

        int notificationId =
                createNotificationId(
                        event.getId()
                );

        /*
         * Uvijek prvo brišemo eventualni stari alarm.
         *
         * Ovo je važno kod Edit Event:
         * - promjena početnog vremena
         * - promjena reminder opcije
         * - promjena na No reminder
         */
        NotificationScheduler.cancelNotification(
                context,
                notificationId
        );

        String startAt =
                event.getStartAt();

        ReminderType reminderType =
                event.getReminderType();

        if (startAt == null
                || startAt.trim().isEmpty()) {

            return;
        }

        if (reminderType == null
                || reminderType == ReminderType.NONE) {

            return;
        }

        Date startDate =
                parseSupabaseDate(
                        startAt
                );

        if (startDate == null) {
            return;
        }

        long reminderOffsetMillis =
                reminderType.getMinutesBefore()
                        * 60L
                        * 1000L;

        long triggerAtMillis =
                startDate.getTime()
                        - reminderOffsetMillis;

        /*
         * Ne zakazujemo reminder čije je vrijeme
         * već prošlo.
         */
        if (triggerAtMillis
                <= System.currentTimeMillis()) {

            return;
        }

        String eventTitle =
                event.getTitle();

        if (eventTitle == null
                || eventTitle.trim().isEmpty()) {

            eventTitle =
                    "Upcoming event";
        }

        NotificationScheduler.scheduleNotification(
                context,
                notificationId,
                triggerAtMillis,
                "Event reminder",
                eventTitle
        );
    }

    public static void cancelEventReminder(
            Context context,
            String eventId
    ) {

        if (context == null
                || eventId == null
                || eventId.trim().isEmpty()) {

            return;
        }

        NotificationScheduler.cancelNotification(
                context,
                createNotificationId(
                        eventId
                )
        );
    }

    private static int createNotificationId(
            String eventId
    ) {

        return (
                "event_" + eventId
        ).hashCode();
    }

    private static Date parseSupabaseDate(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"
        };

        for (String format : formats) {

            try {

                SimpleDateFormat parser =
                        new SimpleDateFormat(
                                format,
                                Locale.US
                        );

                parser.setLenient(
                        false
                );

                return parser.parse(
                        value
                );

            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}