package com.smartplanner.app.api;

import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.PlanningPreferencesRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PlanningPreferencesApi {

    @GET("rest/v1/planning_preferences")
    Call<List<PlanningPreferences>> getPreferences(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("limit") int limit
    );


    @Headers("Prefer: return=representation")
    @POST("rest/v1/planning_preferences")
    Call<List<PlanningPreferences>> createPreferences(
            @Query("select") String select,
            @Body PlanningPreferencesRequest request
    );


    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/planning_preferences")
    Call<List<PlanningPreferences>> updatePreferences(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Body PlanningPreferencesRequest request
    );
}