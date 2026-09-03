package com.smartplanner.app.models;

import com.google.gson.annotations.SerializedName;

public class CategoryRequest {

    @SerializedName("user_id")
    private final String userId;

    @SerializedName("name")
    private final String name;

    @SerializedName("icon")
    private final String icon;

    @SerializedName("color")
    private final String color;

    public CategoryRequest(
            String userId,
            String name,
            String icon,
            String color
    ) {
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }
}