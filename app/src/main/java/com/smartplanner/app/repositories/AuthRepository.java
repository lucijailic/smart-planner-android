package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.AuthApi;
import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.models.auth.ForgotPasswordRequest;
import com.smartplanner.app.models.auth.LoginRequest;
import com.smartplanner.app.models.auth.RegisterRequest;
import com.smartplanner.app.models.auth.UpdatePasswordRequest;
import com.smartplanner.app.storage.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        authApi = ApiClient
                .getClient(context)
                .create(AuthApi.class);

        sessionManager = SessionManager.getInstance(context);
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    public void login(
            String email,
            String password,
            AuthCallback<AuthResponse> callback
    ) {

        LoginRequest request =
                new LoginRequest(email, password);

        authApi.login("password", request)
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<AuthResponse> call,
                            Response<AuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            AuthResponse authResponse = response.body();

                            if (saveSession(authResponse)) {
                                callback.onSuccess(authResponse);
                            } else {
                                callback.onError(
                                        "Unable to create a valid session."
                                );
                            }

                        } else {
                            callback.onError(
                                    "Invalid email or password."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AuthResponse> call,
                            Throwable throwable
                    ) {
                        callback.onError(
                                "Unable to connect. Please try again."
                        );
                    }
                });
    }

    public void register(
            String email,
            String password,
            String firstName,
            String lastName,
            AuthCallback<AuthResponse> callback
    ) {

        RegisterRequest request =
                new RegisterRequest(
                        email,
                        password,
                        firstName,
                        lastName
                );

        authApi.register(request)
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<AuthResponse> call,
                            Response<AuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            AuthResponse authResponse = response.body();

                            /*
                             * If email confirmation is disabled,
                             * Supabase may immediately return a session.
                             *
                             * If email confirmation is enabled,
                             * the user can be created without an
                             * access token / refresh token.
                             */
                            if (hasSessionData(authResponse)) {
                                saveSession(authResponse);
                            }

                            callback.onSuccess(authResponse);

                        } else {
                            callback.onError(
                                    "Unable to create account."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AuthResponse> call,
                            Throwable throwable
                    ) {
                        callback.onError(
                                "Unable to connect. Please try again."
                        );
                    }
                });
    }

    public void forgotPassword(
            String email,
            AuthCallback<Void> callback
    ) {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest(email);

        authApi.forgotPassword(request)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(
                                    "Unable to send reset instructions."
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
                });
    }

    public void changePassword(
            String newPassword,
            AuthCallback<AuthResponse.User> callback
    ) {

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(newPassword);

        authApi.updatePassword(request)
                .enqueue(new Callback<AuthResponse.User>() {

                    @Override
                    public void onResponse(
                            Call<AuthResponse.User> call,
                            Response<AuthResponse.User> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {
                            callback.onError(
                                    "Unable to update password."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AuthResponse.User> call,
                            Throwable throwable
                    ) {
                        callback.onError(
                                "Unable to connect. Please try again."
                        );
                    }
                });
    }

    public void refreshSession(
            AuthCallback<AuthResponse> callback
    ) {

        String refreshToken =
                sessionManager.getRefreshToken();

        if (refreshToken == null
                || refreshToken.isEmpty()) {

            callback.onError(
                    "No refresh token is available."
            );
            return;
        }

        Map<String, String> request =
                new HashMap<>();

        request.put(
                "refresh_token",
                refreshToken
        );

        authApi.refreshSession(
                        "refresh_token",
                        request
                )
                .enqueue(new Callback<AuthResponse>() {

                    @Override
                    public void onResponse(
                            Call<AuthResponse> call,
                            Response<AuthResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            AuthResponse authResponse =
                                    response.body();

                            if (saveRefreshedSession(
                                    authResponse
                            )) {
                                callback.onSuccess(
                                        authResponse
                                );
                            } else {
                                sessionManager.clearSession();

                                callback.onError(
                                        "Unable to refresh session."
                                );
                            }

                        } else {
                            sessionManager.clearSession();

                            callback.onError(
                                    "Your session has expired."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AuthResponse> call,
                            Throwable throwable
                    ) {
                        callback.onError(
                                "Unable to connect. Please try again."
                        );
                    }
                });
    }

    public void logout(
            AuthCallback<Void> callback
    ) {

        authApi.logout("local")
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        /*
                         * Local session should always be removed
                         * when the user chooses Logout.
                         */
                        sessionManager.clearSession();

                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(
                                    "You have been signed out locally."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable throwable
                    ) {

                        sessionManager.clearSession();

                        callback.onError(
                                "You have been signed out locally."
                        );
                    }
                });
    }

    public boolean hasSession() {
        return sessionManager.hasSession();
    }

    public boolean isAccessTokenExpired() {
        return sessionManager.isAccessTokenExpired();
    }

    private boolean saveSession(
            AuthResponse response
    ) {

        if (!hasSessionData(response)
                || response.getUser() == null
                || response.getUser().getId() == null) {

            return false;
        }

        sessionManager.saveSession(
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getUser().getId(),
                response.getExpiresIn()
        );

        return true;
    }

    private boolean saveRefreshedSession(
            AuthResponse response
    ) {

        if (response.getAccessToken() == null
                || response.getAccessToken().isEmpty()) {

            return false;
        }

        String refreshToken =
                response.getRefreshToken();

        if (refreshToken == null
                || refreshToken.isEmpty()) {

            refreshToken =
                    sessionManager.getRefreshToken();
        }

        String userId =
                sessionManager.getUserId();

        if (response.getUser() != null
                && response.getUser().getId() != null) {

            userId =
                    response.getUser().getId();
        }

        if (refreshToken == null
                || userId == null) {

            return false;
        }

        sessionManager.saveSession(
                response.getAccessToken(),
                refreshToken,
                userId,
                response.getExpiresIn()
        );

        return true;
    }

    private boolean hasSessionData(
            AuthResponse response
    ) {

        return response.getAccessToken() != null
                && !response.getAccessToken().isEmpty()
                && response.getRefreshToken() != null
                && !response.getRefreshToken().isEmpty();
    }
}