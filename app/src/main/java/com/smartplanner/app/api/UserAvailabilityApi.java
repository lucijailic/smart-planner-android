package com.smartplanner.app.api;

import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.UserAvailabilityRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserAvailabilityApi {

    @GET("rest/v1/user_availability")
    Call<List<UserAvailability>> getAvailability(
            @Query("user_id") String userFilter,
            @Query("select") String select
    );


    @Headers(
            "Prefer: resolution=merge-duplicates,return=representation"
    )
    @POST("rest/v1/user_availability")
    Call<List<UserAvailability>> saveAvailability(
            @Query("on_conflict") String conflictColumns,
            @Query("select") String select,
            @Body List<UserAvailabilityRequest> requests
    );
}