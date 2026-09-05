package com.smartplanner.app.repositories;

import android.content.Context;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.TaskApi;
import com.smartplanner.app.models.Subtask;
import com.smartplanner.app.models.SubtaskCompletedRequest;
import com.smartplanner.app.models.SubtaskRequest;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.TaskImportantRequest;
import com.smartplanner.app.models.TaskRequest;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.storage.SessionManager;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepository {

    private static final String TASK_SELECT =
            "id,user_id,category_id,title,description,priority,status," +
                    "deadline,estimated_duration,is_important,reminder_type," +
                    "completed_at,created_at,updated_at";

    private static final String SUBTASK_SELECT =
            "id,task_id,user_id,title,is_completed,created_at,updated_at";

    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse(
                    "application/json; charset=utf-8"
            );

    private final TaskApi taskApi;
    private final SessionManager sessionManager;

    public TaskRepository(
            Context context
    ) {

        taskApi =
                ApiClient
                        .getClient(context)
                        .create(TaskApi.class);

        sessionManager =
                SessionManager.getInstance(context);
    }

    public interface TaskCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }

    // =========================================================
    // TASKS
    // =========================================================

    public void getTasks(
            TaskCallback<List<Task>> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        taskApi.getTasks(
                        "eq." + userId,
                        TASK_SELECT,
                        "created_at.desc"
                )
                .enqueue(
                        new Callback<List<Task>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Task>> call,
                                    Response<List<Task>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load tasks."
                                    );

                                    return;
                                }

                                List<Task> tasks =
                                        response.body();

                                if (tasks == null) {

                                    callback.onError(
                                            "Unable to load tasks."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        tasks
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Task>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void getTask(
            String taskId,
            TaskCallback<Task> callback
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        taskApi.getTask(
                        "eq." + taskId,
                        TASK_SELECT
                )
                .enqueue(
                        new Callback<List<Task>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Task>> call,
                                    Response<List<Task>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load task."
                                    );

                                    return;
                                }

                                List<Task> tasks =
                                        response.body();

                                if (tasks == null
                                        || tasks.isEmpty()) {

                                    callback.onError(
                                            "Task not found."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        tasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Task>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
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
            ReminderType reminderType,
            TaskCallback<Task> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        if (title == null
                || title.trim().isEmpty()) {

            callback.onError(
                    "Task title is required."
            );

            return;
        }

        if (title.trim().length() > 150) {

            callback.onError(
                    "Task title is too long."
            );

            return;
        }

        if (estimatedDuration != null
                && estimatedDuration <= 0) {

            callback.onError(
                    "Estimated duration must be greater than 0."
            );

            return;
        }

        ReminderType safeReminderType =
                normalizeReminderType(
                        deadline,
                        reminderType
                );

        TaskPriority safePriority =
                priority != null
                        ? priority
                        : TaskPriority.MEDIUM;

        TaskRequest request =
                new TaskRequest(
                        userId,
                        normalizeNullableString(categoryId),
                        title.trim(),
                        normalizeNullableString(description),
                        safePriority,
                        TaskStatus.TO_DO,
                        normalizeNullableString(deadline),
                        estimatedDuration,
                        important,
                        safeReminderType,
                        null
                );

        taskApi.createTask(
                        request,
                        TASK_SELECT
                )
                .enqueue(
                        new Callback<List<Task>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Task>> call,
                                    Response<List<Task>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to create task."
                                    );

                                    return;
                                }

                                List<Task> tasks =
                                        response.body();

                                if (tasks == null
                                        || tasks.isEmpty()) {

                                    callback.onError(
                                            "Task creation was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        tasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Task>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
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
            String completedAt,
            TaskCallback<Task> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        if (title == null
                || title.trim().isEmpty()) {

            callback.onError(
                    "Task title is required."
            );

            return;
        }

        if (title.trim().length() > 150) {

            callback.onError(
                    "Task title is too long."
            );

            return;
        }

        if (estimatedDuration != null
                && estimatedDuration <= 0) {

            callback.onError(
                    "Estimated duration must be greater than 0."
            );

            return;
        }

        ReminderType safeReminderType =
                normalizeReminderType(
                        deadline,
                        reminderType
                );

        TaskPriority safePriority =
                priority != null
                        ? priority
                        : TaskPriority.MEDIUM;

        JsonObject request =
                new JsonObject();

        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        if (categoryId == null
                || categoryId.trim().isEmpty()) {

            request.add(
                    "category_id",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "category_id",
                    categoryId.trim()
            );
        }

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        request.addProperty(
                "title",
                title.trim()
        );

        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        if (description == null
                || description.trim().isEmpty()) {

            request.add(
                    "description",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "description",
                    description.trim()
            );
        }

        // -----------------------------------------------------
        // PRIORITY
        // -----------------------------------------------------

        request.addProperty(
                "priority",
                safePriority.name()
        );

        // -----------------------------------------------------
        // DEADLINE
        // -----------------------------------------------------

        if (deadline == null
                || deadline.trim().isEmpty()) {

            /*
             * Ovo mora fizički otići prema Supabaseu kao:
             *
             * "deadline": null
             *
             * Ako bi se polje potpuno izostavilo iz PATCH-a,
             * Supabase bi zadržao stari deadline.
             */
            request.add(
                    "deadline",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "deadline",
                    deadline.trim()
            );
        }

        // -----------------------------------------------------
        // ESTIMATED DURATION
        // -----------------------------------------------------

        if (estimatedDuration == null) {

            request.add(
                    "estimated_duration",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "estimated_duration",
                    estimatedDuration
            );
        }

        // -----------------------------------------------------
        // IMPORTANT
        // -----------------------------------------------------

        request.addProperty(
                "is_important",
                important
        );

        // -----------------------------------------------------
        // REMINDER
        // -----------------------------------------------------

        /*
         * Poslovno pravilo:
         *
         * Task bez deadlinea ne smije imati reminder.
         *
         * normalizeReminderType() zato vraća NONE kada
         * deadline ne postoji.
         */
        request.addProperty(
                "reminder_type",
                safeReminderType.name()
        );

        /*
         * Status i completed_at ovdje namjerno ne mijenjamo.
         *
         * Oni se mijenjaju isključivo metodom
         * updateTaskStatus().
         */

        RequestBody requestBody =
                createJsonRequestBody(
                        request
                );

        taskApi.updateTask(
                        "eq." + taskId,
                        TASK_SELECT,
                        requestBody
                )
                .enqueue(
                        new Callback<List<Task>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Task>> call,
                                    Response<List<Task>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update task."
                                    );

                                    return;
                                }

                                List<Task> tasks =
                                        response.body();

                                if (tasks == null
                                        || tasks.isEmpty()) {

                                    callback.onError(
                                            "Task update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        tasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Task>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void updateImportant(
            String taskId,
            boolean important,
            TaskCallback<Task> callback
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        TaskImportantRequest request =
                new TaskImportantRequest(
                        important
                );

        taskApi.updateImportant(
                        "eq." + taskId,
                        TASK_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<Task>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Task>> call,
                                    Response<List<Task>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update important status."
                                    );

                                    return;
                                }

                                List<Task> tasks =
                                        response.body();

                                if (tasks == null
                                        || tasks.isEmpty()) {

                                    callback.onError(
                                            "Task update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        tasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Task>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void updateTaskStatus(
            String taskId,
            TaskStatus status,
            String completedAt,
            TaskCallback<Task> callback
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        if (status == null) {

            callback.onError(
                    "Invalid task status."
            );

            return;
        }

        JsonObject request =
                new JsonObject();

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        request.addProperty(
                "status",
                status.name()
        );

        // -----------------------------------------------------
        // COMPLETED AT
        // -----------------------------------------------------

        if (completedAt == null
                || completedAt.trim().isEmpty()) {

            /*
             * Kod Reopen Task moramo poslati:
             *
             * "completed_at": null
             *
             * kako bi se prethodno vrijeme završetka
             * stvarno obrisalo iz baze.
             */
            request.add(
                    "completed_at",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "completed_at",
                    completedAt.trim()
            );
        }

        RequestBody requestBody =
                createJsonRequestBody(
                        request
                );

        taskApi.updateTaskStatus(
                        "eq." + taskId,
                        TASK_SELECT,
                        requestBody
                )
                .enqueue(
                        new Callback<List<Task>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Task>> call,
                                    Response<List<Task>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update task status."
                                    );

                                    return;
                                }

                                List<Task> tasks =
                                        response.body();

                                if (tasks == null
                                        || tasks.isEmpty()) {

                                    callback.onError(
                                            "Task update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        tasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Task>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void deleteTask(
            String taskId,
            TaskCallback<Void> callback
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        taskApi.deleteTask(
                        "eq." + taskId
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    callback.onSuccess(
                                            null
                                    );

                                } else {

                                    callback.onError(
                                            "Unable to delete task."
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    // =========================================================
    // SUBTASKS
    // =========================================================

    public void getSubtasks(
            String taskId,
            TaskCallback<List<Subtask>> callback
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        taskApi.getSubtasks(
                        "eq." + taskId,
                        SUBTASK_SELECT,
                        "created_at.asc"
                )
                .enqueue(
                        new Callback<List<Subtask>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Subtask>> call,
                                    Response<List<Subtask>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load subtasks."
                                    );

                                    return;
                                }

                                List<Subtask> subtasks =
                                        response.body();

                                if (subtasks == null) {

                                    callback.onError(
                                            "Unable to load subtasks."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        subtasks
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Subtask>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void createSubtask(
            String taskId,
            String title,
            TaskCallback<Subtask> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        if (taskId == null
                || taskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid task."
            );

            return;
        }

        if (title == null
                || title.trim().isEmpty()) {

            callback.onError(
                    "Subtask title is required."
            );

            return;
        }

        if (title.trim().length() > 150) {

            callback.onError(
                    "Subtask title is too long."
            );

            return;
        }

        SubtaskRequest request =
                new SubtaskRequest(
                        taskId,
                        userId,
                        title.trim(),
                        false
                );

        taskApi.createSubtask(
                        request,
                        SUBTASK_SELECT
                )
                .enqueue(
                        new Callback<List<Subtask>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Subtask>> call,
                                    Response<List<Subtask>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to create subtask."
                                    );

                                    return;
                                }

                                List<Subtask> subtasks =
                                        response.body();

                                if (subtasks == null
                                        || subtasks.isEmpty()) {

                                    callback.onError(
                                            "Subtask creation was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        subtasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Subtask>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void updateSubtaskCompleted(
            String subtaskId,
            boolean completed,
            TaskCallback<Subtask> callback
    ) {

        if (subtaskId == null
                || subtaskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid subtask."
            );

            return;
        }

        SubtaskCompletedRequest request =
                new SubtaskCompletedRequest(
                        completed
                );

        taskApi.updateSubtaskCompleted(
                        "eq." + subtaskId,
                        SUBTASK_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<Subtask>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Subtask>> call,
                                    Response<List<Subtask>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update subtask."
                                    );

                                    return;
                                }

                                List<Subtask> subtasks =
                                        response.body();

                                if (subtasks == null
                                        || subtasks.isEmpty()) {

                                    callback.onError(
                                            "Subtask update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        subtasks.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Subtask>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void deleteSubtask(
            String subtaskId,
            TaskCallback<Void> callback
    ) {

        if (subtaskId == null
                || subtaskId.trim().isEmpty()) {

            callback.onError(
                    "Invalid subtask."
            );

            return;
        }

        taskApi.deleteSubtask(
                        "eq." + subtaskId
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    callback.onSuccess(
                                            null
                                    );

                                } else {

                                    callback.onError(
                                            "Unable to delete subtask."
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private ReminderType normalizeReminderType(
            String deadline,
            ReminderType reminderType
    ) {

        if (deadline == null
                || deadline.trim().isEmpty()) {

            return ReminderType.NONE;
        }

        return reminderType != null
                ? reminderType
                : ReminderType.NONE;
    }

    private String normalizeNullableString(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private RequestBody createJsonRequestBody(
            JsonObject jsonObject
    ) {

        return RequestBody.create(
                jsonObject.toString(),
                JSON_MEDIA_TYPE
        );
    }
}