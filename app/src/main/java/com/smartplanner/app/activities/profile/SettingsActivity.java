package com.smartplanner.app.activities.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.smartplanner.app.R;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView cardChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupListeners();
    }

    private void initViews() {

        cardChangePassword =
                findViewById(R.id.cardChangePassword);
    }

    private void setupListeners() {

        cardChangePassword.setOnClickListener(
                view -> openChangePassword()
        );
    }

    private void openChangePassword() {

        Intent intent = new Intent(
                SettingsActivity.this,
                ChangePasswordActivity.class
        );

        startActivity(intent);
    }
}