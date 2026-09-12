package com.smartplanner.app.notifications;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.smartplanner.app.MainActivity;
import com.smartplanner.app.R;

public class NotificationHelper {

    public static final String CHANNEL_ID =
            "smart_planner_reminders";

    private static final String CHANNEL_NAME =
            "Smart Planner Reminders";

    private static final String CHANNEL_DESCRIPTION =
            "Task and event reminder notifications";

    private NotificationHelper() {
    }

    // =========================================================
    // NOTIFICATION CHANNEL
    // =========================================================

    public static void createNotificationChannel(
            Context context
    ) {

        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.O) {

            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );

        channel.setDescription(
                CHANNEL_DESCRIPTION
        );

        channel.enableVibration(
                true
        );

        NotificationManager notificationManager =
                context.getSystemService(
                        NotificationManager.class
                );

        if (notificationManager != null) {

            notificationManager.createNotificationChannel(
                    channel
            );
        }
    }

    // =========================================================
    // SHOW NOTIFICATION
    // =========================================================

    @SuppressLint("MissingPermission")
    public static void showNotification(
            Context context,
            int notificationId,
            String title,
            String message
    ) {

        createNotificationChannel(
                context
        );

        /*
         * Android 13+ requires POST_NOTIFICATIONS permission.
         *
         * Permission is checked before notify() is called.
         */
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }

        // -----------------------------------------------------
        // OPEN APP WHEN NOTIFICATION IS PRESSED
        // -----------------------------------------------------

        Intent intent =
                new Intent(
                        context,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        // -----------------------------------------------------
        // BUILD NOTIFICATION
        // -----------------------------------------------------

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                R.drawable.ic_profile_notifications
                        )
                        .setContentTitle(
                                title
                        )
                        .setContentText(
                                message
                        )
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(
                                                message
                                        )
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(
                                true
                        )
                        .setContentIntent(
                                pendingIntent
                        );

        // -----------------------------------------------------
        // SHOW NOTIFICATION
        // -----------------------------------------------------

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(
                        context
                );

        notificationManager.notify(
                notificationId,
                builder.build()
        );
    }
}