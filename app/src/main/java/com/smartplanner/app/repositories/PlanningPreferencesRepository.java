package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.PlanningPreferencesApi;
import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.PlanningPreferencesRequest;
import com.smartplanner.app.storage.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanningPreferencesRepository {

    private static final String PREFERENCES_SELECT =
            "user_id,max_daily_minutes,preferred_session_minutes,break_minutes,created_at,updated_at";


    private final PlanningPreferencesApi planningPreferencesApi;

    private final SessionManager sessionManager;

    private final SmartPlanInvalidationRepository
            smartPlanInvalidationRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PlanningPreferencesRepository(
            Context context
    ) {

        Context appContext =
                context.getApplicationContext();


        planningPreferencesApi =
                ApiClient
                        .getClient(appContext)
                        .create(
                                PlanningPreferencesApi.class
                        );


        sessionManager =
                SessionManager.getInstance(
                        appContext
                );


        smartPlanInvalidationRepository =
                new SmartPlanInvalidationRepository(
                        appContext
                );
    }


    // =========================================================
    // CALLBACK
    // =========================================================

    public interface PlanningPreferencesCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }


    // =========================================================
    // GET CURRENT PREFERENCES
    //
    // null result is valid:
    // it means that Smart Plan setup has not been completed yet.
    // =========================================================

    public void getCurrentPreferences(
            PlanningPreferencesCallback<PlanningPreferences> callback
    ) {

        String userId =
                sessionManager.getUserId();


        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }


        planningPreferencesApi
                .getPreferences(
                        "eq." + userId,
                        PREFERENCES_SELECT,
                        1
                )
                .enqueue(
                        new Callback<List<PlanningPreferences>>() {

                            @Override
                            public void onResponse(
                                    Call<List<PlanningPreferences>> call,
                                    Response<List<PlanningPreferences>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load planning preferences."
                                    );

                                    return;
                                }


                                List<PlanningPreferences> preferences =
                                        response.body();


                                /*
                                 * Unlike NotificationPreferences,
                                 * PlanningPreferences are not
                                 * created automatically during
                                 * registration.
                                 *
                                 * Empty result therefore means
                                 * Smart Plan setup has not been
                                 * completed yet.
                                 */
                                if (preferences == null
                                        || preferences.isEmpty()) {

                                    callback.onSuccess(
                                            null
                                    );

                                    return;
                                }


                                callback.onSuccess(
                                        preferences.get(0)
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<PlanningPreferences>> call,
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
    // CREATE PREFERENCES
    //
    // Used during first Smart Plan setup.
    //
    // Usually there is no current Smart Plan at this point.
    // Calling invalidation is still safe because the
    // invalidation repository treats "no current plan" as a
    // successful no-op.
    // =========================================================

    public void createPreferences(
            int maxDailyMinutes,
            int preferredSessionMinutes,
            int breakMinutes,
            PlanningPreferencesCallback<PlanningPreferences> callback
    ) {

        String userId =
                sessionManager.getUserId();


        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }


        PlanningPreferencesRequest request =
                new PlanningPreferencesRequest(
                        userId,
                        maxDailyMinutes,
                        preferredSessionMinutes,
                        breakMinutes
                );


        planningPreferencesApi
                .createPreferences(
                        PREFERENCES_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<PlanningPreferences>>() {

                            @Override
                            public void onResponse(
                                    Call<List<PlanningPreferences>> call,
                                    Response<List<PlanningPreferences>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to create planning preferences."
                                    );

                                    return;
                                }


                                List<PlanningPreferences> preferences =
                                        response.body();


                                if (preferences == null
                                        || preferences.isEmpty()) {

                                    callback.onError(
                                            "Planning preferences were not returned."
                                    );

                                    return;
                                }


                                PlanningPreferences createdPreferences =
                                        preferences.get(0);


                                invalidateSmartPlanAfterPreferencesChange(
                                        () -> callback.onSuccess(
                                                createdPreferences
                                        )
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<PlanningPreferences>> call,
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
    // UPDATE PREFERENCES
    //
    // All three fields directly affect Smart Plan generation:
    //
    // max_daily_minutes
    // preferred_session_minutes
    // break_minutes
    //
    // Therefore an existing plan becomes NEEDS_UPDATE after a
    // successful update.
    // =========================================================

    public void updatePreferences(
            int maxDailyMinutes,
            int preferredSessionMinutes,
            int breakMinutes,
            PlanningPreferencesCallback<PlanningPreferences> callback
    ) {

        String userId =
                sessionManager.getUserId();


        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }


        PlanningPreferencesRequest request =
                new PlanningPreferencesRequest(
                        userId,
                        maxDailyMinutes,
                        preferredSessionMinutes,
                        breakMinutes
                );


        planningPreferencesApi
                .updatePreferences(
                        "eq." + userId,
                        PREFERENCES_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<PlanningPreferences>>() {

                            @Override
                            public void onResponse(
                                    Call<List<PlanningPreferences>> call,
                                    Response<List<PlanningPreferences>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update planning preferences."
                                    );

                                    return;
                                }


                                List<PlanningPreferences> preferences =
                                        response.body();


                                if (preferences == null
                                        || preferences.isEmpty()) {

                                    callback.onError(
                                            "Planning preferences update was not returned."
                                    );

                                    return;
                                }


                                PlanningPreferences updatedPreferences =
                                        preferences.get(0);


                                invalidateSmartPlanAfterPreferencesChange(
                                        () -> callback.onSuccess(
                                                updatedPreferences
                                        )
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<PlanningPreferences>> call,
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
    // SMART PLAN INVALIDATION
    //
    // Preferences operation has already succeeded when this
    // helper runs.
    //
    // A secondary invalidation failure therefore must not turn
    // a successful Preferences operation into an error.
    // =========================================================

    private void invalidateSmartPlanAfterPreferencesChange(
            Runnable onFinished
    ) {

        smartPlanInvalidationRepository
                .markCurrentPlanNeedsUpdate(
                        new SmartPlanInvalidationRepository
                                .InvalidationCallback() {

                            @Override
                            public void onComplete() {

                                if (onFinished != null) {

                                    onFinished.run();
                                }
                            }


                            @Override
                            public void onError(
                                    String message
                            ) {

                                /*
                                 * Preferences were already saved.
                                 *
                                 * Preserve the successful result
                                 * even if Smart Plan invalidation
                                 * itself failed.
                                 */
                                if (onFinished != null) {

                                    onFinished.run();
                                }
                            }
                        }
                );
    }
}