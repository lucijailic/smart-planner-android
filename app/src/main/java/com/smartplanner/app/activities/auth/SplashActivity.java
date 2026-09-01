package com.smartplanner.app.activities.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.smartplanner.app.MainActivity;
import com.smartplanner.app.R;
import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.repositories.AuthRepository;

public class SplashActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authRepository = new AuthRepository(this);

        checkSession();
    }

    private void checkSession() {

        if (!authRepository.hasSession()) {
            openLogin();
            return;
        }

        if (!authRepository.isAccessTokenExpired()) {
            openMain();
            return;
        }

        authRepository.refreshSession(
                new AuthRepository.AuthCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(AuthResponse result) {
                        runOnUiThread(() -> openMain());
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> openLogin());
                    }
                }
        );
    }

    private void openMain() {

        Intent intent = new Intent(
                SplashActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void openLogin() {

        Intent intent = new Intent(
                SplashActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}