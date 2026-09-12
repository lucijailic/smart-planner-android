package com.smartplanner.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver
        extends BroadcastReceiver {

    public static final String EXTRA_NOTIFICATION_ID =
            "notification_id";

    public static final String EXTRA_TITLE =
            "notification_title";

    public static final String EXTRA_MESSAGE =
            "notification_message";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        if (context == null
                || intent == null) {

            return;
        }

        if (!NotificationSettingsManager
                .areNotificationsEnabled(context)) {

            return;
        }

        int notificationId =
                intent.getIntExtra(
                        EXTRA_NOTIFICATION_ID,
                        0
                );

        String title =
                intent.getStringExtra(
                        EXTRA_TITLE
                );

        String message =
                intent.getStringExtra(
                        EXTRA_MESSAGE
                );

        if (title == null
                || title.trim().isEmpty()) {

            title =
                    "Smart Planner";
        }

        if (message == null
                || message.trim().isEmpty()) {

            message =
                    "You have an upcoming reminder.";
        }

        NotificationHelper.showNotification(
                context,
                notificationId,
                title,
                message
        );
    }
}