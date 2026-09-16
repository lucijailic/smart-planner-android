package com.smartplanner.app.api;

import com.smartplanner.app.models.CreateSmartPlanRequest;
import com.smartplanner.app.models.RegenerateSmartPlanRequest;
import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.SmartPlanItemRequest;

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

public interface SmartPlanApi {

    @GET("rest/v1/smart_plans")
    Call<List<SmartPlan>> getCurrentSmartPlan(
            @Query("user_id") String userFilter,
            @Query("status") String statusFilter,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );

    @GET("rest/v1/smart_plans")
    Call<List<SmartPlan>> getSmartPlan(
            @Query("id") String idFilter,
            @Query("select") String select
    );


    @Headers("Prefer: return=representation")
    @POST("rest/v1/smart_plans")
    Call<List<SmartPlan>> createSmartPlan(
            @Body RequestBody requestBody,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/smart_plans")
    Call<List<SmartPlan>> updateSmartPlan(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );



    @POST("rest/v1/rpc/create_smart_plan")
    Call<List<SmartPlan>> createSmartPlanAtomic(
            @Body CreateSmartPlanRequest request
    );


    // =========================================================
    // ATOMIC REGENERATE RPC
    // =========================================================

    @POST("rest/v1/rpc/regenerate_smart_plan")
    Call<List<SmartPlan>> regenerateSmartPlan(
            @Body RegenerateSmartPlanRequest request
    );


    // =========================================================
    // SMART PLAN ITEMS
    // =========================================================

    @GET("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> getSmartPlanItems(
            @Query("smart_plan_id") String smartPlanFilter,
            @Query("select") String select,
            @Query("order") String order
    );


    @GET("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> getUserSmartPlanItems(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("order") String order
    );


    @GET("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> getSmartPlanItem(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> createSmartPlanItem(
            @Body SmartPlanItemRequest request,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> updateSmartPlanItem(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );

    @DELETE("rest/v1/smart_plan_items")
    Call<Void> deleteSmartPlanItem(
            @Query("id") String idFilter
    );
}