package com.smartplanner.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.smartplanner.app.BuildConfig;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.auth.LoginActivity;
import com.smartplanner.app.activities.profile.CategoriesActivity;
import com.smartplanner.app.activities.profile.EditProfileActivity;
import com.smartplanner.app.activities.profile.NotificationsActivity;
import com.smartplanner.app.activities.profile.SettingsActivity;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.UserProfile;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProgressBar progressProfile;

    private LinearLayout layoutProfileContent;
    private LinearLayout layoutProfileError;

    private TextView tvProfileInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileError;

    private MaterialButton btnRetryProfile;

    private MaterialCardView cardEditProfile;
    private MaterialCardView cardCategories;
    private MaterialCardView cardNotifications;
    private MaterialCardView cardSettings;
    private MaterialCardView cardAbout;
    private MaterialCardView cardLogout;
    private MaterialCardView cardDeleteAccount;

    private ProfileViewModel profileViewModel;
    private AuthRepository authRepository;

    private CredentialManager credentialManager;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initRepositories();
        setupActivityResultLaunchers();
        setupViewModel();
        setupListeners();
    }

    private void initViews(View view) {

        progressProfile =
                view.findViewById(R.id.progressProfile);

        layoutProfileContent =
                view.findViewById(R.id.layoutProfileContent);

        layoutProfileError =
                view.findViewById(R.id.layoutProfileError);

        tvProfileInitials =
                view.findViewById(R.id.tvProfileInitials);

        tvProfileName =
                view.findViewById(R.id.tvProfileName);

        tvProfileEmail =
                view.findViewById(R.id.tvProfileEmail);

        tvProfileError =
                view.findViewById(R.id.tvProfileError);

        btnRetryProfile =
                view.findViewById(R.id.btnRetryProfile);

        cardEditProfile =
                view.findViewById(R.id.cardEditProfile);

        cardCategories =
                view.findViewById(R.id.cardCategories);

        cardNotifications =
                view.findViewById(R.id.cardNotifications);

        cardSettings =
                view.findViewById(R.id.cardSettings);

        cardAbout =
                view.findViewById(R.id.cardAbout);

        cardLogout =
                view.findViewById(R.id.cardLogout);

        cardDeleteAccount =
                view.findViewById(R.id.cardDeleteAccount);
    }

    private void initRepositories() {

        authRepository =
                new AuthRepository(requireContext());

        credentialManager =
                CredentialManager.create(requireContext());
    }

    private void setupActivityResultLaunchers() {

        editProfileLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    == requireActivity().RESULT_OK) {

                                profileViewModel.loadProfile();
                            }
                        }
                );
    }

    private void setupViewModel() {

        profileViewModel =
                new ViewModelProvider(this)
                        .get(ProfileViewModel.class);

        profileViewModel
                .getProfileState()
                .observe(
                        getViewLifecycleOwner(),
                        this::renderProfileState
                );

        profileViewModel.loadProfile();
    }

    private void setupListeners() {

        btnRetryProfile.setOnClickListener(
                view -> profileViewModel.loadProfile()
        );

        cardEditProfile.setOnClickListener(
                view -> openEditProfile()
        );

        cardCategories.setOnClickListener(
                view -> openCategories()
        );

        cardNotifications.setOnClickListener(
                view -> openNotifications()
        );

        cardSettings.setOnClickListener(
                view -> openSettings()
        );

        cardAbout.setOnClickListener(
                view -> showAboutDialog()
        );

        cardLogout.setOnClickListener(
                view -> performLogout()
        );

        cardDeleteAccount.setOnClickListener(
                view -> showDeleteAccountDialog()
        );
    }

    private void openEditProfile() {

        Intent intent = new Intent(
                requireContext(),
                EditProfileActivity.class
        );

        editProfileLauncher.launch(intent);
    }

    private void openCategories() {

        Intent intent = new Intent(
                requireContext(),
                CategoriesActivity.class
        );

        startActivity(intent);
    }

    private void openNotifications() {

        Intent intent = new Intent(
                requireContext(),
                NotificationsActivity.class
        );

        startActivity(intent);
    }

    private void openSettings() {

        Intent intent = new Intent(
                requireContext(),
                SettingsActivity.class
        );

        startActivity(intent);
    }

    private void showAboutDialog() {

        if (!isAdded()) {
            return;
        }

        View dialogView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_about_app,
                                null
                        );

        TextView tvAboutVersion =
                dialogView.findViewById(
                        R.id.tvAboutVersion
                );

        MaterialButton btnCloseAbout =
                dialogView.findViewById(
                        R.id.btnCloseAbout
                );

        tvAboutVersion.setText(
                getString(
                        R.string.about_dialog_version,
                        BuildConfig.VERSION_NAME
                )
        );

        AlertDialog aboutDialog =
                new MaterialAlertDialogBuilder(
                        requireContext()
                )
                        .setView(dialogView)
                        .create();

        btnCloseAbout.setOnClickListener(
                view -> aboutDialog.dismiss()
        );

        aboutDialog.show();
    }

    private void showDeleteAccountDialog() {

        if (!isAdded()) {
            return;
        }

        View dialogView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_delete_account,
                                null
                        );

        TextInputEditText etDeleteConfirmation =
                dialogView.findViewById(
                        R.id.etDeleteConfirmation
                );

        MaterialButton btnCancelDelete =
                dialogView.findViewById(
                        R.id.btnCancelDelete
                );

        MaterialButton btnConfirmDelete =
                dialogView.findViewById(
                        R.id.btnConfirmDelete
                );

        ProgressBar progressDeleteAccount =
                dialogView.findViewById(
                        R.id.progressDeleteAccount
                );

        AlertDialog deleteDialog =
                new MaterialAlertDialogBuilder(
                        requireContext()
                )
                        .setView(dialogView)
                        .create();

        deleteDialog.setCanceledOnTouchOutside(false);

        btnConfirmDelete.setEnabled(false);

        etDeleteConfirmation.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                        // No action needed.
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        boolean confirmed =
                                s != null
                                        && "DELETE".equals(
                                        s.toString().trim()
                                );

                        btnConfirmDelete.setEnabled(
                                confirmed
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {
                        // No action needed.
                    }
                }
        );

        btnCancelDelete.setOnClickListener(
                view -> deleteDialog.dismiss()
        );

        btnConfirmDelete.setOnClickListener(
                view -> {

                    String confirmation =
                            etDeleteConfirmation
                                    .getText() == null
                                    ? ""
                                    : etDeleteConfirmation
                                    .getText()
                                    .toString()
                                    .trim();

                    if (!"DELETE".equals(confirmation)) {
                        return;
                    }

                    performDeleteAccount(
                            deleteDialog,
                            etDeleteConfirmation,
                            btnCancelDelete,
                            btnConfirmDelete,
                            progressDeleteAccount
                    );
                }
        );

        deleteDialog.show();
    }

    private void performDeleteAccount(
            AlertDialog deleteDialog,
            TextInputEditText etDeleteConfirmation,
            MaterialButton btnCancelDelete,
            MaterialButton btnConfirmDelete,
            ProgressBar progressDeleteAccount
    ) {

        etDeleteConfirmation.setEnabled(false);
        btnCancelDelete.setEnabled(false);
        btnConfirmDelete.setEnabled(false);

        progressDeleteAccount.setVisibility(
                View.VISIBLE
        );

        authRepository.deleteAccount(
                new AuthRepository.AuthCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(
                                () -> {

                                    if (deleteDialog.isShowing()) {
                                        deleteDialog.dismiss();
                                    }

                                    Toast.makeText(
                                            requireContext(),
                                            "Account deleted successfully.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    clearCredentialStateAndOpenLogin();
                                }
                        );
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(
                                () -> {

                                    progressDeleteAccount.setVisibility(
                                            View.GONE
                                    );

                                    etDeleteConfirmation.setEnabled(
                                            true
                                    );

                                    btnCancelDelete.setEnabled(
                                            true
                                    );

                                    String currentText =
                                            etDeleteConfirmation
                                                    .getText() == null
                                                    ? ""
                                                    : etDeleteConfirmation
                                                    .getText()
                                                    .toString()
                                                    .trim();

                                    btnConfirmDelete.setEnabled(
                                            "DELETE".equals(
                                                    currentText
                                            )
                                    );

                                    Toast.makeText(
                                            requireContext(),
                                            message == null
                                                    || message.trim().isEmpty()
                                                    ? "Unable to delete account. Please try again."
                                                    : message,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                        );
                    }
                }
        );
    }

    private void performLogout() {

        cardLogout.setEnabled(false);

        authRepository.logout(
                new AuthRepository.AuthCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(
                                ProfileFragment.this
                                        ::clearCredentialStateAndOpenLogin
                        );
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(
                                () -> {

                                    if (message != null
                                            && !message.trim().isEmpty()) {

                                        Toast.makeText(
                                                requireContext(),
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    clearCredentialStateAndOpenLogin();
                                }
                        );
                    }
                }
        );
    }

    private void clearCredentialStateAndOpenLogin() {

        if (!isAdded()) {
            return;
        }

        ClearCredentialStateRequest request =
                new ClearCredentialStateRequest();

        credentialManager.clearCredentialStateAsync(
                request,
                null,
                ContextCompat.getMainExecutor(
                        requireContext()
                ),
                new androidx.credentials.CredentialManagerCallback<
                        Void,
                        ClearCredentialException>() {

                    @Override
                    public void onResult(Void result) {

                        if (!isAdded()) {
                            return;
                        }

                        openLogin();
                    }

                    @Override
                    public void onError(
                            @NonNull ClearCredentialException exception
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        openLogin();
                    }
                }
        );
    }

    private void openLogin() {

        if (!isAdded()) {
            return;
        }

        Intent intent = new Intent(
                requireContext(),
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    private void renderProfileState(
            UiState<UserProfile> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                showLoading();
                break;

            case SUCCESS:
                showProfile(
                        state.getData()
                );
                break;

            case ERROR:
                showError(
                        state.getMessage()
                );
                break;
        }
    }

    private void showLoading() {

        progressProfile.setVisibility(
                View.VISIBLE
        );

        layoutProfileContent.setVisibility(
                View.GONE
        );

        layoutProfileError.setVisibility(
                View.GONE
        );
    }

    private void showProfile(
            UserProfile profile
    ) {

        progressProfile.setVisibility(
                View.GONE
        );

        layoutProfileError.setVisibility(
                View.GONE
        );

        layoutProfileContent.setVisibility(
                View.VISIBLE
        );

        tvProfileInitials.setText(
                profile.getInitials()
        );

        tvProfileName.setText(
                profile.getFullName()
        );

        tvProfileEmail.setText(
                profile.getEmail()
        );
    }

    private void showError(
            String message
    ) {

        progressProfile.setVisibility(
                View.GONE
        );

        layoutProfileContent.setVisibility(
                View.GONE
        );

        layoutProfileError.setVisibility(
                View.VISIBLE
        );

        if (message == null
                || message.trim().isEmpty()) {

            tvProfileError.setText(
                    R.string.profile_error
            );

        } else {

            tvProfileError.setText(message);
        }
    }
}