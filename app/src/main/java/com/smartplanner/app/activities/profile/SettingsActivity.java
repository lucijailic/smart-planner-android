package com.smartplanner.app.activities.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.smartplan.PlanningPreferencesActivity;
import com.smartplanner.app.activities.smartplan.AvailabilityActivity;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView cardChangePassword;
    private MaterialCardView cardPlanningPreferences;
    private MaterialCardView cardAvailability;

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

        cardPlanningPreferences =
                findViewById(R.id.cardPlanningPreferences);

        cardAvailability =
                findViewById(
                        R.id.cardAvailability
                );
    }

    private void setupListeners() {

        cardChangePassword.setOnClickListener(
                view -> openChangePassword()
        );

        cardPlanningPreferences.setOnClickListener(
                view -> openPlanningPreferences()
        );

        cardAvailability.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    SettingsActivity.this,
                                    AvailabilityActivity.class
                            );

                    startActivity(
                            intent
                    );
                }
        );
    }

    private void openChangePassword() {

        Intent intent = new Intent(
                SettingsActivity.this,
                ChangePasswordActivity.class
        );

        startActivity(intent);
    }

    private void openPlanningPreferences() {

        Intent intent = new Intent(
                SettingsActivity.this,
                PlanningPreferencesActivity.class
        );

        startActivity(intent);
    }
}