package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.repositories.SmartPlanGenerationRepository;
import com.smartplanner.app.repositories.SmartPlanMoveRepository;
import com.smartplanner.app.repositories.SmartPlanRepository;
import com.smartplanner.app.repositories.TaskRepository;
import com.smartplanner.app.smartplan.SmartPlanResult;
import com.smartplanner.app.smartplan.UnscheduledTask;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SmartPlanViewModel
        extends AndroidViewModel {

    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final SmartPlanRepository smartPlanRepository;

    private final SmartPlanGenerationRepository
            smartPlanGenerationRepository;

    private final SmartPlanMoveRepository
            smartPlanMoveRepository;

    private final TaskRepository taskRepository;


    // =========================================================
    // MAIN SMART PLAN STATE
    // =========================================================

    private final MutableLiveData<UiState<SmartPlan>>
            currentPlanState =
            new MutableLiveData<>();


    // =========================================================
    // SMART PLAN ITEMS
    // =========================================================

    private final MutableLiveData<UiState<List<SmartPlanItem>>>
            smartPlanItemsState =
            new MutableLiveData<>();


    // =========================================================
    // TASKS USED BY SMART PLAN UI
    // =========================================================

    private final MutableLiveData<UiState<List<Task>>>
            tasksState =
            new MutableLiveData<>();


    // =========================================================
    // GENERATE / REGENERATE STATE
    // =========================================================

    private final MutableLiveData<
            UiState<
                    SmartPlanGenerationRepository.GenerationResult>>
            generationState =
            new MutableLiveData<>();


    // =========================================================
    // SESSION ACTION STATE
    //
    // COMPLETE
    // SKIP
    // REMOVE
    // MOVE
    // =========================================================

    private final MutableLiveData<UiState<SessionActionResult>>
            actionState =
            new MutableLiveData<>();


    // =========================================================
    // WARNINGS
    // =========================================================

    private final MutableLiveData<List<String>>
            warnings =
            new MutableLiveData<>(
                    new ArrayList<>()
            );


    // =========================================================
    // UNSCHEDULED TASKS
    // =========================================================

    private final MutableLiveData<List<UnscheduledTask>>
            unscheduledTasks =
            new MutableLiveData<>(
                    new ArrayList<>()
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SmartPlanViewModel(
            @NonNull Application application
    ) {

        super(application);

        smartPlanRepository =
                new SmartPlanRepository(
                        application
                );

        smartPlanGenerationRepository =
                new SmartPlanGenerationRepository(
                        application
                );

        smartPlanMoveRepository =
                new SmartPlanMoveRepository(
                        application
                );

        taskRepository =
                new TaskRepository(
                        application
                );
    }


    // =========================================================
    // SESSION ACTION TYPE
    // =========================================================

    public enum SessionActionType {

        COMPLETE,

        SKIP,

        REMOVE,

        MOVE
    }


    // =========================================================
    // SESSION ACTION RESULT
    // =========================================================

    public static class SessionActionResult {

        private final SessionActionType actionType;

        private final String smartPlanItemId;

        private final SmartPlanItem smartPlanItem;


        public SessionActionResult(
                SessionActionType actionType,
                String smartPlanItemId,
                SmartPlanItem smartPlanItem
        ) {

            this.actionType =
                    actionType;

            this.smartPlanItemId =
                    smartPlanItemId;

            this.smartPlanItem =
                    smartPlanItem;
        }


        public SessionActionType getActionType() {
            return actionType;
        }


        public String getSmartPlanItemId() {
            return smartPlanItemId;
        }


        /*
         * COMPLETE / SKIP / MOVE:
         * returns updated SmartPlanItem.
         *
         * REMOVE:
         * returns null because the item was deleted.
         */
        public SmartPlanItem getSmartPlanItem() {
            return smartPlanItem;
        }
    }


    // =========================================================
    // GETTERS
    // =========================================================

    public LiveData<UiState<SmartPlan>>
    getCurrentPlanState() {

        return currentPlanState;
    }


    public LiveData<UiState<List<SmartPlanItem>>>
    getSmartPlanItemsState() {

        return smartPlanItemsState;
    }


    public LiveData<UiState<List<Task>>>
    getTasksState() {

        return tasksState;
    }


    public LiveData<
            UiState<
                    SmartPlanGenerationRepository.GenerationResult>>
    getGenerationState() {

        return generationState;
    }


    public LiveData<UiState<SessionActionResult>>
    getActionState() {

        return actionState;
    }


    public LiveData<List<String>>
    getWarnings() {

        return warnings;
    }


    public LiveData<List<UnscheduledTask>>
    getUnscheduledTasks() {

        return unscheduledTasks;
    }


    // =========================================================
    // LOAD CURRENT SMART PLAN
    // =========================================================

    public void loadCurrentSmartPlan() {

        currentPlanState.setValue(
                UiState.loading()
        );

        smartPlanRepository.getCurrentSmartPlan(
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlan>() {

                    @Override
                    public void onSuccess(
                            SmartPlan result
                    ) {

                        currentPlanState.postValue(
                                UiState.success(
                                        result
                                )
                        );


                        /*
                         * SUCCESS + null is valid.
                         *
                         * User simply has no Smart Plan yet.
                         */
                        if (result == null) {

                            smartPlanItemsState.postValue(
                                    UiState.success(
                                            new ArrayList<>()
                                    )
                            );

                            return;
                        }


                        if (!hasValidSmartPlanId(
                                result
                        )) {

                            smartPlanItemsState.postValue(
                                    UiState.error(
                                            "Invalid Smart Plan."
                                    )
                            );

                            return;
                        }


                        loadSmartPlanItems(
                                result.getId()
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        currentPlanState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // LOAD SMART PLAN ITEMS
    // =========================================================

    public void loadSmartPlanItems(
            String smartPlanId
    ) {

        if (smartPlanId == null
                || smartPlanId
                .trim()
                .isEmpty()) {

            smartPlanItemsState.setValue(
                    UiState.error(
                            "Invalid Smart Plan."
                    )
            );

            return;
        }


        smartPlanItemsState.setValue(
                UiState.loading()
        );


        smartPlanRepository.getSmartPlanItems(
                smartPlanId,
                new SmartPlanRepository
                        .SmartPlanCallback<
                        List<SmartPlanItem>>() {

                    @Override
                    public void onSuccess(
                            List<SmartPlanItem> result
                    ) {

                        List<SmartPlanItem> safeResult =
                                result != null
                                        ? result
                                        : new ArrayList<>();


                        smartPlanItemsState.postValue(
                                UiState.success(
                                        safeResult
                                )
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        smartPlanItemsState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // LOAD TASKS FOR SMART PLAN UI
    // =========================================================

    public void loadTasks() {

        tasksState.setValue(
                UiState.loading()
        );


        taskRepository.getTasks(
                new TaskRepository
                        .TaskCallback<List<Task>>() {

                    @Override
                    public void onSuccess(
                            List<Task> result
                    ) {

                        List<Task> safeResult =
                                result != null
                                        ? result
                                        : new ArrayList<>();


                        tasksState.postValue(
                                UiState.success(
                                        safeResult
                                )
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        tasksState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // GENERATE SMART PLAN
    // =========================================================

    public void generateSmartPlan() {

        if (isGenerationInProgress()) {
            return;
        }


        generationState.setValue(
                UiState.loading()
        );


        smartPlanGenerationRepository.generateSmartPlan(
                new SmartPlanGenerationRepository
                        .SmartPlanGenerationCallback() {

                    @Override
                    public void onSuccess(
                            SmartPlanGenerationRepository
                                    .GenerationResult result
                    ) {

                        handleGenerationSuccess(
                                result
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        generationState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // REGENERATE SMART PLAN
    // =========================================================

    public void regenerateSmartPlan() {

        if (isGenerationInProgress()) {
            return;
        }


        generationState.setValue(
                UiState.loading()
        );


        smartPlanGenerationRepository.regenerateSmartPlan(
                new SmartPlanGenerationRepository
                        .SmartPlanGenerationCallback() {

                    @Override
                    public void onSuccess(
                            SmartPlanGenerationRepository
                                    .GenerationResult result
                    ) {

                        handleGenerationSuccess(
                                result
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        generationState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // COMPLETE SESSION
    // =========================================================

    public void completeSession(
            String smartPlanItemId
    ) {

        if (!isValidItemId(
                smartPlanItemId
        )) {

            actionState.setValue(
                    UiState.error(
                            "Invalid Smart Plan session."
                    )
            );

            return;
        }


        if (isActionInProgress()) {
            return;
        }


        actionState.setValue(
                UiState.loading()
        );


        ZonedDateTime completedAt =
                ZonedDateTime.now();


        smartPlanRepository.completeSmartPlanItem(
                smartPlanItemId,
                completedAt,
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlanItem>() {

                    @Override
                    public void onSuccess(
                            SmartPlanItem result
                    ) {

                        synchronizeTaskAfterSessionCompletion(
                                smartPlanItemId,
                                result
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        actionState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // SYNCHRONIZE TASK AFTER COMPLETED SESSION
    // =========================================================

    private void synchronizeTaskAfterSessionCompletion(
            String smartPlanItemId,
            SmartPlanItem completedItem
    ) {

        if (completedItem == null
                || completedItem.getTaskId() == null
                || completedItem
                .getTaskId()
                .trim()
                .isEmpty()) {

            finishCompleteSessionSuccess(
                    smartPlanItemId,
                    completedItem
            );

            return;
        }


        String taskId =
                completedItem.getTaskId();


        taskRepository.getTask(
                taskId,
                new TaskRepository
                        .TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task task
                    ) {

                        if (task == null
                                || task.getStatus() == null) {

                            finishCompleteSessionSuccess(
                                    smartPlanItemId,
                                    completedItem
                            );

                            return;
                        }


                        // =====================================
                        // TO_DO -> IN_PROGRESS
                        // =====================================

                        if (task.getStatus()
                                == TaskStatus.TO_DO) {

                            updateTaskToInProgress(
                                    taskId,
                                    smartPlanItemId,
                                    completedItem
                            );

                            return;
                        }


                        // =====================================
                        // IN_PROGRESS / COMPLETED / OTHER
                        //
                        // No Task status change required.
                        // =====================================

                        finishCompleteSessionSuccess(
                                smartPlanItemId,
                                completedItem
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        /*
                         * Session completion already succeeded.
                         *
                         * Do not roll it back if secondary Task
                         * synchronization fails.
                         */
                        finishCompleteSessionSuccess(
                                smartPlanItemId,
                                completedItem
                        );
                    }
                }
        );
    }


    // =========================================================
    // TASK: TO_DO -> IN_PROGRESS
    // =========================================================

    private void updateTaskToInProgress(
            String taskId,
            String smartPlanItemId,
            SmartPlanItem completedItem
    ) {

        taskRepository.updateTaskStatus(
                taskId,
                TaskStatus.IN_PROGRESS,
                null,
                new TaskRepository
                        .TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task result
                    ) {

                        finishCompleteSessionSuccess(
                                smartPlanItemId,
                                completedItem
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        finishCompleteSessionSuccess(
                                smartPlanItemId,
                                completedItem
                        );
                    }
                }
        );
    }


    // =========================================================
    // FINISH COMPLETE SESSION SUCCESS
    // =========================================================

    private void finishCompleteSessionSuccess(
            String smartPlanItemId,
            SmartPlanItem completedItem
    ) {

        actionState.postValue(
                UiState.success(
                        new SessionActionResult(
                                SessionActionType.COMPLETE,
                                smartPlanItemId,
                                completedItem
                        )
                )
        );


        refreshCurrentPlanItems();
    }


    // =========================================================
    // SKIP SESSION
    // =========================================================

    public void skipSession(
            String smartPlanItemId
    ) {

        if (!isValidItemId(
                smartPlanItemId
        )) {

            actionState.setValue(
                    UiState.error(
                            "Invalid Smart Plan session."
                    )
            );

            return;
        }


        if (isActionInProgress()) {
            return;
        }


        actionState.setValue(
                UiState.loading()
        );


        smartPlanRepository.skipSmartPlanItem(
                smartPlanItemId,
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlanItem>() {

                    @Override
                    public void onSuccess(
                            SmartPlanItem result
                    ) {

                        actionState.postValue(
                                UiState.success(
                                        new SessionActionResult(
                                                SessionActionType.SKIP,
                                                smartPlanItemId,
                                                result
                                        )
                                )
                        );


                        refreshCurrentPlanItems();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        actionState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // REMOVE SESSION
    // =========================================================

    public void removeSession(
            String smartPlanItemId
    ) {

        if (!isValidItemId(
                smartPlanItemId
        )) {

            actionState.setValue(
                    UiState.error(
                            "Invalid Smart Plan session."
                    )
            );

            return;
        }


        if (isActionInProgress()) {
            return;
        }


        actionState.setValue(
                UiState.loading()
        );


        smartPlanRepository.deleteSmartPlanItem(
                smartPlanItemId,
                new SmartPlanRepository
                        .SmartPlanCallback<Void>() {

                    @Override
                    public void onSuccess(
                            Void result
                    ) {

                        actionState.postValue(
                                UiState.success(
                                        new SessionActionResult(
                                                SessionActionType.REMOVE,
                                                smartPlanItemId,
                                                null
                                        )
                                )
                        );


                        refreshCurrentPlanItems();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        actionState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // MOVE SESSION
    //
    // Full validation is delegated to
    // SmartPlanMoveRepository.
    // =========================================================

    public void moveSession(
            String smartPlanItemId,
            ZonedDateTime newStart
    ) {

        if (!isValidItemId(
                smartPlanItemId
        )) {

            actionState.setValue(
                    UiState.error(
                            "Invalid Smart Plan session."
                    )
            );

            return;
        }


        if (newStart == null) {

            actionState.setValue(
                    UiState.error(
                            "New session time is required."
                    )
            );

            return;
        }


        if (isActionInProgress()) {
            return;
        }


        actionState.setValue(
                UiState.loading()
        );


        smartPlanMoveRepository.moveSession(
                smartPlanItemId,
                newStart,
                new SmartPlanMoveRepository
                        .SmartPlanMoveCallback() {

                    @Override
                    public void onSuccess(
                            SmartPlanItem movedItem
                    ) {

                        actionState.postValue(
                                UiState.success(
                                        new SessionActionResult(
                                                SessionActionType.MOVE,
                                                smartPlanItemId,
                                                movedItem
                                        )
                                )
                        );


                        refreshCurrentPlanItems();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        actionState.postValue(
                                UiState.error(
                                        message
                                )
                        );
                    }
                }
        );
    }


    // =========================================================
    // HANDLE GENERATE / REGENERATE SUCCESS
    // =========================================================

    private void handleGenerationSuccess(
            SmartPlanGenerationRepository
                    .GenerationResult result
    ) {

        if (result == null) {

            generationState.postValue(
                    UiState.error(
                            "Unable to generate Smart Plan."
                    )
            );

            return;
        }


        updateAlgorithmOutput(
                result.getAlgorithmResult()
        );


        generationState.postValue(
                UiState.success(
                        result
                )
        );


        // =====================================================
        // NEW PLAN CREATED
        // =====================================================

        if (result.isPlanCreated()
                && result.getSmartPlan() != null) {

            currentPlanState.postValue(
                    UiState.success(
                            result.getSmartPlan()
                    )
            );


            List<SmartPlanItem> items =
                    result.getSmartPlanItems() != null
                            ? result.getSmartPlanItems()
                            : new ArrayList<>();


            smartPlanItemsState.postValue(
                    UiState.success(
                            items
                    )
            );


            return;
        }


        // =====================================================
        // NO NEW PLAN CREATED
        // =====================================================

        if (result.getSmartPlan() != null) {

            currentPlanState.postValue(
                    UiState.success(
                            result.getSmartPlan()
                    )
            );
        }
    }


    // =========================================================
    // UPDATE WARNINGS + UNSCHEDULED TASKS
    // =========================================================

    private void updateAlgorithmOutput(
            SmartPlanResult result
    ) {

        if (result == null) {

            warnings.postValue(
                    new ArrayList<>()
            );

            unscheduledTasks.postValue(
                    new ArrayList<>()
            );

            return;
        }


        List<String> resultWarnings =
                result.getWarnings() != null
                        ? new ArrayList<>(
                        result.getWarnings()
                )
                        : new ArrayList<>();


        List<UnscheduledTask> resultUnscheduled =
                result.getUnscheduledTasks() != null
                        ? new ArrayList<>(
                        result.getUnscheduledTasks()
                )
                        : new ArrayList<>();


        warnings.postValue(
                resultWarnings
        );

        unscheduledTasks.postValue(
                resultUnscheduled
        );
    }


    // =========================================================
    // REFRESH CURRENT PLAN ITEMS
    // =========================================================

    private void refreshCurrentPlanItems() {

        UiState<SmartPlan> state =
                currentPlanState.getValue();


        if (state == null) {
            return;
        }


        if (state.getStatus()
                != UiState.Status.SUCCESS) {

            return;
        }


        SmartPlan currentPlan =
                state.getData();


        if (!hasValidSmartPlanId(
                currentPlan
        )) {

            return;
        }


        loadSmartPlanItems(
                currentPlan.getId()
        );
    }


    // =========================================================
    // REFRESH FULL SMART PLAN
    // =========================================================

    public void refreshSmartPlan() {

        loadCurrentSmartPlan();
    }


    // =========================================================
    // CLEAR GENERATION OUTPUT
    // =========================================================

    public void clearGenerationOutput() {

        warnings.setValue(
                new ArrayList<>()
        );

        unscheduledTasks.setValue(
                new ArrayList<>()
        );
    }


    // =========================================================
    // GENERATION GUARD
    // =========================================================

    private boolean isGenerationInProgress() {

        UiState<
                SmartPlanGenerationRepository.GenerationResult>
                currentState =
                generationState.getValue();


        return currentState != null
                && currentState.getStatus()
                == UiState.Status.LOADING;
    }


    // =========================================================
    // ACTION GUARD
    // =========================================================

    private boolean isActionInProgress() {

        UiState<SessionActionResult> currentState =
                actionState.getValue();


        return currentState != null
                && currentState.getStatus()
                == UiState.Status.LOADING;
    }


    // =========================================================
    // VALIDATION HELPERS
    // =========================================================

    private boolean isValidItemId(
            String smartPlanItemId
    ) {

        return smartPlanItemId != null
                && !smartPlanItemId
                .trim()
                .isEmpty();
    }


    private boolean hasValidSmartPlanId(
            SmartPlan smartPlan
    ) {

        return smartPlan != null
                && smartPlan.getId() != null
                && !smartPlan
                .getId()
                .trim()
                .isEmpty();
    }
}