package com.smartplanner.app.models;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class CreateSmartPlanRequest {

    @SerializedName("p_period_start")
    private final String periodStart;

    @SerializedName("p_period_end")
    private final String periodEnd;

    @SerializedName("p_items")
    private final JsonArray items;


    public CreateSmartPlanRequest(
            String periodStart,
            String periodEnd,
            JsonArray items
    ) {

        this.periodStart =
                periodStart;

        this.periodEnd =
                periodEnd;

        this.items =
                items;
    }


    public String getPeriodStart() {
        return periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public JsonArray getItems() {
        return items;
    }
}