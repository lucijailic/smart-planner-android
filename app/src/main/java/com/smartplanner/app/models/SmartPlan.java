package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;
import com.smartplanner.app.models.enums.SmartPlanStatus;

public class SmartPlan {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("period_start")
    private String periodStart;

    @SerializedName("period_end")
    private String periodEnd;

    @SerializedName("generated_at")
    private String generatedAt;

    @SerializedName("status")
    private SmartPlanStatus status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;


    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public SmartPlanStatus getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}