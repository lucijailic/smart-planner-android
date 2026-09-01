package com.smartplanner.app.activities.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.utils.ValidationUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmNewPassword;

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmNewPassword;

    private MaterialButton btnSaveNewPassword;
    private ProgressBar progressResetPassword;

    private AuthRepository authRepository;

    private String recoveryAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        authRepository = new AuthRepository(this);

        initViews();

        recoveryAccessToken = extractRecoveryAccessToken(
                getIntent()
        );

        if (recoveryAccessToken == null
                || recoveryAccessToken.isEmpty()) {

            showInvalidRecoveryLink();
            return;
        }

        setupListeners();
    }

    private void initViews() {
        tilNewPassword =
                findViewById(R.id.tilNewPassword);

        tilConfirmNewPassword =
                findViewById(R.id.tilConfirmNewPassword);

        etNewPassword =
                findViewById(R.id.etNewPassword);

        etConfirmNewPassword =
                findViewById(R.id.etConfirmNewPassword);

        btnSaveNewPassword =
                findViewById(R.id.btnSaveNewPassword);

        progressResetPassword =
                findViewById(R.id.progressResetPassword);
    }

    private void setupListeners() {
        btnSaveNewPassword.setOnClickListener(
                view -> attemptPasswordReset()
        );
    }

    private void attemptPasswordReset() {

        clearErrors();

        String newPassword =
                getText(etNewPassword);

        String confirmPassword =
                getText(etConfirmNewPassword);

        if (!validateInput(
                newPassword,
                confirmPassword
        )) {
            return;
        }

        setLoading(true);

        authRepository.resetPassword(
                recoveryAccessToken,
                newPassword,
                new AuthRepository.AuthCallback<AuthResponse.User>() {

                    @Override
                    public void onSuccess(
                            AuthResponse.User result
                    ) {

                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    ResetPasswordActivity.this,
                                    R.string.password_reset_successful,
                                    Toast.LENGTH_LONG
                            ).show();

                            openLogin();
                        });
                    }

                    @Override
                    public void onError(String message) {

                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    ResetPasswordActivity.this,
                                    R.string.error_reset_password,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private boolean validateInput(
            String newPassword,
            String confirmPassword
    ) {

        boolean isValid = true;

        if (!ValidationUtils.isValidPassword(
                newPassword
        )) {

            tilNewPassword.setError(
                    getString(
                            R.string.error_password_too_short
                    )
            );

            isValid = false;
        }

        if (!ValidationUtils.doPasswordsMatch(
                newPassword,
                confirmPassword
        )) {

            tilConfirmNewPassword.setError(
                    getString(
                            R.string.error_passwords_do_not_match
                    )
            );

            isValid = false;
        }

        return isValid;
    }

    private String extractRecoveryAccessToken(
            Intent intent
    ) {

        if (intent == null
                || intent.getData() == null) {

            return null;
        }

        Uri uri = intent.getData();

        /*
         * Supabase recovery redirects commonly place
         * session parameters inside the URI fragment:
         *
         * smartplanner://reset-password
         * #access_token=...&type=recovery&...
         */
        String fragment = uri.getFragment();

        if (fragment != null
                && !fragment.isEmpty()) {

            Uri fragmentUri = Uri.parse(
                    "https://smartplanner.local/?"
                            + fragment
            );

            String type =
                    fragmentUri.getQueryParameter("type");

            String token =
                    fragmentUri.getQueryParameter(
                            "access_token"
                    );

            if ("recovery".equals(type)
                    && token != null
                    && !token.isEmpty()) {

                return token;
            }
        }

        /*
         * Also check regular query parameters
         * so the parsing is a little more robust.
         */
        String type =
                uri.getQueryParameter("type");

        String token =
                uri.getQueryParameter("access_token");

        if ("recovery".equals(type)
                && token != null
                && !token.isEmpty()) {

            return token;
        }

        return null;
    }

    private void showInvalidRecoveryLink() {

        btnSaveNewPassword.setEnabled(false);

        Toast.makeText(
                this,
                R.string.error_invalid_recovery_link,
                Toast.LENGTH_LONG
        ).show();
    }

    private void openLogin() {

        Intent intent = new Intent(
                ResetPasswordActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void clearErrors() {
        tilNewPassword.setError(null);
        tilConfirmNewPassword.setError(null);
    }

    private void setLoading(boolean isLoading) {

        progressResetPassword.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );

        btnSaveNewPassword.setEnabled(!isLoading);

        etNewPassword.setEnabled(!isLoading);
        etConfirmNewPassword.setEnabled(!isLoading);
    }

    private String getText(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString();
    }
}