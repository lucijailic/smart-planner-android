package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.repositories.PlanningPreferencesRepository;

public class PlanningPreferencesViewModel
        extends AndroidViewModel {

    private final PlanningPreferencesRepository repository;


    private final MutableLiveData<UiState<PlanningPreferences>>
            preferencesState =
            new MutableLiveData<>();


    private final MutableLiveData<UiState<PlanningPreferences>>
            savePreferencesState =
            new MutableLiveData<>();


    public PlanningPreferencesViewModel(
            @NonNull Application application
    ) {
        super(application);

        repository =
                new PlanningPreferencesRepository(
                        application
                );
    }


    public LiveData<UiState<PlanningPreferences>>
    getPreferencesState() {

        return preferencesState;
    }


    public LiveData<UiState<PlanningPreferences>>
    getSavePreferencesState() {

        return savePreferencesState;
    }


    // =========================================================
    // LOAD
    //
    // Success with null means:
    // Smart Plan preferences do not exist yet.
    // =========================================================

    public void loadPreferences() {

        preferencesState.setValue(
                UiState.loading()
        );


        repository.getCurrentPreferences(
                new PlanningPreferencesRepository
                        .PlanningPreferencesCallback<PlanningPreferences>() {

                    @Override
                    public void onSuccess(
                            PlanningPreferences result
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


    // =========================================================
    // CREATE
    // =========================================================

    public void createPreferences(
            int maxDailyMinutes,
            int preferredSessionMinutes,
            int breakMinutes
    ) {

        savePreferencesState.setValue(
                UiState.loading()
        );


        repository.createPreferences(
                maxDailyMinutes,
                preferredSessionMinutes,
                breakMinutes,
                new PlanningPreferencesRepository
                        .PlanningPreferencesCallback<PlanningPreferences>() {

                    @Override
                    public void onSuccess(
                            PlanningPreferences result
                    ) {

                        savePreferencesState.postValue(
                                UiState.success(result)
                        );


                        /*
                         * Keep the main state synchronized
                         * with the newly created preferences.
                         */
                        preferencesState.postValue(
                                UiState.success(result)
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        savePreferencesState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void updatePreferences(
            int maxDailyMinutes,
            int preferredSessionMinutes,
            int breakMinutes
    ) {

        savePreferencesState.setValue(
                UiState.loading()
        );


        repository.updatePreferences(
                maxDailyMinutes,
                preferredSessionMinutes,
                breakMinutes,
                new PlanningPreferencesRepository
                        .PlanningPreferencesCallback<PlanningPreferences>() {

                    @Override
                    public void onSuccess(
                            PlanningPreferences result
                    ) {

                        savePreferencesState.postValue(
                                UiState.success(result)
                        );


                        /*
                         * Keep the loaded preferences synchronized
                         * with the newly saved values.
                         */
                        preferencesState.postValue(
                                UiState.success(result)
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        savePreferencesState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}