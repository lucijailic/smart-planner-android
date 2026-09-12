package com.smartplanner.app.notifications;

import android.content.Context;

import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskReminderManager {

    private TaskReminderManager() {
    }

    public static void updateTaskReminder(
            Context context,
            Task task
    ) {

        if (context == null
                || task == null
                || task.getId() == null
                || task.getId().trim().isEmpty()) {

            return;
        }

        int notificationId =
                createNotificationId(
                        task.getId()
                );

        /*
         * Prvo uvijek brišemo eventualni stari alarm.
         *
         * Ovo pokriva Edit Task slučaj kada korisnik
         * promijeni deadline ili reminder.
         */
        NotificationScheduler.cancelNotification(
                context,
                notificationId
        );

        if (task.getStatus()
                == TaskStatus.COMPLETED) {

            return;
        }

        String deadline =
                task.getDeadline();

        ReminderType reminderType =
                task.getReminderType();

        if (deadline == null
                || deadline.trim().isEmpty()) {

            return;
        }

        if (reminderType == null
                || reminderType == ReminderType.NONE) {

            return;
        }

        Date deadlineDate =
                parseSupabaseDate(
                        deadline
                );

        if (deadlineDate == null) {

            return;
        }

        long reminderOffsetMillis =
                reminderType.getMinutesBefore()
                        * 60L
                        * 1000L;

        long triggerAtMillis =
                deadlineDate.getTime()
                        - reminderOffsetMillis;

        if (triggerAtMillis
                <= System.currentTimeMillis()) {

            return;
        }

        String taskTitle =
                task.getTitle();

        if (taskTitle == null
                || taskTitle.trim().isEmpty()) {

            taskTitle =
                    "Upcoming task";
        }

        NotificationScheduler.scheduleNotification(
                context,
                notificationId,
                triggerAtMillis,
                "Task reminder",
                taskTitle
        );
    }

    public static void cancelTaskReminder(
            Context context,
            String taskId
    ) {

        if (context == null
                || taskId == null
                || taskId.trim().isEmpty()) {

            return;
        }

        NotificationScheduler.cancelNotification(
                context,
                createNotificationId(taskId)
        );
    }

    private static int createNotificationId(
            String taskId
    ) {

        return taskId.hashCode();
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