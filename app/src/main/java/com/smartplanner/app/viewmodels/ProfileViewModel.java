package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.UserProfile;
import com.smartplanner.app.repositories.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository profileRepository;

    private final MutableLiveData<UiState<UserProfile>>
            profileState = new MutableLiveData<>();

    private final MutableLiveData<UiState<UserProfile>>
            updateProfileState = new MutableLiveData<>();

    public ProfileViewModel(
            @NonNull Application application
    ) {
        super(application);

        profileRepository =
                new ProfileRepository(application);
    }

    public LiveData<UiState<UserProfile>> getProfileState() {
        return profileState;
    }

    public LiveData<UiState<UserProfile>> getUpdateProfileState() {
        return updateProfileState;
    }

    public void loadProfile() {

        profileState.setValue(
                UiState.loading()
        );

        profileRepository.getCurrentUserProfile(
                new ProfileRepository.ProfileCallback<UserProfile>() {

                    @Override
                    public void onSuccess(
                            UserProfile result
                    ) {

                        profileState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        profileState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updateProfile(
            String firstName,
            String lastName
    ) {

        updateProfileState.setValue(
                UiState.loading()
        );

        profileRepository.updateCurrentUserProfile(
                firstName,
                lastName,
                new ProfileRepository.ProfileCallback<UserProfile>() {

                    @Override
                    public void onSuccess(
                            UserProfile result
                    ) {

                        updateProfileState.postValue(
                                UiState.success(result)
                        );

                        /*
                         * Keep the main profile state synchronized
                         * with the updated profile.
                         */
                        profileState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        updateProfileState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}