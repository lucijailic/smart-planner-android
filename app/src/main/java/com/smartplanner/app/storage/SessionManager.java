package com.smartplanner.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "smart_planner_session";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EXPIRES_AT = "expires_at";

    private static SessionManager instance;

    private final SharedPreferences sharedPreferences;

    private SessionManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }

        return instance;
    }

    public void saveSession(
            String accessToken,
            String refreshToken,
            String userId,
            long expiresInSeconds
    ) {
        long expiresAt = System.currentTimeMillis()
                + (expiresInSeconds * 1000L);

        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public long getExpiresAt() {
        return sharedPreferences.getLong(KEY_EXPIRES_AT, 0L);
    }

    public boolean hasSession() {
        return getAccessToken() != null
                && getRefreshToken() != null
                && getUserId() != null;
    }

    public boolean isAccessTokenExpired() {
        long expiresAt = getExpiresAt();

        if (expiresAt == 0L) {
            return true;
        }

        return System.currentTimeMillis() >= expiresAt;
    }

    public void clearSession() {
        sharedPreferences.edit()
                .clear()
                .apply();
    }
}