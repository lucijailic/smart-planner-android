package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.UserAvailabilityRequest;
import com.smartplanner.app.repositories.UserAvailabilityRepository;

import java.util.List;

public class AvailabilityViewModel
        extends AndroidViewModel {

    private final UserAvailabilityRepository repository;

    private final MutableLiveData<UiState<List<UserAvailability>>>
            availabilityState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<List<UserAvailability>>>
            saveAvailabilityState =
            new MutableLiveData<>();


    public AvailabilityViewModel(
            @NonNull Application application
    ) {

        super(application);

        repository =
                new UserAvailabilityRepository(
                        application
                );
    }


    public LiveData<UiState<List<UserAvailability>>>
    getAvailabilityState() {

        return availabilityState;
    }


    public LiveData<UiState<List<UserAvailability>>>
    getSaveAvailabilityState() {

        return saveAvailabilityState;
    }


    // =========================================================
    // LOAD
    // =========================================================

    public void loadAvailability() {

        availabilityState.setValue(
                UiState.loading()
        );

        repository.getAvailability(
                new UserAvailabilityRepository
                        .UserAvailabilityCallback<List<UserAvailability>>() {

                    @Override
                    public void onSuccess(
                            List<UserAvailability> result
                    ) {

                        availabilityState.postValue(
                                UiState.success(result)
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        availabilityState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }


    // =========================================================
    // SAVE
    // =========================================================

    public void saveAvailability(
            List<UserAvailabilityRequest> requests
    ) {

        saveAvailabilityState.setValue(
                UiState.loading()
        );

        repository.saveAvailability(
                requests,
                new UserAvailabilityRepository
                        .UserAvailabilityCallback<List<UserAvailability>>() {

                    @Override
                    public void onSuccess(
                            List<UserAvailability> result
                    ) {

                        saveAvailabilityState.postValue(
                                UiState.success(result)
                        );

                        availabilityState.postValue(
                                UiState.success(result)
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        saveAvailabilityState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}