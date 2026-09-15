package com.smartplanner.app.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.BuildConfig;
import com.smartplanner.app.MainActivity;
import com.smartplanner.app.R;
import com.smartplanner.app.models.auth.AuthResponse;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.utils.ValidationUtils;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private MaterialButton btnSignIn;
    private MaterialButton btnGoogleSignIn;

    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    private ProgressBar progressLogin;

    private AuthRepository authRepository;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);
        credentialManager = CredentialManager.create(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        progressLogin = findViewById(R.id.progressLogin);
    }

    private void setupListeners() {

        btnSignIn.setOnClickListener(view -> attemptLogin());

        btnGoogleSignIn.setOnClickListener(
                view -> attemptGoogleLogin()
        );

        tvForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );

            startActivity(intent);
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
                            openMainActivity();
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

    private void attemptGoogleLogin() {

        clearErrors();

        if (BuildConfig.GOOGLE_WEB_CLIENT_ID == null
                || BuildConfig.GOOGLE_WEB_CLIENT_ID.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Google Sign-In is not configured.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        setLoading(true);

        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(
                        BuildConfig.GOOGLE_WEB_CLIENT_ID
                ).build();

        GetCredentialRequest request =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(googleOption)
                        .build();

        Executor executor = getMainExecutor();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new androidx.credentials.CredentialManagerCallback<
                        GetCredentialResponse,
                        GetCredentialException>() {

                    @Override
                    public void onResult(
                            GetCredentialResponse result
                    ) {
                        handleGoogleCredential(result);
                    }

                    @Override
                    public void onError(
                            @NonNull GetCredentialException exception
                    ) {
                        setLoading(false);

                        Toast.makeText(
                                LoginActivity.this,
                                "Google Sign-In was cancelled or unavailable.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void handleGoogleCredential(
            GetCredentialResponse response
    ) {

        Credential credential = response.getCredential();

        if (!(credential instanceof CustomCredential)) {
            setLoading(false);

            Toast.makeText(
                    this,
                    "Unexpected Google credential.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        CustomCredential customCredential =
                (CustomCredential) credential;

        if (!GoogleIdTokenCredential
                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(customCredential.getType())) {

            setLoading(false);

            Toast.makeText(
                    this,
                    "Unexpected Google credential.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            GoogleIdTokenCredential googleCredential =
                    GoogleIdTokenCredential.createFrom(
                            customCredential.getData()
                    );

            String idToken =
                    googleCredential.getIdToken();

            if (idToken == null || idToken.trim().isEmpty()) {

                setLoading(false);

                Toast.makeText(
                        this,
                        "Google did not return a valid ID token.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            signInToSupabaseWithGoogle(idToken);

        } catch (Exception exception) {

            setLoading(false);

            Toast.makeText(
                    this,
                    "Unable to read Google account information.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void signInToSupabaseWithGoogle(
            String idToken
    ) {

        authRepository.loginWithGoogle(
                idToken,
                new AuthRepository.AuthCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(
                            AuthResponse result
                    ) {

                        runOnUiThread(() -> {
                            setLoading(false);
                            openMainActivity();
                        });
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        runOnUiThread(() -> {
                            setLoading(false);

                            Toast.makeText(
                                    LoginActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void openMainActivity() {

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
        btnGoogleSignIn.setEnabled(!isLoading);

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