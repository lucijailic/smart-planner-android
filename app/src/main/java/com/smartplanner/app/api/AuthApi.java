package com.smartplanner.app.api;

import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.models.auth.ForgotPasswordRequest;
import com.smartplanner.app.models.auth.LoginRequest;
import com.smartplanner.app.models.auth.RegisterRequest;
import com.smartplanner.app.models.auth.UpdatePasswordRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("auth/v1/token")
    Call<AuthResponse> login(
            @Query("grant_type") String grantType,
            @Body LoginRequest request
    );

    @POST("auth/v1/signup")
    Call<AuthResponse> register(
            @Body RegisterRequest request
    );

    @POST("auth/v1/token")
    Call<AuthResponse> refreshSession(
            @Query("grant_type") String grantType,
            @Body Map<String, String> request
    );

    @POST("auth/v1/recover")
    Call<Void> forgotPassword(
            @Query("redirect_to") String redirectTo,
            @Body ForgotPasswordRequest request
    );

    @POST("auth/v1/logout")
    Call<Void> logout(
            @Query("scope") String scope
    );

    @PUT("auth/v1/user")
    Call<AuthResponse.User> updatePassword(
            @Body UpdatePasswordRequest request
    );

    @PUT("auth/v1/user")
    Call<AuthResponse.User> resetPassword(
            @Header("Authorization") String authorization,
            @Body UpdatePasswordRequest request
    );
}