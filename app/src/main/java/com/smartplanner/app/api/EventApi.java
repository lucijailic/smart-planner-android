package com.smartplanner.app.api;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.EventRequest;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EventApi {

    // =========================================================
    // EVENTS
    // =========================================================

    @GET("rest/v1/events")
    Call<List<Event>> getEvents(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/events")
    Call<List<Event>> getEvent(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/events")
    Call<List<Event>> createEvent(
            @Body EventRequest request,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/events")
    Call<List<Event>> updateEvent(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/events")
    Call<List<Event>> updateImportant(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );

    @DELETE("rest/v1/events")
    Call<Void> deleteEvent(
            @Query("id") String idFilter
    );
}