package com.smartplanner.app.activities.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.UserProfile;
import com.smartplanner.app.utils.ValidationUtils;
import com.smartplanner.app.viewmodels.ProfileViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;

    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etEmail;

    private LinearLayout layoutEditProfileForm;
    private LinearLayout layoutEditProfileError;

    private ProgressBar progressEditProfileLoading;
    private ProgressBar progressSaveProfile;

    private TextView tvEditProfileError;

    private MaterialButton btnSaveProfile;
    private MaterialButton btnRetryEditProfile;

    private ProfileViewModel profileViewModel;

    private String originalFirstName = "";
    private String originalLastName = "";

    private boolean profileLoaded = false;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupViewModel();
        setupListeners();
        setupBackNavigation();
    }

    private void initViews() {

        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);

        layoutEditProfileForm =
                findViewById(R.id.layoutEditProfileForm);

        layoutEditProfileError =
                findViewById(R.id.layoutEditProfileError);

        progressEditProfileLoading =
                findViewById(R.id.progressEditProfileLoading);

        progressSaveProfile =
                findViewById(R.id.progressSaveProfile);

        tvEditProfileError =
                findViewById(R.id.tvEditProfileError);

        btnSaveProfile =
                findViewById(R.id.btnSaveProfile);

        btnRetryEditProfile =
                findViewById(R.id.btnRetryEditProfile);
    }

    private void setupViewModel() {

        profileViewModel =
                new ViewModelProvider(this)
                        .get(ProfileViewModel.class);

        profileViewModel
                .getProfileState()
                .observe(
                        this,
                        this::renderProfileState
                );

        profileViewModel
                .getUpdateProfileState()
                .observe(
                        this,
                        this::renderUpdateState
                );

        profileViewModel.loadProfile();
    }

    private void setupListeners() {

        btnSaveProfile.setOnClickListener(
                view -> attemptSave()
        );

        btnRetryEditProfile.setOnClickListener(
                view -> profileViewModel.loadProfile()
        );
    }

    private void setupBackNavigation() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (isSaving) {
                            return;
                        }

                        if (hasUnsavedChanges()) {
                            showDiscardDialog();
                        } else {
                            finish();
                        }
                    }
                }
        );
    }

    private void renderProfileState(
            UiState<UserProfile> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                showInitialLoading();
                break;

            case SUCCESS:
                populateProfile(state.getData());
                break;

            case ERROR:
                showLoadError(state.getMessage());
                break;
        }
    }

    private void renderUpdateState(
            UiState<UserProfile> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                setSaving(true);
                break;

            case SUCCESS:
                setSaving(false);

                Toast.makeText(
                        this,
                        R.string.profile_updated,
                        Toast.LENGTH_SHORT
                ).show();

                setResult(RESULT_OK);
                finish();
                break;

            case ERROR:
                setSaving(false);

                Toast.makeText(
                        this,
                        R.string.profile_update_error,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void populateProfile(
            UserProfile profile
    ) {

        if (profile == null) {
            showLoadError(null);
            return;
        }

        progressEditProfileLoading.setVisibility(View.GONE);
        layoutEditProfileError.setVisibility(View.GONE);
        layoutEditProfileForm.setVisibility(View.VISIBLE);

        originalFirstName =
                profile.getFirstName() == null
                        ? ""
                        : profile.getFirstName();

        originalLastName =
                profile.getLastName() == null
                        ? ""
                        : profile.getLastName();

        etFirstName.setText(originalFirstName);
        etLastName.setText(originalLastName);
        etEmail.setText(profile.getEmail());

        profileLoaded = true;
    }

    private void attemptSave() {

        clearErrors();

        String firstName =
                getText(etFirstName).trim();

        String lastName =
                getText(etLastName).trim();

        if (!validateInput(firstName, lastName)) {
            return;
        }

        if (!hasUnsavedChanges()) {
            finish();
            return;
        }

        profileViewModel.updateProfile(
                firstName,
                lastName
        );
    }

    private boolean validateInput(
            String firstName,
            String lastName
    ) {

        boolean valid = true;

        if (!ValidationUtils.isValidName(firstName)) {

            tilFirstName.setError(
                    getString(R.string.error_first_name)
            );

            valid = false;
        }

        if (!ValidationUtils.isValidName(lastName)) {

            tilLastName.setError(
                    getString(R.string.error_last_name)
            );

            valid = false;
        }

        return valid;
    }

    private boolean hasUnsavedChanges() {

        if (!profileLoaded) {
            return false;
        }

        String firstName =
                getText(etFirstName).trim();

        String lastName =
                getText(etLastName).trim();

        return !firstName.equals(originalFirstName.trim())
                || !lastName.equals(originalLastName.trim());
    }

    private void showDiscardDialog() {

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.discard_changes_title)
                .setMessage(R.string.discard_changes_message)
                .setNegativeButton(
                        R.string.continue_editing,
                        null
                )
                .setPositiveButton(
                        R.string.discard,
                        (dialog, which) -> finish()
                )
                .show();
    }

    private void showInitialLoading() {

        progressEditProfileLoading.setVisibility(View.VISIBLE);
        layoutEditProfileForm.setVisibility(View.GONE);
        layoutEditProfileError.setVisibility(View.GONE);
    }

    private void showLoadError(
            String message
    ) {

        progressEditProfileLoading.setVisibility(View.GONE);
        layoutEditProfileForm.setVisibility(View.GONE);
        layoutEditProfileError.setVisibility(View.VISIBLE);

        if (message == null
                || message.trim().isEmpty()) {

            tvEditProfileError.setText(
                    R.string.profile_error
            );

        } else {

            tvEditProfileError.setText(message);
        }
    }

    private void setSaving(boolean saving) {

        isSaving = saving;

        progressSaveProfile.setVisibility(
                saving
                        ? View.VISIBLE
                        : View.GONE
        );

        btnSaveProfile.setEnabled(!saving);

        etFirstName.setEnabled(!saving);
        etLastName.setEnabled(!saving);
    }

    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
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