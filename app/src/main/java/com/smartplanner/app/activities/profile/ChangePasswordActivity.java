package com.smartplanner.app.activities.profile;

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

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilNewPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnSavePassword;
    private ProgressBar progressChangePassword;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        authRepository = new AuthRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {

        tilNewPassword =
                findViewById(R.id.tilNewPassword);

        tilConfirmPassword =
                findViewById(R.id.tilConfirmPassword);

        etNewPassword =
                findViewById(R.id.etNewPassword);

        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);

        btnSavePassword =
                findViewById(R.id.btnSavePassword);

        progressChangePassword =
                findViewById(R.id.progressChangePassword);
    }

    private void setupListeners() {

        btnSavePassword.setOnClickListener(
                view -> attemptPasswordChange()
        );
    }

    private void attemptPasswordChange() {

        clearErrors();

        String newPassword =
                getText(etNewPassword);

        String confirmPassword =
                getText(etConfirmPassword);

        if (!validateInput(
                newPassword,
                confirmPassword
        )) {
            return;
        }

        setLoading(true);

        authRepository.changePassword(
                newPassword,
                new AuthRepository.AuthCallback<AuthResponse.User>() {

                    @Override
                    public void onSuccess(
                            AuthResponse.User result
                    ) {

                        runOnUiThread(() -> {

                            setLoading(false);

                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    R.string.change_password_success,
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        });
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        runOnUiThread(() -> {

                            setLoading(false);

                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    message == null
                                            || message.trim().isEmpty()
                                            ? getString(
                                            R.string.change_password_error
                                    )
                                            : message,
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

        if (newPassword.isEmpty()) {

            tilNewPassword.setError(
                    getString(
                            R.string.error_password_required
                    )
            );

            isValid = false;

        } else if (!ValidationUtils.isValidPassword(
                newPassword
        )) {

            tilNewPassword.setError(
                    getString(
                            R.string.error_password_too_short
                    )
            );

            isValid = false;
        }

        if (confirmPassword.isEmpty()) {

            tilConfirmPassword.setError(
                    getString(
                            R.string.error_confirm_password_required
                    )
            );

            isValid = false;

        } else if (!ValidationUtils.doPasswordsMatch(
                newPassword,
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

        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void setLoading(
            boolean isLoading
    ) {

        progressChangePassword.setVisibility(
                isLoading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnSavePassword.setEnabled(
                !isLoading
        );

        etNewPassword.setEnabled(
                !isLoading
        );

        etConfirmPassword.setEnabled(
                !isLoading
        );
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