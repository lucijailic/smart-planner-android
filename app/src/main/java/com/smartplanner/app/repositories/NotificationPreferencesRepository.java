package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.NotificationPreferencesApi;
import com.smartplanner.app.models.NotificationPreferences;
import com.smartplanner.app.models.NotificationPreferencesRequest;
import com.smartplanner.app.notifications.NotificationSettingsManager;
import com.smartplanner.app.storage.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationPreferencesRepository {

    private static final String PREFERENCES_SELECT =
            "user_id,notifications_enabled,default_task_reminder,default_event_reminder,created_at,updated_at";

    private final NotificationPreferencesApi notificationPreferencesApi;
    private final SessionManager sessionManager;

    private final Context appContext;

    public NotificationPreferencesRepository(
            Context context
    ) {

        appContext =
                context.getApplicationContext();

        notificationPreferencesApi =
                ApiClient
                        .getClient(appContext)
                        .create(
                                NotificationPreferencesApi.class
                        );

        sessionManager =
                SessionManager.getInstance(
                        appContext
                );
    }

    public interface NotificationPreferencesCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }

    // =========================================================
    // GET CURRENT PREFERENCES
    // =========================================================

    public void getCurrentPreferences(
            NotificationPreferencesCallback<NotificationPreferences> callback
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

        notificationPreferencesApi
                .getPreferences(
                        "eq." + userId,
                        PREFERENCES_SELECT,
                        1
                )
                .enqueue(
                        new Callback<List<NotificationPreferences>>() {

                            @Override
                            public void onResponse(
                                    Call<List<NotificationPreferences>> call,
                                    Response<List<NotificationPreferences>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load notification preferences."
                                    );

                                    return;
                                }

                                List<NotificationPreferences> preferences =
                                        response.body();

                                if (preferences == null
                                        || preferences.isEmpty()) {

                                    callback.onError(
                                            "Notification preferences were not found."
                                    );

                                    return;
                                }

                                NotificationPreferences result =
                                        preferences.get(0);


                                NotificationSettingsManager.savePreferences(
                                        appContext,
                                        result
                                );

                                callback.onSuccess(
                                        result
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<NotificationPreferences>> call,
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
    // =========================================================

    public void updatePreferences(
            boolean notificationsEnabled,
            String defaultTaskReminder,
            String defaultEventReminder,
            NotificationPreferencesCallback<NotificationPreferences> callback
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

        NotificationPreferencesRequest request =
                new NotificationPreferencesRequest(
                        notificationsEnabled,
                        defaultTaskReminder,
                        defaultEventReminder
                );

        notificationPreferencesApi
                .updatePreferences(
                        "eq." + userId,
                        PREFERENCES_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<NotificationPreferences>>() {

                            @Override
                            public void onResponse(
                                    Call<List<NotificationPreferences>> call,
                                    Response<List<NotificationPreferences>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update notification preferences."
                                    );

                                    return;
                                }

                                List<NotificationPreferences> preferences =
                                        response.body();

                                if (preferences == null
                                        || preferences.isEmpty()) {

                                    callback.onError(
                                            "Notification preferences update was not returned."
                                    );

                                    return;
                                }

                                NotificationPreferences result =
                                        preferences.get(0);


                                NotificationSettingsManager.savePreferences(
                                        appContext,
                                        result
                                );

                                callback.onSuccess(
                                        result
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<NotificationPreferences>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }
}