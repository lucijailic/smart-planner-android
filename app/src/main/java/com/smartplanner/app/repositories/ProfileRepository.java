package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.ProfileApi;
import com.smartplanner.app.models.UpdateProfileRequest;
import com.smartplanner.app.models.UserProfile;
import com.smartplanner.app.storage.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    private static final String PROFILE_SELECT =
            "id,first_name,last_name,email,created_at,updated_at";

    private final ProfileApi profileApi;
    private final SessionManager sessionManager;

    public ProfileRepository(Context context) {

        profileApi = ApiClient
                .getClient(context)
                .create(ProfileApi.class);

        sessionManager =
                SessionManager.getInstance(context);
    }

    public interface ProfileCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }

    public void getCurrentUserProfile(
            ProfileCallback<UserProfile> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        profileApi.getProfile(
                        "eq." + userId,
                        PROFILE_SELECT,
                        1
                )
                .enqueue(
                        new Callback<List<UserProfile>>() {

                            @Override
                            public void onResponse(
                                    Call<List<UserProfile>> call,
                                    Response<List<UserProfile>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load your profile."
                                    );

                                    return;
                                }

                                List<UserProfile> profiles =
                                        response.body();

                                if (profiles == null
                                        || profiles.isEmpty()) {

                                    callback.onError(
                                            "Profile information was not found."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        profiles.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<UserProfile>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void updateCurrentUserProfile(
            String firstName,
            String lastName,
            ProfileCallback<UserProfile> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        UpdateProfileRequest request =
                new UpdateProfileRequest(
                        firstName,
                        lastName
                );

        profileApi.updateProfile(
                        "eq." + userId,
                        PROFILE_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<UserProfile>>() {

                            @Override
                            public void onResponse(
                                    Call<List<UserProfile>> call,
                                    Response<List<UserProfile>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update your profile."
                                    );

                                    return;
                                }

                                List<UserProfile> profiles =
                                        response.body();

                                if (profiles == null
                                        || profiles.isEmpty()) {

                                    callback.onError(
                                            "Profile update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        profiles.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<UserProfile>> call,
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