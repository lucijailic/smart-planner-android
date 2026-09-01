package com.smartplanner.app.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.utils.ValidationUtils;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;

    private MaterialButton btnSendResetLink;

    private ProgressBar progressForgotPassword;

    private TextView tvBackToLogin;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authRepository = new AuthRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);

        btnSendResetLink =
                findViewById(R.id.btnSendResetLink);

        progressForgotPassword =
                findViewById(R.id.progressForgotPassword);

        tvBackToLogin =
                findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {

        btnSendResetLink.setOnClickListener(
                view -> attemptPasswordReset()
        );

        tvBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ForgotPasswordActivity.this,
                    LoginActivity.class
            );

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        });
    }

    private void attemptPasswordReset() {

        clearErrors();

        String email = getText(etEmail).trim();

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(
                    getString(R.string.error_invalid_email)
            );
            return;
        }

        setLoading(true);

        authRepository.forgotPassword(
                email,
                new AuthRepository.AuthCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    R.string.reset_link_sent,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    R.string.error_send_reset_link,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void clearErrors() {
        tilEmail.setError(null);
    }

    private void setLoading(boolean isLoading) {

        progressForgotPassword.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );

        btnSendResetLink.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        tvBackToLogin.setEnabled(!isLoading);
    }

    private String getText(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString();
    }
}