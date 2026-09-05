package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class TaskImportantRequest {

    @SerializedName("is_important")
    private final boolean important;

    public TaskImportantRequest(boolean important) {
        this.important = important;
    }
}