package com.smartplanner.app.api;

import com.smartplanner.app.models.NotificationPreferences;
import com.smartplanner.app.models.NotificationPreferencesRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface NotificationPreferencesApi {

    @GET("rest/v1/notification_preferences")
    Call<List<NotificationPreferences>> getPreferences(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("limit") int limit
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/notification_preferences")
    Call<List<NotificationPreferences>> updatePreferences(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Body NotificationPreferencesRequest request
    );
}