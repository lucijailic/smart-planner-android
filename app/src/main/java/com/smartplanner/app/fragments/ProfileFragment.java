package com.smartplanner.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.auth.LoginActivity;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.UserProfile;
import com.smartplanner.app.repositories.AuthRepository;
import com.smartplanner.app.viewmodels.ProfileViewModel;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.smartplanner.app.activities.profile.EditProfileActivity;
import com.smartplanner.app.activities.profile.CategoriesActivity;
import com.smartplanner.app.activities.profile.NotificationsActivity;
import com.smartplanner.app.activities.profile.SettingsActivity;

public class ProfileFragment extends Fragment {

    private ProgressBar progressProfile;

    private LinearLayout layoutProfileContent;
    private LinearLayout layoutProfileError;

    private TextView tvProfileInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileError;

    private MaterialButton btnRetryProfile;

    private MaterialCardView cardLogout;

    private ProfileViewModel profileViewModel;
    private AuthRepository authRepository;

    private MaterialCardView cardEditProfile;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    private MaterialCardView cardCategories;

    private MaterialCardView cardNotifications;

    private MaterialCardView cardSettings;

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
        setupViewModel();
        setupListeners();
        setupActivityResultLaunchers();
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

        cardLogout =
                view.findViewById(R.id.cardLogout);

        cardEditProfile =
                view.findViewById(R.id.cardEditProfile);

        cardCategories =
                view.findViewById(R.id.cardCategories);

        cardNotifications =
                view.findViewById(R.id.cardNotifications);

        cardSettings =
                view.findViewById(R.id.cardSettings);
    }

    private void initRepositories() {

        authRepository =
                new AuthRepository(requireContext());
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

        cardLogout.setOnClickListener(
                view -> performLogout()
        );

        cardEditProfile.setOnClickListener(
                view -> {

                    Intent intent = new Intent(
                            requireContext(),
                            EditProfileActivity.class
                    );

                    editProfileLauncher.launch(intent);
                }
        );

        cardCategories.setOnClickListener(
                view -> {

                    Intent intent = new Intent(
                            requireContext(),
                            CategoriesActivity.class
                    );

                    startActivity(intent);
                }
        );

        cardNotifications.setOnClickListener(
                view -> {

                    Intent intent = new Intent(
                            requireContext(),
                            NotificationsActivity.class
                    );

                    startActivity(intent);
                }
        );

        cardSettings.setOnClickListener(
                view -> {

                    Intent intent = new Intent(
                            requireContext(),
                            SettingsActivity.class
                    );

                    startActivity(intent);
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
                                () -> openLogin()
                        );
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(
                                () -> {

                                    Toast.makeText(
                                            requireContext(),
                                            message,
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    /*
                                     * AuthRepository already clears
                                     * the local session even if the
                                     * Supabase logout request fails.
                                     */
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
}