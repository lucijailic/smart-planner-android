package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class SubtaskCompletedRequest {

    @SerializedName("is_completed")
    private final boolean completed;

    public SubtaskCompletedRequest(
            boolean completed
    ) {

        this.completed = completed;
    }
}