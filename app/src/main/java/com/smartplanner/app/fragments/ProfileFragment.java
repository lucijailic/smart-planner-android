package com.smartplanner.app.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private ProfileViewModel profileViewModel;
    private AuthRepository authRepository;

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
    }

    private void initRepositories() {

        authRepository =
                new AuthRepository(requireContext());
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
                                ProfileFragment.this::openLogin
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

                                    openLogin();
                                }
                        );
                    }
                }
        );
    }

    private void openLogin() {

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