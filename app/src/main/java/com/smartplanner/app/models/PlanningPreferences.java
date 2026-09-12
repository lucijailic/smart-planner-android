package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class PlanningPreferences {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("max_daily_minutes")
    private int maxDailyMinutes;

    @SerializedName("preferred_session_minutes")
    private int preferredSessionMinutes;

    @SerializedName("break_minutes")
    private int breakMinutes;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;


    public PlanningPreferences() {
    }


    public PlanningPreferences(
            String userId,
            int maxDailyMinutes,
            int preferredSessionMinutes,
            int breakMinutes,
            String createdAt,
            String updatedAt
    ) {
        this.userId = userId;
        this.maxDailyMinutes = maxDailyMinutes;
        this.preferredSessionMinutes = preferredSessionMinutes;
        this.breakMinutes = breakMinutes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public int getMaxDailyMinutes() {
        return maxDailyMinutes;
    }

    public void setMaxDailyMinutes(int maxDailyMinutes) {
        this.maxDailyMinutes = maxDailyMinutes;
    }


    public int getPreferredSessionMinutes() {
        return preferredSessionMinutes;
    }

    public void setPreferredSessionMinutes(int preferredSessionMinutes) {
        this.preferredSessionMinutes = preferredSessionMinutes;
    }


    public int getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(int breakMinutes) {
        this.breakMinutes = breakMinutes;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}