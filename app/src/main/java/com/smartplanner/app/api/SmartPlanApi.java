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

    // =========================================================
    // SMART PLANS
    // =========================================================

    /*
     * Dohvat trenutnog Smart Plana korisnika.
     *
     * Current plan:
     * ACTIVE ili NEEDS_UPDATE
     */
    @GET("rest/v1/smart_plans")
    Call<List<SmartPlan>> getCurrentSmartPlan(
            @Query("user_id") String userFilter,
            @Query("status") String statusFilter,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );


    /*
     * Dohvat jednog Smart Plana po ID-u.
     */
    @GET("rest/v1/smart_plans")
    Call<List<SmartPlan>> getSmartPlan(
            @Query("id") String idFilter,
            @Query("select") String select
    );


    /*
     * Stari REST način kreiranja parent Smart Plana.
     *
     * Ostavljen je radi kompatibilnosti postojećeg koda.
     *
     * Nakon povezivanja atomic Generate flowa više ga
     * nećemo koristiti za standardni Generate.
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/smart_plans")
    Call<List<SmartPlan>> createSmartPlan(
            @Body RequestBody requestBody,
            @Query("select") String select
    );


    /*
     * Update Smart Plana.
     *
     * Koristi se za:
     *
     * ACTIVE -> NEEDS_UPDATE
     * ACTIVE / NEEDS_UPDATE -> ARCHIVED
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/smart_plans")
    Call<List<SmartPlan>> updateSmartPlan(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );


    // =========================================================
    // ATOMIC FIRST GENERATE RPC
    // =========================================================

    /*
     * Supabase PostgreSQL RPC:
     *
     * public.create_smart_plan(
     *      p_period_start,
     *      p_period_end,
     *      p_items
     * )
     *
     * Function atomically:
     *
     * 1. verifies that no current Smart Plan exists
     * 2. creates new ACTIVE Smart Plan
     * 3. creates all generated SmartPlanItems
     *
     * If any operation fails, PostgreSQL rolls back
     * the entire transaction.
     */
    @POST("rest/v1/rpc/create_smart_plan")
    Call<List<SmartPlan>> createSmartPlanAtomic(
            @Body CreateSmartPlanRequest request
    );


    // =========================================================
    // ATOMIC REGENERATE RPC
    // =========================================================

    /*
     * Supabase PostgreSQL RPC:
     *
     * public.regenerate_smart_plan(
     *      p_period_start,
     *      p_period_end,
     *      p_items
     * )
     *
     * Function atomically:
     *
     * 1. archives previous ACTIVE / NEEDS_UPDATE plan
     * 2. creates new ACTIVE plan
     * 3. creates all generated SmartPlanItems
     *
     * If any operation fails, PostgreSQL rolls back
     * the entire transaction.
     */
    @POST("rest/v1/rpc/regenerate_smart_plan")
    Call<List<SmartPlan>> regenerateSmartPlan(
            @Body RegenerateSmartPlanRequest request
    );


    // =========================================================
    // SMART PLAN ITEMS
    // =========================================================

    /*
     * Dohvat svih sessiona jednog Smart Plana.
     */
    @GET("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> getSmartPlanItems(
            @Query("smart_plan_id") String smartPlanFilter,
            @Query("select") String select,
            @Query("order") String order
    );


    /*
     * Dohvat svih SmartPlanItem zapisa korisnika.
     *
     * Potrebno kod Generate / Regenerate flowa jer
     * povijesni COMPLETED sessioni smanjuju
     * preostalo trajanje originalnog Taska.
     */
    @GET("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> getUserSmartPlanItems(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("order") String order
    );


    /*
     * Dohvat jednog SmartPlanItem-a.
     */
    @GET("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> getSmartPlanItem(
            @Query("id") String idFilter,
            @Query("select") String select
    );


    /*
     * Stari REST način kreiranja jednog sessiona.
     *
     * Ostavljamo ga jer ga postojeći repository još uvijek
     * sadrži i može biti koristan za druge operacije.
     *
     * Atomic Generate neće ga koristiti.
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> createSmartPlanItem(
            @Body SmartPlanItemRequest request,
            @Query("select") String select
    );


    /*
     * Update Smart Plan sessiona.
     *
     * Koristi se za:
     *
     * Move
     * Complete
     * Skip
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/smart_plan_items")
    Call<List<SmartPlanItem>> updateSmartPlanItem(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );


    /*
     * Remove Smart Plan session.
     */
    @DELETE("rest/v1/smart_plan_items")
    Call<Void> deleteSmartPlanItem(
            @Query("id") String idFilter
    );
}