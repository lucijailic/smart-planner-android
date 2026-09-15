package com.smartplanner.app.models.auth;

import com.google.gson.annotations.SerializedName;

public class GoogleIdTokenRequest {

    @SerializedName("provider")
    private final String provider;

    @SerializedName("id_token")
    private final String idToken;

    public GoogleIdTokenRequest(String idToken) {
        this.provider = "google";
        this.idToken = idToken;
    }
}