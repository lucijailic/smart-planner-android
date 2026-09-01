package com.smartplanner.app.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.MainActivity;
import com.smartplanner.app.R;
import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private MaterialButton btnSignIn;

    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    private ProgressBar progressLogin;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSignIn = findViewById(R.id.btnSignIn);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        progressLogin = findViewById(R.id.progressLogin);
    }

    private void setupListeners() {

        btnSignIn.setOnClickListener(view -> attemptLogin());

        tvForgotPassword.setOnClickListener(view -> {
            // Navigation will be added after ForgotPasswordActivity is created.
        });

        tvCreateAccount.setOnClickListener(view -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void attemptLogin() {

        clearErrors();

        String email = getText(etEmail).trim();
        String password = getText(etPassword);

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true);

        authRepository.login(
                email,
                password,
                new AuthRepository.AuthCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(AuthResponse result) {

                        runOnUiThread(() -> {
                            setLoading(false);

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    MainActivity.class
                            );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {

                        runOnUiThread(() -> {
                            setLoading(false);

                            tilPassword.setError(message);
                        });
                    }
                }
        );
    }

    private boolean validateInput(
            String email,
            String password
    ) {

        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(
                    getString(R.string.error_invalid_email)
            );

            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError(
                    getString(R.string.error_password_required)
            );

            isValid = false;

        } else if (!ValidationUtils.isValidPassword(password)) {

            tilPassword.setError(
                    getString(R.string.error_password_too_short)
            );

            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void setLoading(boolean isLoading) {

        progressLogin.setVisibility(
                isLoading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnSignIn.setEnabled(!isLoading);

        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);

        tvForgotPassword.setEnabled(!isLoading);
        tvCreateAccount.setEnabled(!isLoading);
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