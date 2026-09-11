package com.smartplanner.app.models;

import java.util.Date;

public class ImportantItem {

    public enum Type {
        TASK,
        EVENT
    }

    private final Type type;
    private final Task task;
    private final Event event;
    private final Date sortDate;

    private ImportantItem(
            Type type,
            Task task,
            Event event,
            Date sortDate
    ) {

        this.type = type;
        this.task = task;
        this.event = event;
        this.sortDate = sortDate;
    }

    public static ImportantItem fromTask(
            Task task,
            Date sortDate
    ) {

        return new ImportantItem(
                Type.TASK,
                task,
                null,
                sortDate
        );
    }

    public static ImportantItem fromEvent(
            Event event,
            Date sortDate
    ) {

        return new ImportantItem(
                Type.EVENT,
                null,
                event,
                sortDate
        );
    }

    public Type getType() {
        return type;
    }

    public Task getTask() {
        return task;
    }

    public Event getEvent() {
        return event;
    }

    public Date getSortDate() {
        return sortDate;
    }

    public String getId() {

        if (type == Type.TASK
                && task != null) {

            return task.getId();
        }

        if (type == Type.EVENT
                && event != null) {

            return event.getId();
        }

        return null;
    }

    public String getTitle() {

        if (type == Type.TASK
                && task != null) {

            return task.getTitle();
        }

        if (type == Type.EVENT
                && event != null) {

            return event.getTitle();
        }

        return null;
    }
}