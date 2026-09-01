package com.smartplanner.app.api;

import androidx.annotation.NonNull;

import com.smartplanner.app.storage.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request originalRequest = chain.request();

        // If the request already has an Authorization header,
        // keep it. This is needed for flows such as password recovery.
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest);
        }

        String accessToken = sessionManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request authenticatedRequest = originalRequest
                .newBuilder()
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .build();

        return chain.proceed(authenticatedRequest);
    }
}