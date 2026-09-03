package com.smartplanner.app.api;

import com.smartplanner.app.models.UpdateProfileRequest;
import com.smartplanner.app.models.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ProfileApi {

    @GET("rest/v1/profiles")
    Call<List<UserProfile>> getProfile(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Query("limit") int limit
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/profiles")
    Call<List<UserProfile>> updateProfile(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body UpdateProfileRequest request
    );
}