package com.smartplanner.app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartplanner.app.models.NotificationPreferences;
import com.smartplanner.app.models.enums.ReminderType;

public class NotificationSettingsManager {

    private static final String PREFS_NAME =
            "notification_settings";

    private static final String KEY_NOTIFICATIONS_ENABLED =
            "notifications_enabled";

    private static final String KEY_DEFAULT_TASK_REMINDER =
            "default_task_reminder";

    private static final String KEY_DEFAULT_EVENT_REMINDER =
            "default_event_reminder";

    /*
     * Fallback vrijednosti koristimo samo ako lokalni
     * cache još nikada nije sinkroniziran sa Supabaseom.
     *
     * One odgovaraju fallback vrijednostima koje već
     * koristi NotificationsActivity.
     */
    private static final boolean DEFAULT_NOTIFICATIONS_ENABLED =
            true;

    private static final String DEFAULT_TASK_REMINDER =
            ReminderType.ONE_DAY.name();

    private static final String DEFAULT_EVENT_REMINDER =
            ReminderType.THIRTY_MINUTES.name();

    private NotificationSettingsManager() {
    }

    private static SharedPreferences getPreferences(
            Context context
    ) {

        return context
                .getApplicationContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );
    }

    // =========================================================
    // SAVE ALL SETTINGS
    // =========================================================

    public static void savePreferences(
            Context context,
            NotificationPreferences preferences
    ) {

        if (context == null
                || preferences == null) {

            return;
        }

        String taskReminder =
                normalizeReminderValue(
                        preferences.getDefaultTaskReminder(),
                        DEFAULT_TASK_REMINDER
                );

        String eventReminder =
                normalizeReminderValue(
                        preferences.getDefaultEventReminder(),
                        DEFAULT_EVENT_REMINDER
                );

        getPreferences(context)
                .edit()
                .putBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        preferences.isNotificationsEnabled()
                )
                .putString(
                        KEY_DEFAULT_TASK_REMINDER,
                        taskReminder
                )
                .putString(
                        KEY_DEFAULT_EVENT_REMINDER,
                        eventReminder
                )
                .apply();
    }

    // =========================================================
    // NOTIFICATIONS ENABLED
    // =========================================================

    public static boolean areNotificationsEnabled(
            Context context
    ) {

        if (context == null) {
            return DEFAULT_NOTIFICATIONS_ENABLED;
        }

        return getPreferences(context)
                .getBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        DEFAULT_NOTIFICATIONS_ENABLED
                );
    }

    // =========================================================
    // DEFAULT TASK REMINDER
    // =========================================================

    public static ReminderType getDefaultTaskReminder(
            Context context
    ) {

        if (context == null) {
            return ReminderType.ONE_DAY;
        }

        String value =
                getPreferences(context)
                        .getString(
                                KEY_DEFAULT_TASK_REMINDER,
                                DEFAULT_TASK_REMINDER
                        );

        return parseReminderType(
                value,
                ReminderType.ONE_DAY
        );
    }

    // =========================================================
    // DEFAULT EVENT REMINDER
    // =========================================================

    public static ReminderType getDefaultEventReminder(
            Context context
    ) {

        if (context == null) {
            return ReminderType.THIRTY_MINUTES;
        }

        String value =
                getPreferences(context)
                        .getString(
                                KEY_DEFAULT_EVENT_REMINDER,
                                DEFAULT_EVENT_REMINDER
                        );

        return parseReminderType(
                value,
                ReminderType.THIRTY_MINUTES
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static String normalizeReminderValue(
            String value,
            String fallback
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return fallback;
        }

        try {

            ReminderType.valueOf(
                    value.trim()
            );

            return value.trim();

        } catch (IllegalArgumentException exception) {

            return fallback;
        }
    }

    private static ReminderType parseReminderType(
            String value,
            ReminderType fallback
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return fallback;
        }

        try {

            return ReminderType.valueOf(
                    value.trim()
            );

        } catch (IllegalArgumentException exception) {

            return fallback;
        }
    }
}