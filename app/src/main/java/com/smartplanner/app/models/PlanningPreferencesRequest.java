package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class PlanningPreferencesRequest {

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("max_daily_minutes")
    private final int maxDailyMinutes;

    @SerializedName("preferred_session_minutes")
    private final int preferredSessionMinutes;

    @SerializedName("break_minutes")
    private final int breakMinutes;

    public PlanningPreferencesRequest(
            String userId,
            int maxDailyMinutes,
            int preferredSessionMinutes,
            int breakMinutes
    ) {
        this.userId = userId;
        this.maxDailyMinutes = maxDailyMinutes;
        this.preferredSessionMinutes = preferredSessionMinutes;
        this.breakMinutes = breakMinutes;
    }
}