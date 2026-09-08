package com.smartplanner.app.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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

    // =========================================================
    // ENUMS
    // =========================================================

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

    // =========================================================
    // VIEW MODELS
    // =========================================================

    private TasksViewModel viewModel;
    private CategoriesViewModel categoriesViewModel;

    // =========================================================
    // ADAPTER
    // =========================================================

    private TaskAdapter taskAdapter;

    // =========================================================
    // VIEWS
    // =========================================================

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

    // =========================================================
    // DATA
    // =========================================================

    private final List<Task> allTasks =
            new ArrayList<>();

    private final List<Category> allCategories =
            new ArrayList<>();

    // =========================================================
    // FILTER STATE
    // =========================================================

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

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TasksFragment() {

        super(
                R.layout.fragment_tasks
        );
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        initViews(
                view
        );

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

    // =========================================================
    // INITIALIZATION
    // =========================================================

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
                new ViewModelProvider(
                        this
                )
                        .get(
                                TasksViewModel.class
                        );

        categoriesViewModel =
                new ViewModelProvider(
                        this
                )
                        .get(
                                CategoriesViewModel.class
                        );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnRetryTasks.setOnClickListener(
                view -> {

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

                    startActivity(
                            intent
                    );
                }
        );

        btnAdvancedFilters.setOnClickListener(
                view ->
                        showAdvancedFiltersDialog()
        );

        btnSortTasks.setOnClickListener(
                view ->
                        showSortDialog()
        );

        taskAdapter.setOnImportantClickListener(
                (task, newImportantState) -> {

                    if (task == null
                            || task.getId() == null
                            || task.getId()
                            .trim()
                            .isEmpty()) {

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
                            || task.getId()
                            .trim()
                            .isEmpty()) {

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

                    startActivity(
                            intent
                    );
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
                view ->
                        selectQuickFilter(
                                QuickFilter.ALL
                        )
        );

        chipToDo.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.TO_DO
                        )
        );

        chipInProgress.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.IN_PROGRESS
                        )
        );

        chipCompleted.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.COMPLETED
                        )
        );

        chipOverdue.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.OVERDUE
                        )
        );
    }

    // =========================================================
    // OBSERVERS
    // =========================================================

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

    // =========================================================
    // STATE HANDLERS
    // =========================================================

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
                        || message
                        .trim()
                        .isEmpty()) {

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

    // =========================================================
    // QUICK FILTER
    // =========================================================

    private void selectQuickFilter(
            QuickFilter filter
    ) {

        selectedQuickFilter =
                filter;

        updateQuickFilterSelection();

        applyFiltersAndSorting();
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

    // =========================================================
    // SORT BOTTOM SHEET
    // =========================================================

    private void showSortDialog() {

        View dialogView =
                LayoutInflater
                        .from(
                                requireContext()
                        )
                        .inflate(
                                R.layout.dialog_task_sort,
                                null
                        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );

        dialog.setContentView(
                dialogView
        );

        // -----------------------------------------------------
        // CARDS
        // -----------------------------------------------------

        MaterialCardView cardDefault =
                dialogView.findViewById(
                        R.id.cardSortDefault
                );

        MaterialCardView cardNearest =
                dialogView.findViewById(
                        R.id.cardSortNearest
                );

        MaterialCardView cardFarthest =
                dialogView.findViewById(
                        R.id.cardSortFarthest
                );

        MaterialCardView cardPriority =
                dialogView.findViewById(
                        R.id.cardSortPriority
                );

        MaterialCardView cardNewest =
                dialogView.findViewById(
                        R.id.cardSortNewest
                );

        MaterialCardView cardOldest =
                dialogView.findViewById(
                        R.id.cardSortOldest
                );

        MaterialCardView cardTitle =
                dialogView.findViewById(
                        R.id.cardSortTitle
                );

        // -----------------------------------------------------
        // RADIO BUTTONS
        // -----------------------------------------------------

        RadioButton radioDefault =
                dialogView.findViewById(
                        R.id.radioSortDefault
                );

        RadioButton radioNearest =
                dialogView.findViewById(
                        R.id.radioSortNearest
                );

        RadioButton radioFarthest =
                dialogView.findViewById(
                        R.id.radioSortFarthest
                );

        RadioButton radioPriority =
                dialogView.findViewById(
                        R.id.radioSortPriority
                );

        RadioButton radioNewest =
                dialogView.findViewById(
                        R.id.radioSortNewest
                );

        RadioButton radioOldest =
                dialogView.findViewById(
                        R.id.radioSortOldest
                );

        RadioButton radioTitle =
                dialogView.findViewById(
                        R.id.radioSortTitle
                );

        // -----------------------------------------------------
        // APPLY BUTTON
        // -----------------------------------------------------

        MaterialButton btnApplySort =
                dialogView.findViewById(
                        R.id.btnApplySort
                );

        // -----------------------------------------------------
        // ARRAYS
        // -----------------------------------------------------

        MaterialCardView[] cards = {
                cardDefault,
                cardNearest,
                cardFarthest,
                cardPriority,
                cardNewest,
                cardOldest,
                cardTitle
        };

        RadioButton[] radios = {
                radioDefault,
                radioNearest,
                radioFarthest,
                radioPriority,
                radioNewest,
                radioOldest,
                radioTitle
        };

        SortOption[] options = {
                SortOption.NONE,
                SortOption.NEAREST_DEADLINE,
                SortOption.FARTHEST_DEADLINE,
                SortOption.HIGHEST_PRIORITY,
                SortOption.NEWEST,
                SortOption.OLDEST,
                SortOption.TITLE_A_Z
        };

        // -----------------------------------------------------
        // TEMPORARY STATE
        // -----------------------------------------------------

        final SortOption[] tempSort = {
                selectedSortOption
        };

        updateSortDialogSelection(
                cards,
                radios,
                options,
                tempSort[0]
        );

        // -----------------------------------------------------
        // CARD LISTENERS
        // -----------------------------------------------------

        for (int i = 0;
             i < cards.length;
             i++) {

            final int position =
                    i;

            cards[i].setOnClickListener(
                    view -> {

                        tempSort[0] =
                                options[position];

                        updateSortDialogSelection(
                                cards,
                                radios,
                                options,
                                tempSort[0]
                        );
                    }
            );
        }

        // -----------------------------------------------------
        // APPLY
        // -----------------------------------------------------

        btnApplySort.setOnClickListener(
                view -> {

                    selectedSortOption =
                            tempSort[0];

                    updateSortButton();

                    applyFiltersAndSorting();

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    private void updateSortDialogSelection(
            MaterialCardView[] cards,
            RadioButton[] radios,
            SortOption[] options,
            SortOption selected
    ) {

        int selectedBackground =
                Color.parseColor(
                        "#E8FAF7"
                );

        int normalBackground =
                requireContext()
                        .getColor(
                                R.color.sp_surface
                        );

        int selectedStroke =
                requireContext()
                        .getColor(
                                R.color.sp_teal
                        );

        int normalStroke =
                requireContext()
                        .getColor(
                                R.color.sp_border
                        );

        for (int i = 0;
             i < cards.length;
             i++) {

            boolean isSelected =
                    options[i]
                            == selected;

            radios[i].setChecked(
                    isSelected
            );

            cards[i].setCardBackgroundColor(
                    isSelected
                            ? selectedBackground
                            : normalBackground
            );

            cards[i].setStrokeColor(
                    isSelected
                            ? selectedStroke
                            : normalStroke
            );

            cards[i].setStrokeWidth(
                    isSelected
                            ? dpToPxInt(2)
                            : dpToPxInt(1)
            );
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

    // =========================================================
    // ADVANCED FILTERS BOTTOM SHEET
    // =========================================================

    private void showAdvancedFiltersDialog() {

        View dialogView =
                LayoutInflater
                        .from(
                                requireContext()
                        )
                        .inflate(
                                R.layout.dialog_task_filters,
                                null
                        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );

        dialog.setContentView(
                dialogView
        );

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        TextView chipStatusAny =
                dialogView.findViewById(
                        R.id.chipFilterStatusAny
                );

        TextView chipStatusToDo =
                dialogView.findViewById(
                        R.id.chipFilterStatusToDo
                );

        TextView chipStatusInProgress =
                dialogView.findViewById(
                        R.id.chipFilterStatusInProgress
                );

        TextView chipStatusCompleted =
                dialogView.findViewById(
                        R.id.chipFilterStatusCompleted
                );

        // -----------------------------------------------------
        // PRIORITY
        // -----------------------------------------------------

        TextView chipPriorityAny =
                dialogView.findViewById(
                        R.id.chipFilterPriorityAny
                );

        TextView chipPriorityLow =
                dialogView.findViewById(
                        R.id.chipFilterPriorityLow
                );

        TextView chipPriorityMedium =
                dialogView.findViewById(
                        R.id.chipFilterPriorityMedium
                );

        TextView chipPriorityHigh =
                dialogView.findViewById(
                        R.id.chipFilterPriorityHigh
                );

        // -----------------------------------------------------
        // IMPORTANT
        // -----------------------------------------------------

        TextView chipImportantAny =
                dialogView.findViewById(
                        R.id.chipFilterImportantAny
                );

        TextView chipImportantOnly =
                dialogView.findViewById(
                        R.id.chipFilterImportantOnly
                );

        TextView chipImportantNot =
                dialogView.findViewById(
                        R.id.chipFilterImportantNot
                );

        // -----------------------------------------------------
        // DROPDOWNS
        // -----------------------------------------------------

        MaterialAutoCompleteTextView dropdownCategory =
                dialogView.findViewById(
                        R.id.dropdownFilterCategory
                );

        MaterialAutoCompleteTextView dropdownDate =
                dialogView.findViewById(
                        R.id.dropdownFilterDate
                );

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------

        TextView btnClearFilters =
                dialogView.findViewById(
                        R.id.btnClearFilters
                );

        MaterialButton btnApplyFilters =
                dialogView.findViewById(
                        R.id.btnApplyFilters
                );

        // -----------------------------------------------------
        // TEMPORARY FILTER STATE
        // -----------------------------------------------------

        final AdvancedStatusFilter[] tempStatus = {
                selectedAdvancedStatus
        };

        final AdvancedPriorityFilter[] tempPriority = {
                selectedAdvancedPriority
        };

        final AdvancedImportantFilter[] tempImportant = {
                selectedAdvancedImportant
        };

        final AdvancedDateFilter[] tempDate = {
                selectedAdvancedDate
        };

        final String[] tempCategoryId = {
                selectedCategoryId
        };

        // -----------------------------------------------------
        // CATEGORY OPTIONS
        // -----------------------------------------------------

        List<String> categoryOptions =
                new ArrayList<>();

        categoryOptions.add(
                "Any category"
        );

        for (Category category
                : allCategories) {

            if (category != null
                    && category.getName() != null
                    && !category
                    .getName()
                    .trim()
                    .isEmpty()) {

                categoryOptions.add(
                        category.getName()
                );
            }
        }

        dropdownCategory.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        categoryOptions
                )
        );

        dropdownCategory.setText(
                getSelectedCategoryText(),
                false
        );

        dropdownCategory.setOnItemClickListener(
                (parent,
                 view,
                 position,
                 id) -> {

                    String selected =
                            categoryOptions.get(
                                    position
                            );

                    tempCategoryId[0] =
                            findCategoryIdByName(
                                    selected
                            );
                }
        );

        // -----------------------------------------------------
        // DATE OPTIONS
        // -----------------------------------------------------

        String[] dateOptions = {
                "Any date",
                "Today",
                "Tomorrow",
                "This week",
                "No deadline"
        };

        dropdownDate.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        dateOptions
                )
        );

        dropdownDate.setText(
                getAdvancedDateText(),
                false
        );

        dropdownDate.setOnItemClickListener(
                (parent,
                 view,
                 position,
                 id) -> {

                    switch (position) {

                        case 1:

                            tempDate[0] =
                                    AdvancedDateFilter.TODAY;

                            break;

                        case 2:

                            tempDate[0] =
                                    AdvancedDateFilter.TOMORROW;

                            break;

                        case 3:

                            tempDate[0] =
                                    AdvancedDateFilter.THIS_WEEK;

                            break;

                        case 4:

                            tempDate[0] =
                                    AdvancedDateFilter.NO_DEADLINE;

                            break;

                        case 0:
                        default:

                            tempDate[0] =
                                    AdvancedDateFilter.ANY;

                            break;
                    }
                }
        );

        // -----------------------------------------------------
        // INITIAL CHIP STATE
        // -----------------------------------------------------

        updateStatusDialogChips(
                chipStatusAny,
                chipStatusToDo,
                chipStatusInProgress,
                chipStatusCompleted,
                tempStatus[0]
        );

        updatePriorityDialogChips(
                chipPriorityAny,
                chipPriorityLow,
                chipPriorityMedium,
                chipPriorityHigh,
                tempPriority[0]
        );

        updateImportantDialogChips(
                chipImportantAny,
                chipImportantOnly,
                chipImportantNot,
                tempImportant[0]
        );

        // -----------------------------------------------------
        // STATUS LISTENERS
        // -----------------------------------------------------

        chipStatusAny.setOnClickListener(
                view -> {

                    tempStatus[0] =
                            AdvancedStatusFilter.ANY;

                    updateStatusDialogChips(
                            chipStatusAny,
                            chipStatusToDo,
                            chipStatusInProgress,
                            chipStatusCompleted,
                            tempStatus[0]
                    );
                }
        );

        chipStatusToDo.setOnClickListener(
                view -> {

                    tempStatus[0] =
                            AdvancedStatusFilter.TO_DO;

                    updateStatusDialogChips(
                            chipStatusAny,
                            chipStatusToDo,
                            chipStatusInProgress,
                            chipStatusCompleted,
                            tempStatus[0]
                    );
                }
        );

        chipStatusInProgress.setOnClickListener(
                view -> {

                    tempStatus[0] =
                            AdvancedStatusFilter.IN_PROGRESS;

                    updateStatusDialogChips(
                            chipStatusAny,
                            chipStatusToDo,
                            chipStatusInProgress,
                            chipStatusCompleted,
                            tempStatus[0]
                    );
                }
        );

        chipStatusCompleted.setOnClickListener(
                view -> {

                    tempStatus[0] =
                            AdvancedStatusFilter.COMPLETED;

                    updateStatusDialogChips(
                            chipStatusAny,
                            chipStatusToDo,
                            chipStatusInProgress,
                            chipStatusCompleted,
                            tempStatus[0]
                    );
                }
        );

        // -----------------------------------------------------
        // PRIORITY LISTENERS
        // -----------------------------------------------------

        chipPriorityAny.setOnClickListener(
                view -> {

                    tempPriority[0] =
                            AdvancedPriorityFilter.ANY;

                    updatePriorityDialogChips(
                            chipPriorityAny,
                            chipPriorityLow,
                            chipPriorityMedium,
                            chipPriorityHigh,
                            tempPriority[0]
                    );
                }
        );

        chipPriorityLow.setOnClickListener(
                view -> {

                    tempPriority[0] =
                            AdvancedPriorityFilter.LOW;

                    updatePriorityDialogChips(
                            chipPriorityAny,
                            chipPriorityLow,
                            chipPriorityMedium,
                            chipPriorityHigh,
                            tempPriority[0]
                    );
                }
        );

        chipPriorityMedium.setOnClickListener(
                view -> {

                    tempPriority[0] =
                            AdvancedPriorityFilter.MEDIUM;

                    updatePriorityDialogChips(
                            chipPriorityAny,
                            chipPriorityLow,
                            chipPriorityMedium,
                            chipPriorityHigh,
                            tempPriority[0]
                    );
                }
        );

        chipPriorityHigh.setOnClickListener(
                view -> {

                    tempPriority[0] =
                            AdvancedPriorityFilter.HIGH;

                    updatePriorityDialogChips(
                            chipPriorityAny,
                            chipPriorityLow,
                            chipPriorityMedium,
                            chipPriorityHigh,
                            tempPriority[0]
                    );
                }
        );

        // -----------------------------------------------------
        // IMPORTANT LISTENERS
        // -----------------------------------------------------

        chipImportantAny.setOnClickListener(
                view -> {

                    tempImportant[0] =
                            AdvancedImportantFilter.ANY;

                    updateImportantDialogChips(
                            chipImportantAny,
                            chipImportantOnly,
                            chipImportantNot,
                            tempImportant[0]
                    );
                }
        );

        chipImportantOnly.setOnClickListener(
                view -> {

                    tempImportant[0] =
                            AdvancedImportantFilter.IMPORTANT;

                    updateImportantDialogChips(
                            chipImportantAny,
                            chipImportantOnly,
                            chipImportantNot,
                            tempImportant[0]
                    );
                }
        );

        chipImportantNot.setOnClickListener(
                view -> {

                    tempImportant[0] =
                            AdvancedImportantFilter.NOT_IMPORTANT;

                    updateImportantDialogChips(
                            chipImportantAny,
                            chipImportantOnly,
                            chipImportantNot,
                            tempImportant[0]
                    );
                }
        );

        // -----------------------------------------------------
        // CLEAR
        // -----------------------------------------------------

        btnClearFilters.setOnClickListener(
                view -> {

                    tempStatus[0] =
                            AdvancedStatusFilter.ANY;

                    tempPriority[0] =
                            AdvancedPriorityFilter.ANY;

                    tempImportant[0] =
                            AdvancedImportantFilter.ANY;

                    tempDate[0] =
                            AdvancedDateFilter.ANY;

                    tempCategoryId[0] =
                            null;

                    dropdownCategory.setText(
                            "Any category",
                            false
                    );

                    dropdownDate.setText(
                            "Any date",
                            false
                    );

                    updateStatusDialogChips(
                            chipStatusAny,
                            chipStatusToDo,
                            chipStatusInProgress,
                            chipStatusCompleted,
                            tempStatus[0]
                    );

                    updatePriorityDialogChips(
                            chipPriorityAny,
                            chipPriorityLow,
                            chipPriorityMedium,
                            chipPriorityHigh,
                            tempPriority[0]
                    );

                    updateImportantDialogChips(
                            chipImportantAny,
                            chipImportantOnly,
                            chipImportantNot,
                            tempImportant[0]
                    );
                }
        );

        // -----------------------------------------------------
        // APPLY
        // -----------------------------------------------------

        btnApplyFilters.setOnClickListener(
                view -> {

                    selectedAdvancedStatus =
                            tempStatus[0];

                    selectedAdvancedPriority =
                            tempPriority[0];

                    selectedAdvancedImportant =
                            tempImportant[0];

                    selectedAdvancedDate =
                            tempDate[0];

                    tempCategoryId[0] =
                            findCategoryIdByName(
                                    dropdownCategory
                                            .getText()
                                            .toString()
                            );

                    selectedCategoryId =
                            tempCategoryId[0];

                    updateAdvancedFilterButton();

                    applyFiltersAndSorting();

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    // =========================================================
    // FILTER DIALOG UI
    // =========================================================

    private void updateStatusDialogChips(
            TextView chipAny,
            TextView chipToDo,
            TextView chipInProgress,
            TextView chipCompleted,
            AdvancedStatusFilter selected
    ) {

        setDialogChipSelected(
                chipAny,
                selected
                        == AdvancedStatusFilter.ANY
        );

        setDialogChipSelected(
                chipToDo,
                selected
                        == AdvancedStatusFilter.TO_DO
        );

        setDialogChipSelected(
                chipInProgress,
                selected
                        == AdvancedStatusFilter.IN_PROGRESS
        );

        setDialogChipSelected(
                chipCompleted,
                selected
                        == AdvancedStatusFilter.COMPLETED
        );
    }

    private void updatePriorityDialogChips(
            TextView chipAny,
            TextView chipLow,
            TextView chipMedium,
            TextView chipHigh,
            AdvancedPriorityFilter selected
    ) {

        setDialogChipSelected(
                chipAny,
                selected
                        == AdvancedPriorityFilter.ANY
        );

        setDialogChipSelected(
                chipLow,
                selected
                        == AdvancedPriorityFilter.LOW
        );

        setDialogChipSelected(
                chipMedium,
                selected
                        == AdvancedPriorityFilter.MEDIUM
        );

        setDialogChipSelected(
                chipHigh,
                selected
                        == AdvancedPriorityFilter.HIGH
        );
    }

    private void updateImportantDialogChips(
            TextView chipAny,
            TextView chipImportant,
            TextView chipNotImportant,
            AdvancedImportantFilter selected
    ) {

        setDialogChipSelected(
                chipAny,
                selected
                        == AdvancedImportantFilter.ANY
        );

        setDialogChipSelected(
                chipImportant,
                selected
                        == AdvancedImportantFilter.IMPORTANT
        );

        setDialogChipSelected(
                chipNotImportant,
                selected
                        == AdvancedImportantFilter.NOT_IMPORTANT
        );
    }

    private void setDialogChipSelected(
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

    // =========================================================
    // FILTER BUTTON
    // =========================================================

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

    // =========================================================
    // CATEGORY HELPERS
    // =========================================================

    private String findCategoryIdByName(
            String categoryName
    ) {

        if (categoryName == null
                || categoryName
                .trim()
                .isEmpty()
                || categoryName.equals(
                "Any category"
        )) {

            return null;
        }

        for (Category category
                : allCategories) {

            if (category != null
                    && category.getName() != null
                    && category.getName()
                    .equals(
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

        for (Category category
                : allCategories) {

            if (category != null
                    && selectedCategoryId.equals(
                    category.getId()
            )) {

                return category.getName();
            }
        }

        return "Any category";
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

    // =========================================================
    // FILTER + SORT ENGINE
    // =========================================================

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

        for (Task task
                : allTasks) {

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

    // =========================================================
    // SORT ENGINE
    // =========================================================

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
                        (first,
                         second) ->
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
                        (first,
                         second) ->
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
                        (first,
                         second) ->
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
                        (first,
                         second) ->
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
                        (first,
                         second) ->
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
                        (first,
                         second) -> {

                            String firstTitle =
                                    first.getTitle() == null
                                            ? ""
                                            : first.getTitle();

                            String secondTitle =
                                    second.getTitle() == null
                                            ? ""
                                            : second.getTitle();

                            return firstTitle
                                    .compareToIgnoreCase(
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
                || task.getDeadline()
                .trim()
                .isEmpty()) {

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
                || task.getCreatedAt()
                .trim()
                .isEmpty()) {

            return null;
        }

        return parseSupabaseDate(
                task.getCreatedAt()
        );
    }

    // =========================================================
    // FILTER MATCHERS
    // =========================================================

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
                        && !isOverdue(
                        task
                );

            case IN_PROGRESS:

                return task.getStatus()
                        == TaskStatus.IN_PROGRESS
                        && !isOverdue(
                        task
                );

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
                    || deadline
                    .trim()
                    .isEmpty();
        }

        if (deadline == null
                || deadline
                .trim()
                .isEmpty()) {

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

    // =========================================================
    // DATE HELPERS
    // =========================================================

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
                || deadline
                .trim()
                .isEmpty()) {

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

        for (String format
                : formats) {

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

    // =========================================================
    // UI STATES
    // =========================================================

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

    // =========================================================
    // UI HELPERS
    // =========================================================

    private int dpToPxInt(
            int dp
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }
}