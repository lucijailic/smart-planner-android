package com.smartplanner.app.api;

import com.smartplanner.app.models.Subtask;
import com.smartplanner.app.models.SubtaskCompletedRequest;
import com.smartplanner.app.models.SubtaskRequest;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.TaskImportantRequest;
import com.smartplanner.app.models.TaskRequest;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TaskApi {

    // =========================================================
    // TASKS
    // =========================================================

    @GET("rest/v1/tasks")
    Call<List<Task>> getTasks(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/tasks")
    Call<List<Task>> getTask(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/tasks")
    Call<List<Task>> createTask(
            @Body TaskRequest request,
            @Query("select") String select
    );

    /*
     * RequestBody koristimo kod updatea kako bismo mogli
     * eksplicitno poslati JSON null vrijednosti.
     *
     * Primjer:
     * "deadline": null
     *
     * Kod PATCH zahtjeva:
     *
     * polje nije poslano -> postojeća vrijednost ostaje
     * polje je null      -> postojeća vrijednost se briše
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/tasks")
    Call<List<Task>> updateTask(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/tasks")
    Call<List<Task>> updateImportant(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body TaskImportantRequest request
    );

    /*
     * I ovdje koristimo RequestBody jer kod Reopen Task
     * moramo moći poslati:
     *
     * "completed_at": null
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/tasks")
    Call<List<Task>> updateTaskStatus(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body RequestBody requestBody
    );

    @DELETE("rest/v1/tasks")
    Call<Void> deleteTask(
            @Query("id") String idFilter
    );

    // =========================================================
    // SUBTASKS
    // =========================================================

    @GET("rest/v1/subtasks")
    Call<List<Subtask>> getSubtasks(
            @Query("task_id") String taskFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/subtasks")
    Call<List<Subtask>> createSubtask(
            @Body SubtaskRequest request,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/subtasks")
    Call<List<Subtask>> updateSubtaskCompleted(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body SubtaskCompletedRequest request
    );

    @DELETE("rest/v1/subtasks")
    Call<Void> deleteSubtask(
            @Query("id") String idFilter
    );
}