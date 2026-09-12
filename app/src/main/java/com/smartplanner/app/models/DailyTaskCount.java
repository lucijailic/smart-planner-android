package com.smartplanner.app.models;

public class DailyTaskCount {

    private final String label;
    private final int count;

    public DailyTaskCount(
            String label,
            int count
    ) {

        this.label = label;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }
}