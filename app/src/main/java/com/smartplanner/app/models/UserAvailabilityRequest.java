package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class UserAvailabilityRequest {

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("day_of_week")
    private final String dayOfWeek;

    @SerializedName("start_time")
    private final String startTime;

    @SerializedName("end_time")
    private final String endTime;

    @SerializedName("is_enabled")
    private final boolean enabled;


    public UserAvailabilityRequest(
            String dayOfWeek,
            String startTime,
            String endTime,
            boolean enabled
    ) {
        this.userId = null;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
    }

    public UserAvailabilityRequest(
            String userId,
            String dayOfWeek,
            String startTime,
            String endTime,
            boolean enabled
    ) {
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
    }


    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isEnabled() {
        return enabled;
    }
}