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
import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnRegister;
    private ProgressBar progressRegister;
    private TextView tvSignIn;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        progressRegister = findViewById(R.id.progressRegister);
        tvSignIn = findViewById(R.id.tvSignIn);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(view -> attemptRegistration());

        tvSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });
    }

    private void attemptRegistration() {
        clearErrors();

        String firstName = getText(etFirstName).trim();
        String lastName = getText(etLastName).trim();
        String email = getText(etEmail).trim();
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (!validateInput(
                firstName,
                lastName,
                email,
                password,
                confirmPassword
        )) {
            return;
        }

        setLoading(true);

        authRepository.register(
                email,
                password,
                firstName,
                lastName,
                new AuthRepository.AuthCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(AuthResponse result) {
                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    RegisterActivity.this,
                                    R.string.registration_successful,
                                    Toast.LENGTH_SHORT
                            ).show();

                            /*
                             * For now, return to Login.
                             *
                             * We will finalize this behavior after
                             * configuring the Supabase email
                             * confirmation flow.
                             */
                            Intent intent = new Intent(
                                    RegisterActivity.this,
                                    LoginActivity.class
                            );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            );

                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    RegisterActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private boolean validateInput(
            String firstName,
            String lastName,
            String email,
            String password,
            String confirmPassword
    ) {
        boolean isValid = true;

        if (!ValidationUtils.isValidName(firstName)) {
            tilFirstName.setError(
                    getString(R.string.error_first_name)
            );
            isValid = false;
        }

        if (!ValidationUtils.isValidName(lastName)) {
            tilLastName.setError(
                    getString(R.string.error_last_name)
            );
            isValid = false;
        }

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

        if (!ValidationUtils.doPasswordsMatch(
                password,
                confirmPassword
        )) {
            tilConfirmPassword.setError(
                    getString(
                            R.string.error_passwords_do_not_match
                    )
            );
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void setLoading(boolean isLoading) {
        progressRegister.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );

        btnRegister.setEnabled(!isLoading);

        etFirstName.setEnabled(!isLoading);
        etLastName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);

        tvSignIn.setEnabled(!isLoading);
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString();
    }
}