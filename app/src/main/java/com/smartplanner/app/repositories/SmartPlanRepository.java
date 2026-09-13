package com.smartplanner.app.repositories;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.SmartPlanApi;
import com.smartplanner.app.models.CreateSmartPlanRequest;
import com.smartplanner.app.models.RegenerateSmartPlanRequest;
import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.SmartPlanItemRequest;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;
import com.smartplanner.app.models.enums.SmartPlanStatus;
import com.smartplanner.app.smartplan.GeneratedSmartPlanItem;
import com.smartplanner.app.storage.SessionManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartPlanRepository {

    // =========================================================
    // SELECTS
    // =========================================================

    private static final String SMART_PLAN_SELECT =
            "id,user_id,period_start,period_end,generated_at,status," +
                    "created_at,updated_at";

    private static final String SMART_PLAN_ITEM_SELECT =
            "id,smart_plan_id,user_id,task_id,planned_start,planned_end," +
                    "planned_duration,status,is_manually_adjusted," +
                    "completed_at,created_at,updated_at";

    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse(
                    "application/json; charset=utf-8"
            );


    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final SmartPlanApi smartPlanApi;

    private final SessionManager sessionManager;


    public SmartPlanRepository(
            Context context
    ) {

        Context appContext =
                context.getApplicationContext();

        smartPlanApi =
                ApiClient
                        .getClient(appContext)
                        .create(
                                SmartPlanApi.class
                        );

        sessionManager =
                SessionManager.getInstance(
                        appContext
                );
    }


    // =========================================================
    // CALLBACK
    // =========================================================

    public interface SmartPlanCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }


    // =========================================================
    // GET CURRENT SMART PLAN
    // =========================================================

    public void getCurrentSmartPlan(
            SmartPlanCallback<SmartPlan> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (!hasValidUser(
                userId,
                callback
        )) {
            return;
        }

        smartPlanApi
                .getCurrentSmartPlan(
                        "eq." + userId,
                        "in.(ACTIVE,NEEDS_UPDATE)",
                        SMART_PLAN_SELECT,
                        "generated_at.desc",
                        1
                )
                .enqueue(
                        new Callback<List<SmartPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlan>> call,
                                    Response<List<SmartPlan>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load Smart Plan."
                                    );

                                    return;
                                }

                                List<SmartPlan> plans =
                                        response.body();

                                if (plans == null
                                        || plans.isEmpty()) {

                                    callback.onSuccess(
                                            null
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        plans.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlan>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // GET SMART PLAN
    // =========================================================

    public void getSmartPlan(
            String smartPlanId,
            SmartPlanCallback<SmartPlan> callback
    ) {

        if (smartPlanId == null
                || smartPlanId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan."
            );

            return;
        }

        smartPlanApi
                .getSmartPlan(
                        "eq." + smartPlanId,
                        SMART_PLAN_SELECT
                )
                .enqueue(
                        new Callback<List<SmartPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlan>> call,
                                    Response<List<SmartPlan>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load Smart Plan."
                                    );

                                    return;
                                }

                                List<SmartPlan> plans =
                                        response.body();

                                if (plans == null
                                        || plans.isEmpty()) {

                                    callback.onError(
                                            "Smart Plan not found."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        plans.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlan>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // GET SMART PLAN ITEMS
    // =========================================================

    public void getSmartPlanItems(
            String smartPlanId,
            SmartPlanCallback<List<SmartPlanItem>> callback
    ) {

        if (smartPlanId == null
                || smartPlanId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan."
            );

            return;
        }

        smartPlanApi
                .getSmartPlanItems(
                        "eq." + smartPlanId,
                        SMART_PLAN_ITEM_SELECT,
                        "planned_start.asc"
                )
                .enqueue(
                        new Callback<List<SmartPlanItem>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlanItem>> call,
                                    Response<List<SmartPlanItem>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load Smart Plan sessions."
                                    );

                                    return;
                                }

                                List<SmartPlanItem> items =
                                        response.body();

                                if (items == null) {

                                    items =
                                            new ArrayList<>();
                                }

                                callback.onSuccess(
                                        items
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlanItem>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // GET ALL USER SMART PLAN ITEMS
    // =========================================================

    public void getUserSmartPlanItems(
            SmartPlanCallback<List<SmartPlanItem>> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (!hasValidUser(
                userId,
                callback
        )) {
            return;
        }

        smartPlanApi
                .getUserSmartPlanItems(
                        "eq." + userId,
                        SMART_PLAN_ITEM_SELECT,
                        "planned_start.asc"
                )
                .enqueue(
                        new Callback<List<SmartPlanItem>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlanItem>> call,
                                    Response<List<SmartPlanItem>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load Smart Plan history."
                                    );

                                    return;
                                }

                                List<SmartPlanItem> items =
                                        response.body();

                                if (items == null) {

                                    items =
                                            new ArrayList<>();
                                }

                                callback.onSuccess(
                                        items
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlanItem>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // CREATE FIRST SMART PLAN - OLD REST METHOD
    //
    // Kept for compatibility.
    //
    // Standard Generate flow will later use
    // createSmartPlanAtomically().
    // =========================================================

    public void createSmartPlan(
            LocalDate periodStart,
            LocalDate periodEnd,
            SmartPlanCallback<SmartPlan> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (!hasValidUser(
                userId,
                callback
        )) {
            return;
        }

        if (periodStart == null
                || periodEnd == null) {

            callback.onError(
                    "Invalid Smart Plan period."
            );

            return;
        }

        if (!periodEnd.equals(
                periodStart.plusDays(6)
        )) {

            callback.onError(
                    "Smart Plan period must contain 7 days."
            );

            return;
        }

        JsonObject request =
                new JsonObject();

        request.addProperty(
                "user_id",
                userId
        );

        request.addProperty(
                "period_start",
                periodStart.toString()
        );

        request.addProperty(
                "period_end",
                periodEnd.toString()
        );

        request.addProperty(
                "status",
                SmartPlanStatus.ACTIVE.name()
        );

        RequestBody requestBody =
                createJsonRequestBody(
                        request
                );

        smartPlanApi
                .createSmartPlan(
                        requestBody,
                        SMART_PLAN_SELECT
                )
                .enqueue(
                        new Callback<List<SmartPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlan>> call,
                                    Response<List<SmartPlan>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to create Smart Plan."
                                    );

                                    return;
                                }

                                List<SmartPlan> plans =
                                        response.body();

                                if (plans == null
                                        || plans.isEmpty()) {

                                    callback.onError(
                                            "Smart Plan creation was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        plans.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlan>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // ATOMIC CREATE FIRST SMART PLAN
    //
    // Calls:
    //
    // public.create_smart_plan(...)
    //
    // PostgreSQL performs:
    //
    // new ACTIVE Smart Plan
    // + all generated SmartPlanItems
    //
    // inside one transaction.
    // =========================================================

    public void createSmartPlanAtomically(
            LocalDate periodStart,
            LocalDate periodEnd,
            List<GeneratedSmartPlanItem> generatedItems,
            SmartPlanCallback<SmartPlan> callback
    ) {

        String userId =
                sessionManager.getUserId();


        if (!hasValidUser(
                userId,
                callback
        )) {
            return;
        }


        if (periodStart == null
                || periodEnd == null) {

            callback.onError(
                    "Invalid Smart Plan period."
            );

            return;
        }


        if (!periodEnd.equals(
                periodStart.plusDays(6)
        )) {

            callback.onError(
                    "Smart Plan period must contain 7 days."
            );

            return;
        }


        if (generatedItems == null
                || generatedItems.isEmpty()) {

            callback.onError(
                    "Smart Plan cannot be created without sessions."
            );

            return;
        }


        JsonArray itemsJson =
                new JsonArray();


        for (GeneratedSmartPlanItem generatedItem
                : generatedItems) {

            if (!isGeneratedItemValid(
                    generatedItem
            )) {

                callback.onError(
                        "Generated Smart Plan contains an invalid session."
                );

                return;
            }


            JsonObject itemJson =
                    new JsonObject();


            itemJson.addProperty(
                    "task_id",
                    generatedItem.getTaskId()
            );


            itemJson.addProperty(
                    "planned_start",
                    generatedItem
                            .getPlannedStart()
                            .toOffsetDateTime()
                            .toString()
            );


            itemJson.addProperty(
                    "planned_end",
                    generatedItem
                            .getPlannedEnd()
                            .toOffsetDateTime()
                            .toString()
            );


            itemJson.addProperty(
                    "planned_duration",
                    generatedItem.getPlannedDuration()
            );


            itemsJson.add(
                    itemJson
            );
        }


        CreateSmartPlanRequest request =
                new CreateSmartPlanRequest(
                        periodStart.toString(),
                        periodEnd.toString(),
                        itemsJson
                );


        smartPlanApi
                .createSmartPlanAtomic(
                        request
                )
                .enqueue(
                        new Callback<List<SmartPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlan>> call,
                                    Response<List<SmartPlan>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to create Smart Plan."
                                    );

                                    return;
                                }


                                List<SmartPlan> plans =
                                        response.body();


                                if (plans == null
                                        || plans.isEmpty()) {

                                    callback.onError(
                                            "Created Smart Plan was not returned."
                                    );

                                    return;
                                }


                                callback.onSuccess(
                                        plans.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlan>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // ATOMIC REGENERATE SMART PLAN
    //
    // Calls:
    //
    // public.regenerate_smart_plan(...)
    //
    // PostgreSQL performs:
    //
    // old current plan -> ARCHIVED
    // new plan -> ACTIVE
    // insert all sessions
    //
    // inside one transaction.
    // =========================================================

    public void regenerateSmartPlan(
            LocalDate periodStart,
            LocalDate periodEnd,
            List<GeneratedSmartPlanItem> generatedItems,
            SmartPlanCallback<SmartPlan> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (!hasValidUser(
                userId,
                callback
        )) {
            return;
        }

        if (periodStart == null
                || periodEnd == null) {

            callback.onError(
                    "Invalid Smart Plan period."
            );

            return;
        }

        if (!periodEnd.equals(
                periodStart.plusDays(6)
        )) {

            callback.onError(
                    "Smart Plan period must contain 7 days."
            );

            return;
        }

        if (generatedItems == null
                || generatedItems.isEmpty()) {

            callback.onError(
                    "Smart Plan cannot be regenerated without sessions."
            );

            return;
        }

        JsonArray itemsJson =
                new JsonArray();

        for (GeneratedSmartPlanItem generatedItem
                : generatedItems) {

            if (!isGeneratedItemValid(
                    generatedItem
            )) {

                callback.onError(
                        "Generated Smart Plan contains an invalid session."
                );

                return;
            }

            JsonObject itemJson =
                    new JsonObject();

            itemJson.addProperty(
                    "task_id",
                    generatedItem.getTaskId()
            );

            itemJson.addProperty(
                    "planned_start",
                    generatedItem
                            .getPlannedStart()
                            .toOffsetDateTime()
                            .toString()
            );

            itemJson.addProperty(
                    "planned_end",
                    generatedItem
                            .getPlannedEnd()
                            .toOffsetDateTime()
                            .toString()
            );

            itemJson.addProperty(
                    "planned_duration",
                    generatedItem.getPlannedDuration()
            );

            itemsJson.add(
                    itemJson
            );
        }

        RegenerateSmartPlanRequest request =
                new RegenerateSmartPlanRequest(
                        periodStart.toString(),
                        periodEnd.toString(),
                        itemsJson
                );

        smartPlanApi
                .regenerateSmartPlan(
                        request
                )
                .enqueue(
                        new Callback<List<SmartPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlan>> call,
                                    Response<List<SmartPlan>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to regenerate Smart Plan."
                                    );

                                    return;
                                }

                                List<SmartPlan> plans =
                                        response.body();

                                if (plans == null
                                        || plans.isEmpty()) {

                                    callback.onError(
                                            "Regenerated Smart Plan was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        plans.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlan>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // CREATE GENERATED ITEMS
    //
    // Legacy persistence method kept for compatibility.
    //
    // Atomic Generate will no longer need this method.
    // =========================================================

    public void createGeneratedSmartPlanItems(
            String smartPlanId,
            List<GeneratedSmartPlanItem> generatedItems,
            SmartPlanCallback<List<SmartPlanItem>> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (!hasValidUser(
                userId,
                callback
        )) {
            return;
        }

        if (smartPlanId == null
                || smartPlanId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan."
            );

            return;
        }

        if (generatedItems == null
                || generatedItems.isEmpty()) {

            callback.onSuccess(
                    new ArrayList<>()
            );

            return;
        }

        List<SmartPlanItem> createdItems =
                new ArrayList<>();

        createGeneratedSmartPlanItemRecursive(
                smartPlanId,
                userId,
                generatedItems,
                0,
                createdItems,
                callback
        );
    }


    private void createGeneratedSmartPlanItemRecursive(
            String smartPlanId,
            String userId,
            List<GeneratedSmartPlanItem> generatedItems,
            int index,
            List<SmartPlanItem> createdItems,
            SmartPlanCallback<List<SmartPlanItem>> callback
    ) {

        if (index >= generatedItems.size()) {

            callback.onSuccess(
                    createdItems
            );

            return;
        }


        GeneratedSmartPlanItem generatedItem =
                generatedItems.get(
                        index
                );


        if (!isGeneratedItemValid(
                generatedItem
        )) {

            callback.onError(
                    "Generated Smart Plan contains an invalid session."
            );

            return;
        }


        SmartPlanItemRequest request =
                new SmartPlanItemRequest(
                        smartPlanId,
                        userId,
                        generatedItem.getTaskId(),
                        generatedItem
                                .getPlannedStart()
                                .toOffsetDateTime()
                                .toString(),
                        generatedItem
                                .getPlannedEnd()
                                .toOffsetDateTime()
                                .toString(),
                        generatedItem.getPlannedDuration(),
                        SmartPlanItemStatus.PLANNED,
                        false
                );


        smartPlanApi
                .createSmartPlanItem(
                        request,
                        SMART_PLAN_ITEM_SELECT
                )
                .enqueue(
                        new Callback<List<SmartPlanItem>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlanItem>> call,
                                    Response<List<SmartPlanItem>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to save Smart Plan session."
                                    );

                                    return;
                                }


                                List<SmartPlanItem> items =
                                        response.body();


                                if (items == null
                                        || items.isEmpty()) {

                                    callback.onError(
                                            "Smart Plan session creation was not returned."
                                    );

                                    return;
                                }


                                createdItems.add(
                                        items.get(0)
                                );


                                createGeneratedSmartPlanItemRecursive(
                                        smartPlanId,
                                        userId,
                                        generatedItems,
                                        index + 1,
                                        createdItems,
                                        callback
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlanItem>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // MOVE SMART PLAN ITEM
    // =========================================================

    public void moveSmartPlanItem(
            String smartPlanItemId,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            SmartPlanCallback<SmartPlanItem> callback
    ) {

        if (smartPlanItemId == null
                || smartPlanItemId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan session."
            );

            return;
        }


        if (newStart == null
                || newEnd == null) {

            callback.onError(
                    "Invalid session time."
            );

            return;
        }


        if (!newEnd.isAfter(
                newStart
        )) {

            callback.onError(
                    "Session end must be after session start."
            );

            return;
        }


        if (!isOnFifteenMinuteGrid(
                newStart
        )
                || !isOnFifteenMinuteGrid(
                newEnd
        )) {

            callback.onError(
                    "Session time must use the 15-minute grid."
            );

            return;
        }


        long durationMinutes =
                Duration
                        .between(
                                newStart,
                                newEnd
                        )
                        .toMinutes();


        if (durationMinutes <= 0
                || durationMinutes % 15 != 0
                || durationMinutes > Integer.MAX_VALUE) {

            callback.onError(
                    "Session duration must use the 15-minute grid."
            );

            return;
        }


        JsonObject request =
                new JsonObject();


        request.addProperty(
                "planned_start",
                newStart
                        .toOffsetDateTime()
                        .toString()
        );


        request.addProperty(
                "planned_end",
                newEnd
                        .toOffsetDateTime()
                        .toString()
        );


        request.addProperty(
                "planned_duration",
                (int) durationMinutes
        );


        request.addProperty(
                "is_manually_adjusted",
                true
        );


        RequestBody requestBody =
                createJsonRequestBody(
                        request
                );


        updateSmartPlanItem(
                smartPlanItemId,
                requestBody,
                "Unable to move Smart Plan session.",
                callback
        );
    }


    // =========================================================
    // COMPLETE SMART PLAN ITEM
    // =========================================================

    public void completeSmartPlanItem(
            String smartPlanItemId,
            ZonedDateTime completedAt,
            SmartPlanCallback<SmartPlanItem> callback
    ) {

        if (smartPlanItemId == null
                || smartPlanItemId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan session."
            );

            return;
        }


        ZonedDateTime safeCompletedAt =
                completedAt != null
                        ? completedAt
                        : ZonedDateTime.now();


        JsonObject request =
                new JsonObject();


        request.addProperty(
                "status",
                SmartPlanItemStatus.COMPLETED.name()
        );


        request.addProperty(
                "completed_at",
                safeCompletedAt
                        .toOffsetDateTime()
                        .toString()
        );


        updateSmartPlanItem(
                smartPlanItemId,
                createJsonRequestBody(
                        request
                ),
                "Unable to complete Smart Plan session.",
                callback
        );
    }


    // =========================================================
    // SKIP SMART PLAN ITEM
    // =========================================================

    public void skipSmartPlanItem(
            String smartPlanItemId,
            SmartPlanCallback<SmartPlanItem> callback
    ) {

        if (smartPlanItemId == null
                || smartPlanItemId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan session."
            );

            return;
        }


        JsonObject request =
                new JsonObject();


        request.addProperty(
                "status",
                SmartPlanItemStatus.SKIPPED.name()
        );


        request.add(
                "completed_at",
                JsonNull.INSTANCE
        );


        updateSmartPlanItem(
                smartPlanItemId,
                createJsonRequestBody(
                        request
                ),
                "Unable to skip Smart Plan session.",
                callback
        );
    }


    // =========================================================
    // REMOVE SMART PLAN ITEM
    // =========================================================

    public void deleteSmartPlanItem(
            String smartPlanItemId,
            SmartPlanCallback<Void> callback
    ) {

        if (smartPlanItemId == null
                || smartPlanItemId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan session."
            );

            return;
        }


        smartPlanApi
                .deleteSmartPlanItem(
                        "eq." + smartPlanItemId
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    callback.onSuccess(
                                            null
                                    );

                                } else {

                                    callback.onError(
                                            "Unable to remove Smart Plan session."
                                    );
                                }
                            }


                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // MARK NEEDS UPDATE
    // =========================================================

    public void markSmartPlanNeedsUpdate(
            String smartPlanId,
            SmartPlanCallback<SmartPlan> callback
    ) {

        if (smartPlanId == null
                || smartPlanId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan."
            );

            return;
        }


        updateSmartPlanStatus(
                smartPlanId,
                SmartPlanStatus.NEEDS_UPDATE,
                callback
        );
    }


    // =========================================================
    // ARCHIVE
    // =========================================================

    public void archiveSmartPlan(
            String smartPlanId,
            SmartPlanCallback<SmartPlan> callback
    ) {

        if (smartPlanId == null
                || smartPlanId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Smart Plan."
            );

            return;
        }


        updateSmartPlanStatus(
                smartPlanId,
                SmartPlanStatus.ARCHIVED,
                callback
        );
    }


    // =========================================================
    // UPDATE SMART PLAN STATUS
    // =========================================================

    private void updateSmartPlanStatus(
            String smartPlanId,
            SmartPlanStatus status,
            SmartPlanCallback<SmartPlan> callback
    ) {

        if (status == null) {

            callback.onError(
                    "Invalid Smart Plan status."
            );

            return;
        }


        JsonObject request =
                new JsonObject();


        request.addProperty(
                "status",
                status.name()
        );


        smartPlanApi
                .updateSmartPlan(
                        "eq." + smartPlanId,
                        SMART_PLAN_SELECT,
                        createJsonRequestBody(
                                request
                        )
                )
                .enqueue(
                        new Callback<List<SmartPlan>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlan>> call,
                                    Response<List<SmartPlan>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update Smart Plan."
                                    );

                                    return;
                                }


                                List<SmartPlan> plans =
                                        response.body();


                                if (plans == null
                                        || plans.isEmpty()) {

                                    callback.onError(
                                            "Smart Plan update was not returned."
                                    );

                                    return;
                                }


                                callback.onSuccess(
                                        plans.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlan>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // UPDATE SMART PLAN ITEM
    // =========================================================

    private void updateSmartPlanItem(
            String smartPlanItemId,
            RequestBody requestBody,
            String errorMessage,
            SmartPlanCallback<SmartPlanItem> callback
    ) {

        smartPlanApi
                .updateSmartPlanItem(
                        "eq." + smartPlanItemId,
                        SMART_PLAN_ITEM_SELECT,
                        requestBody
                )
                .enqueue(
                        new Callback<List<SmartPlanItem>>() {

                            @Override
                            public void onResponse(
                                    Call<List<SmartPlanItem>> call,
                                    Response<List<SmartPlanItem>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            errorMessage
                                    );

                                    return;
                                }


                                List<SmartPlanItem> items =
                                        response.body();


                                if (items == null
                                        || items.isEmpty()) {

                                    callback.onError(
                                            "Smart Plan session update was not returned."
                                    );

                                    return;
                                }


                                callback.onSuccess(
                                        items.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<SmartPlanItem>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isGeneratedItemValid(
            GeneratedSmartPlanItem item
    ) {

        if (item == null) {

            return false;
        }


        if (item.getTaskId() == null
                || item.getTaskId()
                .trim()
                .isEmpty()) {

            return false;
        }


        if (item.getPlannedStart() == null
                || item.getPlannedEnd() == null) {

            return false;
        }


        if (!item
                .getPlannedEnd()
                .isAfter(
                        item.getPlannedStart()
                )) {

            return false;
        }


        if (item.getPlannedDuration() <= 0
                || item.getPlannedDuration() % 15 != 0) {

            return false;
        }


        return isOnFifteenMinuteGrid(
                item.getPlannedStart()
        )
                && isOnFifteenMinuteGrid(
                item.getPlannedEnd()
        );
    }


    private <T> boolean hasValidUser(
            String userId,
            SmartPlanCallback<T> callback
    ) {

        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return false;
        }


        return true;
    }


    private boolean isOnFifteenMinuteGrid(
            ZonedDateTime dateTime
    ) {

        return dateTime != null
                && dateTime.getMinute() % 15 == 0
                && dateTime.getSecond() == 0
                && dateTime.getNano() == 0;
    }


    private RequestBody createJsonRequestBody(
            JsonObject jsonObject
    ) {

        return RequestBody.create(
                jsonObject.toString(),
                JSON_MEDIA_TYPE
        );
    }
}