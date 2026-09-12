package com.smartplanner.app.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationScheduler {

    private NotificationScheduler() {
    }

    public static void scheduleNotification(
            Context context,
            int notificationId,
            long triggerAtMillis,
            String title,
            String message
    ) {

        if (triggerAtMillis
                <= System.currentTimeMillis()) {

            return;
        }

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent =
                createPendingIntent(
                        context,
                        notificationId,
                        title,
                        message
                );

        /*
         * Ako aplikacija ima dopuštenje za exact alarm,
         * koristimo točno zakazivanje.
         *
         * Ako nema, koristimo setAndAllowWhileIdle(),
         * tako da reminder i dalje radi bez rušenja aplikacije.
         */
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.S) {

            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );

            } else {

                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }

    public static void cancelNotification(
            Context context,
            int notificationId
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Intent intent =
                new Intent(
                        context,
                        NotificationReceiver.class
                );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_NO_CREATE
                                | PendingIntent.FLAG_IMMUTABLE
                );

        if (pendingIntent != null) {

            alarmManager.cancel(
                    pendingIntent
            );

            pendingIntent.cancel();
        }
    }

    private static PendingIntent createPendingIntent(
            Context context,
            int notificationId,
            String title,
            String message
    ) {

        Intent intent =
                new Intent(
                        context,
                        NotificationReceiver.class
                );

        intent.putExtra(
                NotificationReceiver.EXTRA_NOTIFICATION_ID,
                notificationId
        );

        intent.putExtra(
                NotificationReceiver.EXTRA_TITLE,
                title
        );

        intent.putExtra(
                NotificationReceiver.EXTRA_MESSAGE,
                message
        );

        return PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }
}