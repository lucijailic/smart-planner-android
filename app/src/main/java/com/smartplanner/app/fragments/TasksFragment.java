package com.smartplanner.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.task.AddEditTaskActivity;
import com.smartplanner.app.activities.task.TaskDetailsActivity;
import com.smartplanner.app.adapters.TaskAdapter;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.viewmodels.CategoriesViewModel;
import com.smartplanner.app.viewmodels.TasksViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private enum QuickFilter {
        ALL,
        TO_DO,
        IN_PROGRESS,
        COMPLETED,
        OVERDUE
    }

    private enum AdvancedStatusFilter {
        ANY,
        TO_DO,
        IN_PROGRESS,
        COMPLETED
    }

    private enum AdvancedPriorityFilter {
        ANY,
        LOW,
        MEDIUM,
        HIGH
    }

    private enum AdvancedImportantFilter {
        ANY,
        IMPORTANT,
        NOT_IMPORTANT
    }

    private enum AdvancedDateFilter {
        ANY,
        TODAY,
        TOMORROW,
        THIS_WEEK,
        NO_DEADLINE
    }

    private enum SortOption {
        NONE,
        NEAREST_DEADLINE,
        FARTHEST_DEADLINE,
        HIGHEST_PRIORITY,
        NEWEST,
        OLDEST,
        TITLE_A_Z
    }

    private TasksViewModel viewModel;
    private CategoriesViewModel categoriesViewModel;

    private TaskAdapter taskAdapter;

    private RecyclerView rvTasks;
    private ProgressBar progressTasks;

    private LinearLayout layoutEmptyTasks;
    private LinearLayout layoutErrorTasks;

    private TextView tvTasksError;

    private MaterialButton btnRetryTasks;
    private MaterialButton btnAdvancedFilters;
    private MaterialButton btnSortTasks;

    private FloatingActionButton fabAddTask;

    private EditText etTaskSearch;

    private TextView chipAll;
    private TextView chipToDo;
    private TextView chipInProgress;
    private TextView chipCompleted;
    private TextView chipOverdue;

    private final List<Task> allTasks =
            new ArrayList<>();

    private final List<Category> allCategories =
            new ArrayList<>();

    private QuickFilter selectedQuickFilter =
            QuickFilter.ALL;

    private AdvancedStatusFilter selectedAdvancedStatus =
            AdvancedStatusFilter.ANY;

    private AdvancedPriorityFilter selectedAdvancedPriority =
            AdvancedPriorityFilter.ANY;

    private AdvancedImportantFilter selectedAdvancedImportant =
            AdvancedImportantFilter.ANY;

    private AdvancedDateFilter selectedAdvancedDate =
            AdvancedDateFilter.ANY;

    private SortOption selectedSortOption =
            SortOption.NONE;

    private String selectedCategoryId =
            null;

    public TasksFragment() {
        super(R.layout.fragment_tasks);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        initViews(view);
        setupRecyclerView();
        setupViewModels();
        setupListeners();

        observeTasks();
        observeCategories();
        observeImportantAction();

        updateQuickFilterSelection();
        updateAdvancedFilterButton();
        updateSortButton();

        categoriesViewModel.loadCategories();
    }

    private void initViews(
            View view
    ) {

        rvTasks =
                view.findViewById(
                        R.id.rvTasks
                );

        progressTasks =
                view.findViewById(
                        R.id.progressTasks
                );

        layoutEmptyTasks =
                view.findViewById(
                        R.id.layoutEmptyTasks
                );

        layoutErrorTasks =
                view.findViewById(
                        R.id.layoutErrorTasks
                );

        tvTasksError =
                view.findViewById(
                        R.id.tvTasksError
                );

        btnRetryTasks =
                view.findViewById(
                        R.id.btnRetryTasks
                );

        btnAdvancedFilters =
                view.findViewById(
                        R.id.btnAdvancedFilters
                );

        btnSortTasks =
                view.findViewById(
                        R.id.btnSortTasks
                );

        fabAddTask =
                view.findViewById(
                        R.id.fabAddTask
                );

        etTaskSearch =
                view.findViewById(
                        R.id.etTaskSearch
                );

        chipAll =
                view.findViewById(
                        R.id.chipAll
                );

        chipToDo =
                view.findViewById(
                        R.id.chipToDo
                );

        chipInProgress =
                view.findViewById(
                        R.id.chipInProgress
                );

        chipCompleted =
                view.findViewById(
                        R.id.chipCompleted
                );

        chipOverdue =
                view.findViewById(
                        R.id.chipOverdue
                );
    }

    private void setupRecyclerView() {

        taskAdapter =
                new TaskAdapter();

        rvTasks.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        rvTasks.setAdapter(
                taskAdapter
        );
    }

    private void setupViewModels() {

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                TasksViewModel.class
                        );

        categoriesViewModel =
                new ViewModelProvider(this)
                        .get(
                                CategoriesViewModel.class
                        );
    }

    private void setupListeners() {

        btnRetryTasks.setOnClickListener(
                v -> {

                    viewModel.loadTasks();
                    categoriesViewModel.loadCategories();
                }
        );

        fabAddTask.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    AddEditTaskActivity.class
                            );

                    startActivity(intent);
                }
        );

        btnAdvancedFilters.setOnClickListener(
                view -> showAdvancedFiltersDialog()
        );

        btnSortTasks.setOnClickListener(
                view -> showSortDialog()
        );

        taskAdapter.setOnImportantClickListener(
                (task, newImportantState) -> {

                    if (task == null
                            || task.getId() == null
                            || task.getId().trim().isEmpty()) {

                        Toast.makeText(
                                requireContext(),
                                "Unable to update task.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    viewModel.updateImportant(
                            task.getId(),
                            newImportantState
                    );
                }
        );

        taskAdapter.setOnTaskClickListener(
                task -> {

                    if (task == null
                            || task.getId() == null
                            || task.getId().trim().isEmpty()) {

                        return;
                    }

                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    TaskDetailsActivity.class
                            );

                    intent.putExtra(
                            TaskDetailsActivity.EXTRA_TASK_ID,
                            task.getId()
                    );

                    startActivity(intent);
                }
        );

        etTaskSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        applyFiltersAndSorting();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        chipAll.setOnClickListener(
                view -> selectQuickFilter(
                        QuickFilter.ALL
                )
        );

        chipToDo.setOnClickListener(
                view -> selectQuickFilter(
                        QuickFilter.TO_DO
                )
        );

        chipInProgress.setOnClickListener(
                view -> selectQuickFilter(
                        QuickFilter.IN_PROGRESS
                )
        );

        chipCompleted.setOnClickListener(
                view -> selectQuickFilter(
                        QuickFilter.COMPLETED
                )
        );

        chipOverdue.setOnClickListener(
                view -> selectQuickFilter(
                        QuickFilter.OVERDUE
                )
        );
    }

    private void observeTasks() {

        viewModel
                .getTasksState()
                .observe(
                        getViewLifecycleOwner(),
                        this::handleTasksState
                );
    }

    private void observeCategories() {

        categoriesViewModel
                .getCategoriesState()
                .observe(
                        getViewLifecycleOwner(),
                        this::handleCategoriesState
                );
    }

    private void observeImportantAction() {

        viewModel
                .getImportantActionState()
                .observe(
                        getViewLifecycleOwner(),
                        this::handleImportantActionState
                );
    }

    private void handleCategoriesState(
            UiState<List<Category>> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                break;

            case SUCCESS:

                allCategories.clear();

                if (state.getData() != null) {

                    allCategories.addAll(
                            state.getData()
                    );
                }

                taskAdapter.setCategories(
                        state.getData()
                );

                break;

            case ERROR:
                break;
        }
    }

    private void handleImportantActionState(
            UiState<Task> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                break;

            case SUCCESS:

                viewModel.loadTasks();

                break;

            case ERROR:

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Unable to update important status.";
                }

                Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void handleTasksState(
            UiState<List<Task>> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                showLoading();

                break;

            case SUCCESS:

                allTasks.clear();

                if (state.getData() != null) {

                    allTasks.addAll(
                            state.getData()
                    );
                }

                applyFiltersAndSorting();

                break;

            case ERROR:

                showError(
                        state.getMessage()
                );

                break;
        }
    }

    private void selectQuickFilter(
            QuickFilter filter
    ) {

        selectedQuickFilter =
                filter;

        updateQuickFilterSelection();

        applyFiltersAndSorting();
    }

    private void showSortDialog() {

        String[] sortOptions = {
                "Default",
                "Nearest deadline",
                "Farthest deadline",
                "Highest priority",
                "Newest",
                "Oldest",
                "A–Z"
        };

        int checkedItem =
                getSortOptionPosition();

        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle(
                        "Sort tasks"
                )
                .setSingleChoiceItems(
                        sortOptions,
                        checkedItem,
                        (dialog, which) -> {

                            switch (which) {

                                case 1:

                                    selectedSortOption =
                                            SortOption.NEAREST_DEADLINE;

                                    break;

                                case 2:

                                    selectedSortOption =
                                            SortOption.FARTHEST_DEADLINE;

                                    break;

                                case 3:

                                    selectedSortOption =
                                            SortOption.HIGHEST_PRIORITY;

                                    break;

                                case 4:

                                    selectedSortOption =
                                            SortOption.NEWEST;

                                    break;

                                case 5:

                                    selectedSortOption =
                                            SortOption.OLDEST;

                                    break;

                                case 6:

                                    selectedSortOption =
                                            SortOption.TITLE_A_Z;

                                    break;

                                case 0:
                                default:

                                    selectedSortOption =
                                            SortOption.NONE;

                                    break;
                            }

                            updateSortButton();
                            applyFiltersAndSorting();

                            dialog.dismiss();
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private int getSortOptionPosition() {

        switch (selectedSortOption) {

            case NEAREST_DEADLINE:
                return 1;

            case FARTHEST_DEADLINE:
                return 2;

            case HIGHEST_PRIORITY:
                return 3;

            case NEWEST:
                return 4;

            case OLDEST:
                return 5;

            case TITLE_A_Z:
                return 6;

            case NONE:
            default:
                return 0;
        }
    }

    private void updateSortButton() {

        switch (selectedSortOption) {

            case NEAREST_DEADLINE:

                btnSortTasks.setText(
                        "Nearest"
                );

                break;

            case FARTHEST_DEADLINE:

                btnSortTasks.setText(
                        "Farthest"
                );

                break;

            case HIGHEST_PRIORITY:

                btnSortTasks.setText(
                        "Priority"
                );

                break;

            case NEWEST:

                btnSortTasks.setText(
                        "Newest"
                );

                break;

            case OLDEST:

                btnSortTasks.setText(
                        "Oldest"
                );

                break;

            case TITLE_A_Z:

                btnSortTasks.setText(
                        "A–Z"
                );

                break;

            case NONE:
            default:

                btnSortTasks.setText(
                        "Sort"
                );

                break;
        }
    }

    private void showAdvancedFiltersDialog() {

        View dialogView =
                LayoutInflater
                        .from(requireContext())
                        .inflate(
                                R.layout.dialog_task_filters,
                                null
                        );

        MaterialAutoCompleteTextView dropdownStatus =
                dialogView.findViewById(
                        R.id.dropdownFilterStatus
                );

        MaterialAutoCompleteTextView dropdownCategory =
                dialogView.findViewById(
                        R.id.dropdownFilterCategory
                );

        MaterialAutoCompleteTextView dropdownPriority =
                dialogView.findViewById(
                        R.id.dropdownFilterPriority
                );

        MaterialAutoCompleteTextView dropdownImportant =
                dialogView.findViewById(
                        R.id.dropdownFilterImportant
                );

        MaterialAutoCompleteTextView dropdownDate =
                dialogView.findViewById(
                        R.id.dropdownFilterDate
                );

        String[] statusOptions = {
                "Any status",
                "To Do",
                "In Progress",
                "Completed"
        };

        String[] priorityOptions = {
                "Any priority",
                "Low",
                "Medium",
                "High"
        };

        String[] importantOptions = {
                "Any",
                "Important only",
                "Not important"
        };

        String[] dateOptions = {
                "Any date",
                "Today",
                "Tomorrow",
                "This week",
                "No deadline"
        };

        List<String> categoryOptions =
                new ArrayList<>();

        categoryOptions.add(
                "Any category"
        );

        for (Category category : allCategories) {

            if (category != null
                    && category.getName() != null) {

                categoryOptions.add(
                        category.getName()
                );
            }
        }

        dropdownStatus.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        statusOptions
                )
        );

        dropdownCategory.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        categoryOptions
                )
        );

        dropdownPriority.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        priorityOptions
                )
        );

        dropdownImportant.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        importantOptions
                )
        );

        dropdownDate.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        dateOptions
                )
        );

        dropdownStatus.setText(
                getAdvancedStatusText(),
                false
        );

        dropdownCategory.setText(
                getSelectedCategoryText(),
                false
        );

        dropdownPriority.setText(
                getAdvancedPriorityText(),
                false
        );

        dropdownImportant.setText(
                getAdvancedImportantText(),
                false
        );

        dropdownDate.setText(
                getAdvancedDateText(),
                false
        );

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(
                        requireContext()
                )
                        .setTitle(
                                "Filters"
                        )
                        .setView(
                                dialogView
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setNeutralButton(
                                "Clear",
                                null
                        )
                        .setPositiveButton(
                                "Apply",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                                    androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL
                            )
                            .setOnClickListener(
                                    view -> {

                                        clearAdvancedFilters();

                                        dialog.dismiss();
                                    }
                            );

                    dialog.getButton(
                                    androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(
                                    view -> {

                                        applyAdvancedFilterSelections(
                                                dropdownStatus.getText().toString(),
                                                dropdownCategory.getText().toString(),
                                                dropdownPriority.getText().toString(),
                                                dropdownImportant.getText().toString(),
                                                dropdownDate.getText().toString()
                                        );

                                        dialog.dismiss();
                                    }
                            );
                }
        );

        dialog.show();
    }

    private void applyAdvancedFilterSelections(
            String status,
            String category,
            String priority,
            String important,
            String date
    ) {

        switch (status) {

            case "To Do":

                selectedAdvancedStatus =
                        AdvancedStatusFilter.TO_DO;

                break;

            case "In Progress":

                selectedAdvancedStatus =
                        AdvancedStatusFilter.IN_PROGRESS;

                break;

            case "Completed":

                selectedAdvancedStatus =
                        AdvancedStatusFilter.COMPLETED;

                break;

            default:

                selectedAdvancedStatus =
                        AdvancedStatusFilter.ANY;

                break;
        }

        selectedCategoryId =
                findCategoryIdByName(
                        category
                );

        switch (priority) {

            case "Low":

                selectedAdvancedPriority =
                        AdvancedPriorityFilter.LOW;

                break;

            case "Medium":

                selectedAdvancedPriority =
                        AdvancedPriorityFilter.MEDIUM;

                break;

            case "High":

                selectedAdvancedPriority =
                        AdvancedPriorityFilter.HIGH;

                break;

            default:

                selectedAdvancedPriority =
                        AdvancedPriorityFilter.ANY;

                break;
        }

        switch (important) {

            case "Important only":

                selectedAdvancedImportant =
                        AdvancedImportantFilter.IMPORTANT;

                break;

            case "Not important":

                selectedAdvancedImportant =
                        AdvancedImportantFilter.NOT_IMPORTANT;

                break;

            default:

                selectedAdvancedImportant =
                        AdvancedImportantFilter.ANY;

                break;
        }

        switch (date) {

            case "Today":

                selectedAdvancedDate =
                        AdvancedDateFilter.TODAY;

                break;

            case "Tomorrow":

                selectedAdvancedDate =
                        AdvancedDateFilter.TOMORROW;

                break;

            case "This week":

                selectedAdvancedDate =
                        AdvancedDateFilter.THIS_WEEK;

                break;

            case "No deadline":

                selectedAdvancedDate =
                        AdvancedDateFilter.NO_DEADLINE;

                break;

            default:

                selectedAdvancedDate =
                        AdvancedDateFilter.ANY;

                break;
        }

        updateAdvancedFilterButton();

        applyFiltersAndSorting();
    }

    private void clearAdvancedFilters() {

        selectedAdvancedStatus =
                AdvancedStatusFilter.ANY;

        selectedCategoryId =
                null;

        selectedAdvancedPriority =
                AdvancedPriorityFilter.ANY;

        selectedAdvancedImportant =
                AdvancedImportantFilter.ANY;

        selectedAdvancedDate =
                AdvancedDateFilter.ANY;

        updateAdvancedFilterButton();

        applyFiltersAndSorting();
    }

    private void applyFiltersAndSorting() {

        if (taskAdapter == null) {
            return;
        }

        String query =
                "";

        if (etTaskSearch != null
                && etTaskSearch.getText() != null) {

            query =
                    etTaskSearch
                            .getText()
                            .toString()
                            .trim()
                            .toLowerCase(
                                    Locale.getDefault()
                            );
        }

        List<Task> filteredTasks =
                new ArrayList<>();

        for (Task task : allTasks) {

            if (task == null) {
                continue;
            }

            if (!matchesSearch(
                    task,
                    query
            )) {
                continue;
            }

            if (!matchesQuickFilter(
                    task
            )) {
                continue;
            }

            if (!matchesAdvancedStatus(
                    task
            )) {
                continue;
            }

            if (!matchesCategory(
                    task
            )) {
                continue;
            }

            if (!matchesPriority(
                    task
            )) {
                continue;
            }

            if (!matchesImportant(
                    task
            )) {
                continue;
            }

            if (!matchesDateFilter(
                    task
            )) {
                continue;
            }

            filteredTasks.add(
                    task
            );
        }

        sortTasks(
                filteredTasks
        );

        if (filteredTasks.isEmpty()) {

            showFilteredEmpty();

        } else {

            showTasks(
                    filteredTasks
            );
        }
    }

    private void sortTasks(
            List<Task> tasks
    ) {

        if (tasks == null
                || tasks.size() < 2) {

            return;
        }

        switch (selectedSortOption) {

            case NEAREST_DEADLINE:

                Collections.sort(
                        tasks,
                        (first, second) ->
                                compareDeadline(
                                        first,
                                        second,
                                        true
                                )
                );

                break;

            case FARTHEST_DEADLINE:

                Collections.sort(
                        tasks,
                        (first, second) ->
                                compareDeadline(
                                        first,
                                        second,
                                        false
                                )
                );

                break;

            case HIGHEST_PRIORITY:

                Collections.sort(
                        tasks,
                        (first, second) ->
                                Integer.compare(
                                        getPriorityValue(
                                                second
                                        ),
                                        getPriorityValue(
                                                first
                                        )
                                )
                );

                break;

            case NEWEST:

                Collections.sort(
                        tasks,
                        (first, second) ->
                                compareCreatedAt(
                                        first,
                                        second,
                                        false
                                )
                );

                break;

            case OLDEST:

                Collections.sort(
                        tasks,
                        (first, second) ->
                                compareCreatedAt(
                                        first,
                                        second,
                                        true
                                )
                );

                break;

            case TITLE_A_Z:

                Collections.sort(
                        tasks,
                        (first, second) -> {

                            String firstTitle =
                                    first.getTitle() == null
                                            ? ""
                                            : first.getTitle();

                            String secondTitle =
                                    second.getTitle() == null
                                            ? ""
                                            : second.getTitle();

                            return firstTitle.compareToIgnoreCase(
                                    secondTitle
                            );
                        }
                );

                break;

            case NONE:
            default:
                break;
        }
    }

    private int compareDeadline(
            Task first,
            Task second,
            boolean nearestFirst
    ) {

        Date firstDate =
                getTaskDeadlineDate(
                        first
                );

        Date secondDate =
                getTaskDeadlineDate(
                        second
                );

        if (firstDate == null
                && secondDate == null) {

            return 0;
        }

        /*
         * Tasks without a deadline always remain
         * after tasks that have a deadline.
         */
        if (firstDate == null) {
            return 1;
        }

        if (secondDate == null) {
            return -1;
        }

        int result =
                firstDate.compareTo(
                        secondDate
                );

        return nearestFirst
                ? result
                : -result;
    }

    private Date getTaskDeadlineDate(
            Task task
    ) {

        if (task == null
                || task.getDeadline() == null
                || task.getDeadline().trim().isEmpty()) {

            return null;
        }

        return parseSupabaseDate(
                task.getDeadline()
        );
    }

    private int getPriorityValue(
            Task task
    ) {

        if (task == null
                || task.getPriority() == null) {

            return 0;
        }

        switch (task.getPriority()) {

            case HIGH:
                return 3;

            case MEDIUM:
                return 2;

            case LOW:
                return 1;

            default:
                return 0;
        }
    }

    private int compareCreatedAt(
            Task first,
            Task second,
            boolean oldestFirst
    ) {

        Date firstDate =
                getTaskCreatedAtDate(
                        first
                );

        Date secondDate =
                getTaskCreatedAtDate(
                        second
                );

        if (firstDate == null
                && secondDate == null) {

            return 0;
        }

        if (firstDate == null) {
            return 1;
        }

        if (secondDate == null) {
            return -1;
        }

        int result =
                firstDate.compareTo(
                        secondDate
                );

        return oldestFirst
                ? result
                : -result;
    }

    private Date getTaskCreatedAtDate(
            Task task
    ) {

        if (task == null
                || task.getCreatedAt() == null
                || task.getCreatedAt().trim().isEmpty()) {

            return null;
        }

        return parseSupabaseDate(
                task.getCreatedAt()
        );
    }

    private boolean matchesSearch(
            Task task,
            String query
    ) {

        if (query == null
                || query.isEmpty()) {

            return true;
        }

        String title =
                task.getTitle();

        if (title == null) {
            return false;
        }

        return title
                .toLowerCase(
                        Locale.getDefault()
                )
                .contains(
                        query
                );
    }

    private boolean matchesQuickFilter(
            Task task
    ) {

        switch (selectedQuickFilter) {

            case TO_DO:

                return task.getStatus()
                        == TaskStatus.TO_DO
                        && !isOverdue(task);

            case IN_PROGRESS:

                return task.getStatus()
                        == TaskStatus.IN_PROGRESS
                        && !isOverdue(task);

            case COMPLETED:

                return task.getStatus()
                        == TaskStatus.COMPLETED;

            case OVERDUE:

                return isOverdue(
                        task
                );

            case ALL:
            default:

                return true;
        }
    }

    private boolean matchesAdvancedStatus(
            Task task
    ) {

        switch (selectedAdvancedStatus) {

            case TO_DO:

                return task.getStatus()
                        == TaskStatus.TO_DO;

            case IN_PROGRESS:

                return task.getStatus()
                        == TaskStatus.IN_PROGRESS;

            case COMPLETED:

                return task.getStatus()
                        == TaskStatus.COMPLETED;

            case ANY:
            default:

                return true;
        }
    }

    private boolean matchesCategory(
            Task task
    ) {

        if (selectedCategoryId == null) {
            return true;
        }

        return selectedCategoryId.equals(
                task.getCategoryId()
        );
    }

    private boolean matchesPriority(
            Task task
    ) {

        switch (selectedAdvancedPriority) {

            case LOW:

                return task.getPriority()
                        == TaskPriority.LOW;

            case MEDIUM:

                return task.getPriority()
                        == TaskPriority.MEDIUM;

            case HIGH:

                return task.getPriority()
                        == TaskPriority.HIGH;

            case ANY:
            default:

                return true;
        }
    }

    private boolean matchesImportant(
            Task task
    ) {

        switch (selectedAdvancedImportant) {

            case IMPORTANT:
                return task.isImportant();

            case NOT_IMPORTANT:
                return !task.isImportant();

            case ANY:
            default:
                return true;
        }
    }

    private boolean matchesDateFilter(
            Task task
    ) {

        String deadline =
                task.getDeadline();

        if (selectedAdvancedDate
                == AdvancedDateFilter.ANY) {

            return true;
        }

        if (selectedAdvancedDate
                == AdvancedDateFilter.NO_DEADLINE) {

            return deadline == null
                    || deadline.trim().isEmpty();
        }

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

        Calendar deadlineCalendar =
                Calendar.getInstance();

        deadlineCalendar.setTime(
                deadlineDate
        );

        Calendar today =
                Calendar.getInstance();

        switch (selectedAdvancedDate) {

            case TODAY:

                return isSameDay(
                        deadlineCalendar,
                        today
                );

            case TOMORROW:

                Calendar tomorrow =
                        Calendar.getInstance();

                tomorrow.add(
                        Calendar.DAY_OF_YEAR,
                        1
                );

                return isSameDay(
                        deadlineCalendar,
                        tomorrow
                );

            case THIS_WEEK:

                return isInCurrentWeek(
                        deadlineCalendar
                );

            case ANY:
            case NO_DEADLINE:
            default:

                return true;
        }
    }

    private boolean isSameDay(
            Calendar first,
            Calendar second
    ) {

        return first.get(
                Calendar.YEAR
        ) == second.get(
                Calendar.YEAR
        )
                && first.get(
                Calendar.DAY_OF_YEAR
        ) == second.get(
                Calendar.DAY_OF_YEAR
        );
    }

    private boolean isInCurrentWeek(
            Calendar target
    ) {

        Calendar start =
                Calendar.getInstance();

        start.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        start.set(
                Calendar.MINUTE,
                0
        );

        start.set(
                Calendar.SECOND,
                0
        );

        start.set(
                Calendar.MILLISECOND,
                0
        );

        int firstDay =
                start.getFirstDayOfWeek();

        while (start.get(
                Calendar.DAY_OF_WEEK
        ) != firstDay) {

            start.add(
                    Calendar.DAY_OF_MONTH,
                    -1
            );
        }

        Calendar end =
                (Calendar) start.clone();

        end.add(
                Calendar.DAY_OF_MONTH,
                7
        );

        return !target.before(
                start
        )
                && target.before(
                end
        );
    }

    private String findCategoryIdByName(
            String categoryName
    ) {

        if (categoryName == null
                || categoryName.trim().isEmpty()
                || categoryName.equals(
                "Any category"
        )) {

            return null;
        }

        for (Category category : allCategories) {

            if (category != null
                    && category.getName() != null
                    && category.getName().equals(
                    categoryName
            )) {

                return category.getId();
            }
        }

        return null;
    }

    private String getSelectedCategoryText() {

        if (selectedCategoryId == null) {
            return "Any category";
        }

        for (Category category : allCategories) {

            if (category != null
                    && selectedCategoryId.equals(
                    category.getId()
            )) {

                return category.getName();
            }
        }

        return "Any category";
    }

    private String getAdvancedStatusText() {

        switch (selectedAdvancedStatus) {

            case TO_DO:
                return "To Do";

            case IN_PROGRESS:
                return "In Progress";

            case COMPLETED:
                return "Completed";

            case ANY:
            default:
                return "Any status";
        }
    }

    private String getAdvancedPriorityText() {

        switch (selectedAdvancedPriority) {

            case LOW:
                return "Low";

            case MEDIUM:
                return "Medium";

            case HIGH:
                return "High";

            case ANY:
            default:
                return "Any priority";
        }
    }

    private String getAdvancedImportantText() {

        switch (selectedAdvancedImportant) {

            case IMPORTANT:
                return "Important only";

            case NOT_IMPORTANT:
                return "Not important";

            case ANY:
            default:
                return "Any";
        }
    }

    private String getAdvancedDateText() {

        switch (selectedAdvancedDate) {

            case TODAY:
                return "Today";

            case TOMORROW:
                return "Tomorrow";

            case THIS_WEEK:
                return "This week";

            case NO_DEADLINE:
                return "No deadline";

            case ANY:
            default:
                return "Any date";
        }
    }

    private void updateAdvancedFilterButton() {

        int activeFilters =
                0;

        if (selectedAdvancedStatus
                != AdvancedStatusFilter.ANY) {

            activeFilters++;
        }

        if (selectedCategoryId != null) {
            activeFilters++;
        }

        if (selectedAdvancedPriority
                != AdvancedPriorityFilter.ANY) {

            activeFilters++;
        }

        if (selectedAdvancedImportant
                != AdvancedImportantFilter.ANY) {

            activeFilters++;
        }

        if (selectedAdvancedDate
                != AdvancedDateFilter.ANY) {

            activeFilters++;
        }

        if (activeFilters == 0) {

            btnAdvancedFilters.setText(
                    "Filters"
            );

        } else {

            btnAdvancedFilters.setText(
                    "Filters (" + activeFilters + ")"
            );
        }
    }

    private void updateQuickFilterSelection() {

        setChipSelected(
                chipAll,
                selectedQuickFilter
                        == QuickFilter.ALL
        );

        setChipSelected(
                chipToDo,
                selectedQuickFilter
                        == QuickFilter.TO_DO
        );

        setChipSelected(
                chipInProgress,
                selectedQuickFilter
                        == QuickFilter.IN_PROGRESS
        );

        setChipSelected(
                chipCompleted,
                selectedQuickFilter
                        == QuickFilter.COMPLETED
        );

        setChipSelected(
                chipOverdue,
                selectedQuickFilter
                        == QuickFilter.OVERDUE
        );
    }

    private void setChipSelected(
            TextView chip,
            boolean selected
    ) {

        if (chip == null) {
            return;
        }

        if (selected) {

            chip.setBackgroundResource(
                    R.drawable.bg_task_filter_chip_selected
            );

            chip.setTextColor(
                    requireContext()
                            .getColor(
                                    R.color.white
                            )
            );

            chip.setTypeface(
                    chip.getTypeface(),
                    android.graphics.Typeface.BOLD
            );

        } else {

            chip.setBackgroundResource(
                    R.drawable.bg_task_filter_chip
            );

            chip.setTextColor(
                    requireContext()
                            .getColor(
                                    R.color.sp_text_secondary
                            )
            );

            chip.setTypeface(
                    chip.getTypeface(),
                    android.graphics.Typeface.NORMAL
            );
        }
    }

    private void showLoading() {

        progressTasks.setVisibility(
                View.VISIBLE
        );

        rvTasks.setVisibility(
                View.GONE
        );

        layoutEmptyTasks.setVisibility(
                View.GONE
        );

        layoutErrorTasks.setVisibility(
                View.GONE
        );
    }

    private void showTasks(
            List<Task> tasks
    ) {

        progressTasks.setVisibility(
                View.GONE
        );

        layoutEmptyTasks.setVisibility(
                View.GONE
        );

        layoutErrorTasks.setVisibility(
                View.GONE
        );

        rvTasks.setVisibility(
                View.VISIBLE
        );

        taskAdapter.setTasks(
                tasks
        );
    }

    private void showFilteredEmpty() {

        progressTasks.setVisibility(
                View.GONE
        );

        rvTasks.setVisibility(
                View.GONE
        );

        layoutErrorTasks.setVisibility(
                View.GONE
        );

        layoutEmptyTasks.setVisibility(
                View.VISIBLE
        );

        taskAdapter.setTasks(
                new ArrayList<>()
        );
    }

    private void showError(
            String message
    ) {

        progressTasks.setVisibility(
                View.GONE
        );

        rvTasks.setVisibility(
                View.GONE
        );

        layoutEmptyTasks.setVisibility(
                View.GONE
        );

        layoutErrorTasks.setVisibility(
                View.VISIBLE
        );

        tvTasksError.setText(
                message != null
                        ? message
                        : "Unable to load tasks."
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

    @Override
    public void onResume() {

        super.onResume();

        if (viewModel != null) {

            viewModel.loadTasks();
        }

        if (categoriesViewModel != null) {

            categoriesViewModel.loadCategories();
        }
    }
}