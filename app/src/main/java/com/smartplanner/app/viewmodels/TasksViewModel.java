package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.Subtask;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.repositories.TaskRepository;

import java.util.List;

public class TasksViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;

    private final MutableLiveData<UiState<List<Task>>> tasksState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Task>> taskActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Task>> importantActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Task>> taskDetailsState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Task>> statusActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Boolean>> deleteActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<List<Subtask>>> subtasksState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Subtask>> subtaskActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Boolean>> deleteSubtaskActionState =
            new MutableLiveData<>();

    public TasksViewModel(
            @NonNull Application application
    ) {

        super(application);

        taskRepository =
                new TaskRepository(
                        application
                );
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public LiveData<UiState<List<Task>>> getTasksState() {
        return tasksState;
    }

    public LiveData<UiState<Task>> getTaskActionState() {
        return taskActionState;
    }

    public LiveData<UiState<Task>> getImportantActionState() {
        return importantActionState;
    }

    public LiveData<UiState<Task>> getTaskDetailsState() {
        return taskDetailsState;
    }

    public LiveData<UiState<Task>> getStatusActionState() {
        return statusActionState;
    }

    public LiveData<UiState<Boolean>> getDeleteActionState() {
        return deleteActionState;
    }

    public LiveData<UiState<List<Subtask>>> getSubtasksState() {
        return subtasksState;
    }

    public LiveData<UiState<Subtask>> getSubtaskActionState() {
        return subtaskActionState;
    }

    public LiveData<UiState<Boolean>> getDeleteSubtaskActionState() {
        return deleteSubtaskActionState;
    }

    // =========================================================
    // TASKS
    // =========================================================

    public void loadTasks() {

        tasksState.setValue(
                UiState.loading()
        );

        taskRepository.getTasks(
                new TaskRepository.TaskCallback<List<Task>>() {

                    @Override
                    public void onSuccess(
                            List<Task> result
                    ) {

                        tasksState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        tasksState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void loadTask(
            String taskId
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            taskDetailsState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        taskDetailsState.setValue(
                UiState.loading()
        );

        taskRepository.getTask(
                taskId,
                new TaskRepository.TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task result
                    ) {

                        taskDetailsState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        taskDetailsState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void createTask(
            String categoryId,
            String title,
            String description,
            TaskPriority priority,
            String deadline,
            Integer estimatedDuration,
            boolean important,
            ReminderType reminderType
    ) {

        taskActionState.setValue(
                UiState.loading()
        );

        taskRepository.createTask(
                categoryId,
                title,
                description,
                priority,
                deadline,
                estimatedDuration,
                important,
                reminderType,
                new TaskRepository.TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task result
                    ) {

                        taskActionState.postValue(
                                UiState.success(result)
                        );

                        loadTasks();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        taskActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updateTask(
            String taskId,
            String categoryId,
            String title,
            String description,
            TaskPriority priority,
            TaskStatus status,
            String deadline,
            Integer estimatedDuration,
            boolean important,
            ReminderType reminderType,
            String completedAt
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            taskActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        taskActionState.setValue(
                UiState.loading()
        );

        taskRepository.updateTask(
                taskId,
                categoryId,
                title,
                description,
                priority,
                status,
                deadline,
                estimatedDuration,
                important,
                reminderType,
                completedAt,
                new TaskRepository.TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task result
                    ) {

                        taskActionState.postValue(
                                UiState.success(result)
                        );

                        /*
                         * Osvježavamo detalje taska u ovoj
                         * ViewModel instanci.
                         */
                        if (result != null
                                && result.getId() != null
                                && !result.getId().trim().isEmpty()) {

                            taskDetailsState.postValue(
                                    UiState.success(result)
                            );
                        }

                        loadTasks();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        taskActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updateImportant(
            String taskId,
            boolean important
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            importantActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        importantActionState.setValue(
                UiState.loading()
        );

        taskRepository.updateImportant(
                taskId,
                important,
                new TaskRepository.TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task result
                    ) {

                        importantActionState.postValue(
                                UiState.success(result)
                        );

                        loadTasks();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        importantActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updateTaskStatus(
            String taskId,
            TaskStatus status,
            String completedAt
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            statusActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        if (status == null) {

            statusActionState.setValue(
                    UiState.error(
                            "Invalid task status."
                    )
            );

            return;
        }

        statusActionState.setValue(
                UiState.loading()
        );

        taskRepository.updateTaskStatus(
                taskId,
                status,
                completedAt,
                new TaskRepository.TaskCallback<Task>() {

                    @Override
                    public void onSuccess(
                            Task result
                    ) {

                        /*
                         * Activity koja prikazuje detalje odmah
                         * dobiva novi Task objekt.
                         */
                        statusActionState.postValue(
                                UiState.success(result)
                        );

                        /*
                         * I taskDetailsState držimo sinkroniziranim.
                         */
                        if (result != null) {

                            taskDetailsState.postValue(
                                    UiState.success(result)
                            );
                        }

                        /*
                         * Osvježava listu ako ista ViewModel
                         * instanca negdje prikazuje tasksState.
                         *
                         * TasksFragment dodatno radi loadTasks()
                         * u onResume(), pa imamo sigurno
                         * osvježavanje nakon povratka.
                         */
                        loadTasks();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        statusActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void deleteTask(
            String taskId
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            deleteActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        deleteActionState.setValue(
                UiState.loading()
        );

        taskRepository.deleteTask(
                taskId,
                new TaskRepository.TaskCallback<Void>() {

                    @Override
                    public void onSuccess(
                            Void result
                    ) {

                        deleteActionState.postValue(
                                UiState.success(true)
                        );

                        loadTasks();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        deleteActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    // =========================================================
    // SUBTASKS
    // =========================================================

    public void loadSubtasks(
            String taskId
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            subtasksState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        subtasksState.setValue(
                UiState.loading()
        );

        taskRepository.getSubtasks(
                taskId,
                new TaskRepository.TaskCallback<List<Subtask>>() {

                    @Override
                    public void onSuccess(
                            List<Subtask> result
                    ) {

                        subtasksState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        subtasksState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void createSubtask(
            String taskId,
            String title
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            subtaskActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        if (title == null
                || title.trim().isEmpty()) {

            subtaskActionState.setValue(
                    UiState.error(
                            "Subtask title is required."
                    )
            );

            return;
        }

        if (title.trim().length() > 150) {

            subtaskActionState.setValue(
                    UiState.error(
                            "Subtask title is too long."
                    )
            );

            return;
        }

        subtaskActionState.setValue(
                UiState.loading()
        );

        taskRepository.createSubtask(
                taskId,
                title.trim(),
                new TaskRepository.TaskCallback<Subtask>() {

                    @Override
                    public void onSuccess(
                            Subtask result
                    ) {

                        subtaskActionState.postValue(
                                UiState.success(result)
                        );

                        loadSubtasks(
                                taskId
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        subtaskActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updateSubtaskCompleted(
            String taskId,
            String subtaskId,
            boolean completed
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            subtaskActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        if (subtaskId == null
                || subtaskId.trim().isEmpty()) {

            subtaskActionState.setValue(
                    UiState.error(
                            "Invalid subtask."
                    )
            );

            return;
        }

        subtaskActionState.setValue(
                UiState.loading()
        );

        taskRepository.updateSubtaskCompleted(
                subtaskId,
                completed,
                new TaskRepository.TaskCallback<Subtask>() {

                    @Override
                    public void onSuccess(
                            Subtask result
                    ) {

                        subtaskActionState.postValue(
                                UiState.success(result)
                        );

                        loadSubtasks(
                                taskId
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        subtaskActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void deleteSubtask(
            String taskId,
            String subtaskId
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            deleteSubtaskActionState.setValue(
                    UiState.error(
                            "Invalid task."
                    )
            );

            return;
        }

        if (subtaskId == null
                || subtaskId.trim().isEmpty()) {

            deleteSubtaskActionState.setValue(
                    UiState.error(
                            "Invalid subtask."
                    )
            );

            return;
        }

        deleteSubtaskActionState.setValue(
                UiState.loading()
        );

        taskRepository.deleteSubtask(
                subtaskId,
                new TaskRepository.TaskCallback<Void>() {

                    @Override
                    public void onSuccess(
                            Void result
                    ) {

                        deleteSubtaskActionState.postValue(
                                UiState.success(true)
                        );

                        loadSubtasks(
                                taskId
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        deleteSubtaskActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}