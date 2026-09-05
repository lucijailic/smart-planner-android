package com.smartplanner.app.models.enums;

public enum ReminderType {

    NONE(0),
    TEN_MINUTES(10),
    THIRTY_MINUTES(30),
    ONE_HOUR(60),
    ONE_DAY(1440);

    private final int minutesBefore;

    ReminderType(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }
}