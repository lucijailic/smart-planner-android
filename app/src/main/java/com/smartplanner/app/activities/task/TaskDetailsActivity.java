package com.smartplanner.app.activities.task;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.adapters.SubtaskAdapter;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Subtask;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.viewmodels.CategoriesViewModel;
import com.smartplanner.app.viewmodels.TasksViewModel;
import com.smartplanner.app.notifications.TaskReminderManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TaskDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private String taskId;

    private TasksViewModel tasksViewModel;
    private CategoriesViewModel categoriesViewModel;

    private Task currentTask;

    private boolean subtaskCompletionUpdateInProgress = false;

    private final Map<String, Category> categoryMap = new HashMap<>();

    private ProgressBar progressTaskDetails;
    private ProgressBar progressSubtasks;

    private NestedScrollView scrollTaskDetails;

    private LinearLayout layoutTaskDetailsError;

    private TextView tvTaskDetailsError;

    private MaterialButton btnRetryTaskDetails;

    private TextView tvTaskDetailsTitle;
    private TextView tvTaskDetailsImportant;

    private TextView tvTaskDetailsStatusBadge;
    private TextView tvTaskDetailsPriorityBadge;
    private TextView tvTaskHeroDuration;

    private LinearLayout layoutTaskHeroDuration;

    private FrameLayout layoutTaskCategoryIcon;
    private ImageView ivTaskCategoryIcon;

    private TextView tvTaskDetailsCategory;
    private TextView tvTaskDetailsStatus;
    private TextView tvTaskDetailsPriority;
    private TextView tvTaskDetailsDeadline;
    private TextView tvTaskDetailsDuration;
    private TextView tvTaskDetailsReminder;
    private TextView tvTaskDetailsDescription;

    private TextView tvSubtasksTitle;
    private TextView tvSubtasksEmpty;
    private TextView tvSubtasksError;

    private MaterialCardView cardTaskDescription;

    private MaterialButton btnStartTask;
    private MaterialButton btnCompleteTask;
    private MaterialButton btnReopenTask;
    private MaterialButton btnEditTask;
    private MaterialButton btnDeleteTask;
    private MaterialButton btnAddSubtask;

    private RecyclerView recyclerSubtasks;
    private SubtaskAdapter subtaskAdapter;

    private LinearLayout layoutSubtaskProgress;

    private TextView tvSubtaskProgressCount;
    private TextView tvSubtaskProgressPercent;

    private LinearProgressIndicator progressSubtaskCompletion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_details);

        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);

        if (taskId == null || taskId.trim().isEmpty()) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupViewModels();
        setupListeners();

        observeTask();
        observeCategories();
        observeStatusAction();
        observeDeleteAction();
        observeSubtasks();
        observeSubtaskAction();
        observeDeleteSubtaskAction();

        categoriesViewModel.loadCategories();
    }

    private void initViews() {

        progressTaskDetails =
                findViewById(R.id.progressTaskDetails);

        progressSubtasks =
                findViewById(R.id.progressSubtasks);

        scrollTaskDetails =
                findViewById(R.id.scrollTaskDetails);

        layoutTaskDetailsError =
                findViewById(R.id.layoutTaskDetailsError);

        tvTaskDetailsError =
                findViewById(R.id.tvTaskDetailsError);

        btnRetryTaskDetails =
                findViewById(R.id.btnRetryTaskDetails);

        tvTaskDetailsTitle =
                findViewById(R.id.tvTaskDetailsTitle);

        tvTaskDetailsImportant =
                findViewById(R.id.tvTaskDetailsImportant);

        tvTaskDetailsStatusBadge =
                findViewById(R.id.tvTaskDetailsStatusBadge);

        tvTaskDetailsPriorityBadge =
                findViewById(R.id.tvTaskDetailsPriorityBadge);

        tvTaskHeroDuration =
                findViewById(R.id.tvTaskHeroDuration);

        layoutTaskHeroDuration =
                findViewById(R.id.layoutTaskHeroDuration);

        layoutTaskCategoryIcon =
                findViewById(R.id.layoutTaskCategoryIcon);

        ivTaskCategoryIcon =
                findViewById(R.id.ivTaskCategoryIcon);

        tvTaskDetailsCategory =
                findViewById(R.id.tvTaskDetailsCategory);

        tvTaskDetailsStatus =
                findViewById(R.id.tvTaskDetailsStatus);

        tvTaskDetailsPriority =
                findViewById(R.id.tvTaskDetailsPriority);

        tvTaskDetailsDeadline =
                findViewById(R.id.tvTaskDetailsDeadline);

        tvTaskDetailsDuration =
                findViewById(R.id.tvTaskDetailsDuration);

        tvTaskDetailsReminder =
                findViewById(R.id.tvTaskDetailsReminder);

        tvTaskDetailsDescription =
                findViewById(R.id.tvTaskDetailsDescription);

        tvSubtasksTitle =
                findViewById(R.id.tvSubtasksTitle);

        tvSubtasksEmpty =
                findViewById(R.id.tvSubtasksEmpty);

        tvSubtasksError =
                findViewById(R.id.tvSubtasksError);

        cardTaskDescription =
                findViewById(R.id.cardTaskDescription);

        recyclerSubtasks =
                findViewById(R.id.recyclerSubtasks);

        btnStartTask =
                findViewById(R.id.btnStartTask);

        btnCompleteTask =
                findViewById(R.id.btnCompleteTask);

        btnReopenTask =
                findViewById(R.id.btnReopenTask);

        btnEditTask =
                findViewById(R.id.btnEditTask);

        btnDeleteTask =
                findViewById(R.id.btnDeleteTask);

        btnAddSubtask =
                findViewById(R.id.btnAddSubtask);

        layoutSubtaskProgress =
                findViewById(R.id.layoutSubtaskProgress);

        tvSubtaskProgressCount =
                findViewById(R.id.tvSubtaskProgressCount);

        tvSubtaskProgressPercent =
                findViewById(R.id.tvSubtaskProgressPercent);

        progressSubtaskCompletion =
                findViewById(R.id.progressSubtaskCompletion);
    }

    private void setupRecyclerView() {

        subtaskAdapter =
                new SubtaskAdapter(
                        new SubtaskAdapter.OnSubtaskActionListener() {

                            @Override
                            public void onSubtaskCheckedChanged(
                                    Subtask subtask,
                                    boolean completed
                            ) {

                                updateSubtaskCompleted(
                                        subtask,
                                        completed
                                );
                            }

                            @Override
                            public void onDeleteSubtaskClick(
                                    Subtask subtask
                            ) {

                                showDeleteSubtaskConfirmation(
                                        subtask
                                );
                            }
                        }
                );

        recyclerSubtasks.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerSubtasks.setAdapter(
                subtaskAdapter
        );

        recyclerSubtasks.setNestedScrollingEnabled(true);
    }

    private void setupViewModels() {

        tasksViewModel =
                new ViewModelProvider(this)
                        .get(TasksViewModel.class);

        categoriesViewModel =
                new ViewModelProvider(this)
                        .get(CategoriesViewModel.class);
    }

    private void setupListeners() {

        findViewById(
                R.id.btnTaskDetailsBack
        ).setOnClickListener(
                view -> finish()
        );

        btnRetryTaskDetails.setOnClickListener(
                view -> {

                    tasksViewModel.loadTask(taskId);
                    tasksViewModel.loadSubtasks(taskId);
                }
        );

        btnStartTask.setOnClickListener(
                view -> startTask()
        );

        btnCompleteTask.setOnClickListener(
                view -> completeTask()
        );

        btnReopenTask.setOnClickListener(
                view -> reopenTask()
        );

        btnEditTask.setOnClickListener(
                view -> openEditTask()
        );

        btnDeleteTask.setOnClickListener(
                view -> showDeleteConfirmation()
        );

        btnAddSubtask.setOnClickListener(
                view -> showAddSubtaskDialog()
        );
    }

    private void observeTask() {

        tasksViewModel
                .getTaskDetailsState()
                .observe(
                        this,
                        this::handleTaskState
                );
    }

    private void observeCategories() {

        categoriesViewModel
                .getCategoriesState()
                .observe(
                        this,
                        this::handleCategoriesState
                );
    }

    private void observeStatusAction() {

        tasksViewModel
                .getStatusActionState()
                .observe(
                        this,
                        this::handleStatusActionState
                );
    }

    private void observeDeleteAction() {

        tasksViewModel
                .getDeleteActionState()
                .observe(
                        this,
                        this::handleDeleteActionState
                );
    }

    private void observeSubtasks() {

        tasksViewModel
                .getSubtasksState()
                .observe(
                        this,
                        this::handleSubtasksState
                );
    }

    private void observeSubtaskAction() {

        tasksViewModel
                .getSubtaskActionState()
                .observe(
                        this,
                        this::handleSubtaskActionState
                );
    }

    private void observeDeleteSubtaskAction() {

        tasksViewModel
                .getDeleteSubtaskActionState()
                .observe(
                        this,
                        this::handleDeleteSubtaskActionState
                );
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (tasksViewModel != null
                && taskId != null
                && !taskId.trim().isEmpty()) {

            tasksViewModel.loadTask(taskId);
            tasksViewModel.loadSubtasks(taskId);
        }
    }

    private void handleTaskState(
            UiState<Task> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                showLoading();

                break;

            case SUCCESS:

                currentTask =
                        state.getData();

                if (currentTask == null) {

                    showError(
                            "Task not found."
                    );

                    return;
                }

                showTask(
                        currentTask
                );

                break;

            case ERROR:

                showError(
                        state.getMessage()
                );

                break;
        }
    }

    private void handleCategoriesState(
            UiState<List<Category>> state
    ) {

        if (state == null
                || state.getStatus()
                != UiState.Status.SUCCESS) {

            return;
        }

        categoryMap.clear();

        List<Category> categories =
                state.getData();

        if (categories != null) {

            for (Category category : categories) {

                if (category != null
                        && category.getId() != null) {

                    categoryMap.put(
                            category.getId(),
                            category
                    );
                }
            }
        }

        updateCategory();
    }

    private void handleStatusActionState(
            UiState<Task> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                setActionButtonsEnabled(
                        false
                );

                break;

            case SUCCESS:

                setActionButtonsEnabled(
                        true
                );

                currentTask =
                        state.getData();

                if (currentTask != null) {

                    TaskReminderManager.updateTaskReminder(
                            this,
                            currentTask
                    );

                    showTask(
                            currentTask
                    );
                }

                Toast.makeText(
                        this,
                        "Task status updated.",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case ERROR:

                setActionButtonsEnabled(
                        true
                );

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Unable to update task status.";
                }

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void handleDeleteActionState(
            UiState<Boolean> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                setActionButtonsEnabled(
                        false
                );

                break;

            case SUCCESS:

                TaskReminderManager.cancelTaskReminder(
                        this,
                        taskId
                );

                Toast.makeText(
                        this,
                        "Task deleted.",
                        Toast.LENGTH_SHORT
                ).show();

                setResult(
                        RESULT_OK
                );

                finish();

                break;

            case ERROR:

                setActionButtonsEnabled(
                        true
                );

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Unable to delete task.";
                }

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void handleSubtasksState(
            UiState<List<Subtask>> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                progressSubtasks.setVisibility(
                        View.VISIBLE
                );

                recyclerSubtasks.setVisibility(
                        View.GONE
                );

                tvSubtasksEmpty.setVisibility(
                        View.GONE
                );

                tvSubtasksError.setVisibility(
                        View.GONE
                );

                layoutSubtaskProgress.setVisibility(
                        View.GONE
                );

                break;

            case SUCCESS:

                progressSubtasks.setVisibility(
                        View.GONE
                );

                tvSubtasksError.setVisibility(
                        View.GONE
                );

                List<Subtask> subtasks =
                        state.getData();

                subtaskAdapter.setSubtasks(
                        subtasks
                );

                updateSubtasksTitle(
                        subtasks
                );

                if (subtasks == null
                        || subtasks.isEmpty()) {

                    recyclerSubtasks.setVisibility(
                            View.GONE
                    );

                    tvSubtasksEmpty.setVisibility(
                            View.VISIBLE
                    );

                    layoutSubtaskProgress.setVisibility(
                            View.GONE
                    );

                } else {

                    tvSubtasksEmpty.setVisibility(
                            View.GONE
                    );

                    recyclerSubtasks.setVisibility(
                            View.VISIBLE
                    );

                    layoutSubtaskProgress.setVisibility(
                            View.VISIBLE
                    );

                    updateSubtaskProgress(
                            subtasks
                    );
                }

                break;

            case ERROR:

                progressSubtasks.setVisibility(
                        View.GONE
                );

                recyclerSubtasks.setVisibility(
                        View.GONE
                );

                tvSubtasksEmpty.setVisibility(
                        View.GONE
                );

                tvSubtasksError.setVisibility(
                        View.VISIBLE
                );

                layoutSubtaskProgress.setVisibility(
                        View.GONE
                );

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Unable to load subtasks.";
                }

                tvSubtasksError.setText(
                        message
                );

                break;
        }
    }

    private void updateSubtasksTitle(
            List<Subtask> subtasks
    ) {

        tvSubtasksTitle.setText(
                "Subtasks"
        );
    }

    private void updateSubtaskProgress(
            List<Subtask> subtasks
    ) {

        if (subtasks == null
                || subtasks.isEmpty()) {

            tvSubtaskProgressCount.setText(
                    "0 of 0 completed"
            );

            tvSubtaskProgressPercent.setText(
                    "0%"
            );

            progressSubtaskCompletion.setProgress(
                    0
            );

            return;
        }

        int total =
                subtasks.size();

        int completed =
                0;

        for (Subtask subtask : subtasks) {

            if (subtask != null
                    && subtask.isCompleted()) {

                completed++;
            }
        }

        int percentage =
                (int) Math.round(
                        (completed * 100.0)
                                / total
                );

        tvSubtaskProgressCount.setText(
                completed
                        + " of "
                        + total
                        + " completed"
        );

        tvSubtaskProgressPercent.setText(
                percentage + "%"
        );

        progressSubtaskCompletion.setProgress(
                percentage
        );
    }

    private void handleSubtaskActionState(
            UiState<Subtask> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                btnAddSubtask.setEnabled(false);

                break;

            case SUCCESS:

                btnAddSubtask.setEnabled(true);

                if (subtaskCompletionUpdateInProgress) {

                    Toast.makeText(
                            this,
                            "Subtask updated.",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    Toast.makeText(
                            this,
                            "Subtask added.",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                subtaskCompletionUpdateInProgress =
                        false;

                break;

            case ERROR:

                btnAddSubtask.setEnabled(true);

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    if (subtaskCompletionUpdateInProgress) {

                        message =
                                "Unable to update subtask.";

                    } else {

                        message =
                                "Unable to add subtask.";
                    }
                }

                subtaskCompletionUpdateInProgress =
                        false;

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                tasksViewModel.loadSubtasks(
                        taskId
                );

                break;
        }
    }

    private void handleDeleteSubtaskActionState(
            UiState<Boolean> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                btnAddSubtask.setEnabled(false);

                break;

            case SUCCESS:

                btnAddSubtask.setEnabled(true);

                Toast.makeText(
                        this,
                        "Subtask deleted.",
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case ERROR:

                btnAddSubtask.setEnabled(true);

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Unable to delete subtask.";
                }

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                tasksViewModel.loadSubtasks(
                        taskId
                );

                break;
        }
    }

    private void updateSubtaskCompleted(
            Subtask subtask,
            boolean completed
    ) {

        if (subtask == null
                || subtask.getId() == null
                || subtask.getId().trim().isEmpty()) {

            return;
        }

        subtaskCompletionUpdateInProgress =
                true;

        tasksViewModel.updateSubtaskCompleted(
                taskId,
                subtask.getId(),
                completed
        );
    }

    private void showDeleteSubtaskConfirmation(
            Subtask subtask
    ) {

        if (subtask == null
                || subtask.getId() == null
                || subtask.getId().trim().isEmpty()) {

            return;
        }

        String title =
                subtask.getTitle();

        String message;

        if (title == null
                || title.trim().isEmpty()) {

            message =
                    "Are you sure you want to delete this subtask?";

        } else {

            message =
                    "Are you sure you want to delete \""
                            + title
                            + "\"?";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "Delete Subtask"
                )
                .setMessage(
                        message
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteSubtask(subtask)
                )
                .show();
    }

    private void deleteSubtask(
            Subtask subtask
    ) {

        if (subtask == null
                || subtask.getId() == null
                || subtask.getId().trim().isEmpty()) {

            return;
        }

        tasksViewModel.deleteSubtask(
                taskId,
                subtask.getId()
        );
    }

    private void showAddSubtaskDialog() {

        subtaskCompletionUpdateInProgress =
                false;

        TextInputLayout inputLayout =
                new TextInputLayout(this);

        inputLayout.setHint(
                "Subtask title"
        );

        inputLayout.setBoxBackgroundMode(
                TextInputLayout.BOX_BACKGROUND_OUTLINE
        );

        TextInputEditText input =
                new TextInputEditText(
                        inputLayout.getContext()
                );

        input.setSingleLine(true);
        input.setMaxLines(1);

        inputLayout.addView(
                input
        );

        int horizontalPadding =
                (int) (
                        24
                                * getResources()
                                .getDisplayMetrics()
                                .density
                );

        int topPadding =
                (int) (
                        8
                                * getResources()
                                .getDisplayMetrics()
                                .density
                );

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                horizontalPadding,
                topPadding,
                horizontalPadding,
                0
        );

        container.addView(
                inputLayout
        );

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle(
                                "Add Subtask"
                        )
                        .setView(
                                container
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Add",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                                    androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(
                                    view -> {

                                        String title =
                                                input.getText() == null
                                                        ? ""
                                                        : input
                                                        .getText()
                                                        .toString()
                                                        .trim();

                                        inputLayout.setError(
                                                null
                                        );

                                        if (title.isEmpty()) {

                                            inputLayout.setError(
                                                    "Subtask title is required."
                                            );

                                            return;
                                        }

                                        if (title.length() > 150) {

                                            inputLayout.setError(
                                                    "Maximum 150 characters."
                                            );

                                            return;
                                        }

                                        subtaskCompletionUpdateInProgress =
                                                false;

                                        tasksViewModel.createSubtask(
                                                taskId,
                                                title
                                        );

                                        dialog.dismiss();
                                    }
                            );
                }
        );

        dialog.show();
    }

    private void openEditTask() {

        Intent intent =
                new Intent(
                        this,
                        AddEditTaskActivity.class
                );

        intent.putExtra(
                AddEditTaskActivity.EXTRA_TASK_ID,
                taskId
        );

        startActivity(intent);
    }

    private void showDeleteConfirmation() {

        if (currentTask == null) {
            return;
        }

        String taskTitle =
                currentTask.getTitle();

        String message;

        if (taskTitle == null
                || taskTitle.trim().isEmpty()) {

            message =
                    "Are you sure you want to delete this task?";

        } else {

            message =
                    "Are you sure you want to delete \""
                            + taskTitle
                            + "\"?";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "Delete Task"
                )
                .setMessage(
                        message
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteTask()
                )
                .show();
    }

    private void deleteTask() {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            return;
        }

        tasksViewModel.deleteTask(
                taskId
        );
    }

    private void startTask() {

        if (currentTask == null) {
            return;
        }

        tasksViewModel.updateTaskStatus(
                taskId,
                TaskStatus.IN_PROGRESS,
                null
        );
    }

    private void completeTask() {

        if (currentTask == null) {
            return;
        }

        tasksViewModel.updateTaskStatus(
                taskId,
                TaskStatus.COMPLETED,
                getCurrentTimestamp()
        );
    }

    private void reopenTask() {

        if (currentTask == null) {
            return;
        }

        tasksViewModel.updateTaskStatus(
                taskId,
                TaskStatus.TO_DO,
                null
        );
    }

    private void setActionButtonsEnabled(
            boolean enabled
    ) {

        btnStartTask.setEnabled(enabled);
        btnCompleteTask.setEnabled(enabled);
        btnReopenTask.setEnabled(enabled);
        btnEditTask.setEnabled(enabled);
        btnDeleteTask.setEnabled(enabled);
        btnAddSubtask.setEnabled(enabled);
    }

    private void updateStatusButtons(
            Task task
    ) {

        btnStartTask.setVisibility(
                View.GONE
        );

        btnCompleteTask.setVisibility(
                View.GONE
        );

        btnReopenTask.setVisibility(
                View.GONE
        );

        if (task.getStatus() == null) {
            return;
        }

        switch (task.getStatus()) {

            case TO_DO:

                btnStartTask.setVisibility(
                        View.VISIBLE
                );

                btnCompleteTask.setVisibility(
                        View.VISIBLE
                );

                break;

            case IN_PROGRESS:

                btnCompleteTask.setVisibility(
                        View.VISIBLE
                );

                break;

            case COMPLETED:

                btnReopenTask.setVisibility(
                        View.VISIBLE
                );

                break;
        }
    }

    private void showLoading() {

        progressTaskDetails.setVisibility(
                View.VISIBLE
        );

        scrollTaskDetails.setVisibility(
                View.GONE
        );

        layoutTaskDetailsError.setVisibility(
                View.GONE
        );
    }

    private void showTask(
            Task task
    ) {

        progressTaskDetails.setVisibility(
                View.GONE
        );

        layoutTaskDetailsError.setVisibility(
                View.GONE
        );

        scrollTaskDetails.setVisibility(
                View.VISIBLE
        );

        tvTaskDetailsTitle.setText(
                task.getTitle()
        );

        tvTaskDetailsImportant.setVisibility(
                task.isImportant()
                        ? View.VISIBLE
                        : View.GONE
        );

        String status =
                getStatusText(
                        task
                );

        String priority =
                getPriorityText(
                        task
                );

        String duration =
                getDurationText(
                        task
                );

        tvTaskDetailsStatus.setText(
                status
        );

        tvTaskDetailsPriority.setText(
                priority
        );

        tvTaskDetailsDeadline.setText(
                getDeadlineText(
                        task
                )
        );

        tvTaskDetailsDuration.setText(
                duration
        );

        tvTaskDetailsReminder.setText(
                getReminderText(
                        task.getReminderType()
                )
        );

        tvTaskDetailsStatusBadge.setText(
                status
        );

        tvTaskDetailsPriorityBadge.setText(
                priority
        );

        // NEW:
        // Apply semantic colors to hero badges.

        updateStatusBadgeAppearance(
                task
        );

        updatePriorityBadgeAppearance(
                task
        );

        Integer estimatedDuration =
                task.getEstimatedDuration();

        if (estimatedDuration == null) {

            layoutTaskHeroDuration.setVisibility(
                    View.GONE
            );

        } else {

            layoutTaskHeroDuration.setVisibility(
                    View.VISIBLE
            );

            tvTaskHeroDuration.setText(
                    duration
            );
        }

        bindDescription(
                task
        );

        updateCategory();

        updateStatusButtons(
                task
        );
    }

    // =========================================================
    // HERO BADGE COLORS
    // =========================================================

    private void updateStatusBadgeAppearance(
            Task task
    ) {

        if (isOverdue(task)) {

            applyBadgeColors(
                    tvTaskDetailsStatusBadge,
                    R.color.sp_home_overdue_bg,
                    R.color.sp_home_overdue
            );

            return;
        }

        if (task.getStatus() == null) {

            applyBadgeColors(
                    tvTaskDetailsStatusBadge,
                    R.color.sp_home_events_bg,
                    R.color.sp_text_secondary
            );

            return;
        }

        switch (task.getStatus()) {

            case TO_DO:

                applyBadgeColors(
                        tvTaskDetailsStatusBadge,
                        R.color.sp_home_events_bg,
                        R.color.sp_home_blue
                );

                break;

            case IN_PROGRESS:

                applyBadgeColors(
                        tvTaskDetailsStatusBadge,
                        R.color.sp_home_deadline_bg,
                        R.color.sp_home_deadline
                );

                break;

            case COMPLETED:

                applyBadgeColors(
                        tvTaskDetailsStatusBadge,
                        R.color.sp_home_tasks_bg,
                        R.color.sp_teal_dark
                );

                break;
        }
    }

    private void updatePriorityBadgeAppearance(
            Task task
    ) {

        if (task.getPriority() == null) {

            applyBadgeColors(
                    tvTaskDetailsPriorityBadge,
                    R.color.sp_home_events_bg,
                    R.color.sp_text_secondary
            );

            return;
        }

        switch (task.getPriority()) {

            case HIGH:

                applyBadgeColors(
                        tvTaskDetailsPriorityBadge,
                        R.color.sp_home_overdue_bg,
                        R.color.sp_home_overdue
                );

                break;

            case MEDIUM:

                applyBadgeColors(
                        tvTaskDetailsPriorityBadge,
                        R.color.sp_home_deadline_bg,
                        R.color.sp_home_deadline
                );

                break;

            case LOW:

                applyBadgeColors(
                        tvTaskDetailsPriorityBadge,
                        R.color.sp_home_events_bg,
                        R.color.sp_home_blue
                );

                break;
        }
    }

    private void applyBadgeColors(
            TextView badge,
            int backgroundColorResource,
            int textColorResource
    ) {

        int backgroundColor =
                ContextCompat.getColor(
                        this,
                        backgroundColorResource
                );

        int textColor =
                ContextCompat.getColor(
                        this,
                        textColorResource
                );

        badge.setBackgroundTintList(
                ColorStateList.valueOf(
                        backgroundColor
                )
        );

        badge.setTextColor(
                textColor
        );
    }

    private void showError(
            String message
    ) {

        progressTaskDetails.setVisibility(
                View.GONE
        );

        scrollTaskDetails.setVisibility(
                View.GONE
        );

        layoutTaskDetailsError.setVisibility(
                View.VISIBLE
        );

        tvTaskDetailsError.setText(
                message != null
                        ? message
                        : "Unable to load task."
        );
    }

    private void updateCategory() {

        if (currentTask == null) {
            return;
        }

        String categoryId =
                currentTask.getCategoryId();

        if (categoryId == null
                || categoryId.trim().isEmpty()) {

            tvTaskDetailsCategory.setText(
                    "No category"
            );

            setTaskCategoryAppearance(
                    null
            );

            return;
        }

        Category category =
                categoryMap.get(
                        categoryId
                );

        if (category == null) {

            tvTaskDetailsCategory.setText(
                    "No category"
            );

            setTaskCategoryAppearance(
                    null
            );

            return;
        }

        tvTaskDetailsCategory.setText(
                category.getName()
        );

        setTaskCategoryAppearance(
                category
        );
    }

    private void setTaskCategoryAppearance(
            Category category
    ) {

        int iconResource =
                R.drawable.ic_task_category_default;

        int categoryColor =
                Color.parseColor(
                        "#46C8BE"
                );

        if (category != null) {

            iconResource =
                    getTaskCategoryIcon(
                            category
                    );

            categoryColor =
                    getTaskCategoryColor(
                            category
                    );
        }

        ivTaskCategoryIcon.setImageResource(
                iconResource
        );

        ivTaskCategoryIcon.setColorFilter(
                categoryColor
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setShape(
                GradientDrawable.RECTANGLE
        );

        background.setCornerRadius(
                dpToPx(16)
        );

        background.setColor(
                createPastelCategoryColor(
                        categoryColor
                )
        );

        layoutTaskCategoryIcon.setBackground(
                background
        );
    }

    private int getTaskCategoryIcon(
            Category category
    ) {

        if (category == null) {
            return R.drawable.ic_task_category_default;
        }

        String icon =
                category.getIcon();

        if (icon != null
                && !icon.trim().isEmpty()) {

            switch (
                    icon.trim()
                            .toLowerCase(
                                    Locale.US
                            )
            ) {

                case "work":
                    return R.drawable.ic_task_category_work;

                case "personal":
                    return R.drawable.ic_task_category_personal;

                case "health":
                    return R.drawable.ic_task_category_health;

                case "study":
                    return R.drawable.ic_task_category_study;

                case "shopping":
                    return R.drawable.ic_task_category_shopping;
            }
        }

        String name =
                category.getName();

        if (name == null) {
            return R.drawable.ic_task_category_default;
        }

        name =
                name.trim()
                        .toLowerCase(
                                Locale.US
                        );

        if (name.contains("work")
                || name.contains("job")
                || name.contains("business")) {

            return R.drawable.ic_task_category_work;
        }

        if (name.contains("personal")
                || name.contains("home")) {

            return R.drawable.ic_task_category_personal;
        }

        if (name.contains("health")
                || name.contains("fitness")
                || name.contains("gym")
                || name.contains("sport")) {

            return R.drawable.ic_task_category_health;
        }

        if (name.contains("study")
                || name.contains("university")
                || name.contains("school")
                || name.contains("college")) {

            return R.drawable.ic_task_category_study;
        }

        if (name.contains("shopping")
                || name.contains("shop")
                || name.contains("groceries")) {

            return R.drawable.ic_task_category_shopping;
        }

        return R.drawable.ic_task_category_default;
    }

    private int getTaskCategoryColor(
            Category category
    ) {

        if (category == null
                || category.getColor() == null
                || category.getColor()
                .trim()
                .isEmpty()) {

            return Color.parseColor(
                    "#46C8BE"
            );
        }

        try {

            String color =
                    category.getColor()
                            .trim();

            if (!color.startsWith("#")) {
                color = "#" + color;
            }

            return Color.parseColor(
                    color
            );

        } catch (IllegalArgumentException exception) {

            return Color.parseColor(
                    "#46C8BE"
            );
        }
    }

    private int createPastelCategoryColor(
            int color
    ) {

        int red =
                Color.red(color);

        int green =
                Color.green(color);

        int blue =
                Color.blue(color);

        red =
                (int) (
                        red * 0.18f
                                + 255 * 0.82f
                );

        green =
                (int) (
                        green * 0.18f
                                + 255 * 0.82f
                );

        blue =
                (int) (
                        blue * 0.18f
                                + 255 * 0.82f
                );

        return Color.rgb(
                red,
                green,
                blue
        );
    }

    private float dpToPx(
            float dp
    ) {

        return dp
                * getResources()
                .getDisplayMetrics()
                .density;
    }

    private void bindDescription(
            Task task
    ) {

        String description =
                task.getDescription();

        if (description == null
                || description.trim().isEmpty()) {

            cardTaskDescription.setVisibility(
                    View.GONE
            );

            return;
        }

        cardTaskDescription.setVisibility(
                View.VISIBLE
        );

        tvTaskDetailsDescription.setText(
                description
        );
    }

    private String getStatusText(
            Task task
    ) {

        if (isOverdue(task)) {
            return "Overdue";
        }

        if (task.getStatus() == null) {
            return "Unknown";
        }

        switch (task.getStatus()) {

            case TO_DO:
                return "To Do";

            case IN_PROGRESS:
                return "In Progress";

            case COMPLETED:
                return "Completed";

            default:
                return "Unknown";
        }
    }

    private String getPriorityText(
            Task task
    ) {

        if (task.getPriority() == null) {
            return "Not set";
        }

        switch (task.getPriority()) {

            case LOW:
                return "Low";

            case MEDIUM:
                return "Medium";

            case HIGH:
                return "High";

            default:
                return "Not set";
        }
    }

    private String getDeadlineText(
            Task task
    ) {

        String deadline =
                task.getDeadline();

        if (deadline == null
                || deadline.trim().isEmpty()) {

            return "No deadline";
        }

        Date date =
                parseSupabaseDate(
                        deadline
                );

        if (date == null) {
            return deadline;
        }

        SimpleDateFormat output =
                new SimpleDateFormat(
                        "dd MMM yyyy, HH:mm",
                        Locale.ENGLISH
                );

        return output.format(
                date
        );
    }

    private String getDurationText(
            Task task
    ) {

        Integer minutes =
                task.getEstimatedDuration();

        if (minutes == null) {
            return "Not set";
        }

        if (minutes < 60) {
            return minutes + " min";
        }

        int hours =
                minutes / 60;

        int remainingMinutes =
                minutes % 60;

        if (remainingMinutes == 0) {
            return hours + " h";
        }

        return hours
                + " h "
                + remainingMinutes
                + " min";
    }

    private String getReminderText(
            ReminderType reminderType
    ) {

        if (reminderType == null) {
            return "No reminder";
        }

        switch (reminderType) {

            case TEN_MINUTES:
                return "10 minutes before";

            case THIRTY_MINUTES:
                return "30 minutes before";

            case ONE_HOUR:
                return "1 hour before";

            case ONE_DAY:
                return "1 day before";

            case NONE:
            default:
                return "No reminder";
        }
    }

    private String getCurrentTimestamp() {

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        Locale.US
                );

        formatter.setTimeZone(
                TimeZone.getTimeZone(
                        "UTC"
                )
        );

        return formatter.format(
                new Date()
        );
    }

    private boolean isOverdue(
            Task task
    ) {

        if (task == null) {
            return false;
        }

        if (task.getStatus()
                == TaskStatus.COMPLETED) {

            return false;
        }

        String deadline =
                task.getDeadline();

        if (deadline == null
                || deadline.trim().isEmpty()) {

            return false;
        }

        Date deadlineDate =
                parseSupabaseDate(
                        deadline
                );

        if (deadlineDate == null) {
            return false;
        }

        return deadlineDate.before(
                new Date()
        );
    }

    private Date parseSupabaseDate(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"
        };

        for (String format : formats) {

            try {

                SimpleDateFormat parser =
                        new SimpleDateFormat(
                                format,
                                Locale.US
                        );

                return parser.parse(
                        value
                );

            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}