package com.smartplanner.app.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartplanner.app.BuildConfig;
import com.smartplanner.app.R;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView cardChangePassword;
    private MaterialCardView cardAboutApp;

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

        cardAboutApp =
                findViewById(R.id.cardAboutApp);
    }

    private void setupListeners() {

        cardChangePassword.setOnClickListener(
                view -> openChangePassword()
        );

        cardAboutApp.setOnClickListener(
                view -> showAboutDialog()
        );
    }

    private void openChangePassword() {

        Intent intent = new Intent(
                SettingsActivity.this,
                ChangePasswordActivity.class
        );

        startActivity(intent);
    }

    private void showAboutDialog() {

        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_about_app,
                                null
                        );

        TextView tvAboutVersion =
                dialogView.findViewById(
                        R.id.tvAboutVersion
                );

        tvAboutVersion.setText(
                getString(
                        R.string.about_dialog_version,
                        BuildConfig.VERSION_NAME
                )
        );

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton(
                        R.string.close,
                        null
                )
                .show();
    }
}