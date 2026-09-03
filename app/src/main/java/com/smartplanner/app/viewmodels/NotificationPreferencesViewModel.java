package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.NotificationPreferences;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.repositories.NotificationPreferencesRepository;

public class NotificationPreferencesViewModel
        extends AndroidViewModel {

    private final NotificationPreferencesRepository repository;

    private final MutableLiveData<UiState<NotificationPreferences>>
            preferencesState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<NotificationPreferences>>
            updatePreferencesState =
            new MutableLiveData<>();

    public NotificationPreferencesViewModel(
            @NonNull Application application
    ) {
        super(application);

        repository =
                new NotificationPreferencesRepository(
                        application
                );
    }

    public LiveData<UiState<NotificationPreferences>>
    getPreferencesState() {

        return preferencesState;
    }

    public LiveData<UiState<NotificationPreferences>>
    getUpdatePreferencesState() {

        return updatePreferencesState;
    }

    public void loadPreferences() {

        preferencesState.setValue(
                UiState.loading()
        );

        repository.getCurrentPreferences(
                new NotificationPreferencesRepository
                        .NotificationPreferencesCallback<NotificationPreferences>() {

                    @Override
                    public void onSuccess(
                            NotificationPreferences result
                    ) {

                        preferencesState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        preferencesState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updatePreferences(
            boolean notificationsEnabled,
            String defaultTaskReminder,
            String defaultEventReminder
    ) {

        updatePreferencesState.setValue(
                UiState.loading()
        );

        repository.updatePreferences(
                notificationsEnabled,
                defaultTaskReminder,
                defaultEventReminder,
                new NotificationPreferencesRepository
                        .NotificationPreferencesCallback<NotificationPreferences>() {

                    @Override
                    public void onSuccess(
                            NotificationPreferences result
                    ) {

                        updatePreferencesState.postValue(
                                UiState.success(result)
                        );

                        /*
                         * Keep the main preferences state synchronized
                         * with the newly saved data.
                         */
                        preferencesState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        updatePreferencesState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}