package com.smartplanner.app.activities.smartplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.task.TaskDetailsActivity;
import com.smartplanner.app.adapters.SmartPlanAdapter;
import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.enums.SmartPlanStatus;
import com.smartplanner.app.repositories.SmartPlanGenerationRepository;
import com.smartplanner.app.viewmodels.SmartPlanViewModel;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SmartPlanActivity
        extends AppCompatActivity {

    // =========================================================
    // VIEW MODEL
    // =========================================================

    private SmartPlanViewModel viewModel;


    // =========================================================
    // ADAPTER
    // =========================================================

    private SmartPlanAdapter smartPlanAdapter;


    // =========================================================
    // HEADER
    // =========================================================

    private TextView btnBackSmartPlan;


    // =========================================================
    // STATUS
    // =========================================================

    private TextView tvSmartPlanStatus;
    private TextView tvSmartPlanPeriod;
    private TextView tvSmartPlanStatusDescription;


    // =========================================================
    // VIEW MODE
    // =========================================================

    private MaterialButton btnSmartPlanToday;
    private MaterialButton btnSmartPlanWeek;

    private TextView tvSmartPlanSectionTitle;
    private TextView tvSmartPlanSessionCount;


    // =========================================================
    // CONTENT
    // =========================================================

    private RecyclerView recyclerSmartPlan;

    private ProgressBar progressSmartPlan;

    private LinearLayout layoutSmartPlanEmpty;
    private LinearLayout layoutSmartPlanError;

    private TextView tvSmartPlanEmptyTitle;
    private TextView tvSmartPlanEmptyDescription;
    private TextView tvSmartPlanError;


    // =========================================================
    // ACTIONS
    // =========================================================

    private MaterialButton btnRetrySmartPlan;
    private MaterialButton btnGenerateSmartPlan;


    // =========================================================
    // LOCAL STATE
    // =========================================================

    private SmartPlan currentPlan;

    private List<SmartPlanItem> currentItems =
            new ArrayList<>();

    private boolean weekViewSelected =
            false;

    private boolean generating =
            false;


    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_smart_plan
        );


        initViews();

        setupRecyclerView();

        setupViewModel();

        setupListeners();

        observeCurrentPlan();

        observeSmartPlanItems();

        observeTasks();

        observeGeneration();


        selectTodayView();


        viewModel.loadCurrentSmartPlan();

        viewModel.loadTasks();
    }


    @Override
    protected void onResume() {

        super.onResume();


        /*
         * If the user opens TaskDetailsActivity and edits the
         * Task title, returning to Smart Plan should refresh
         * the titles shown on session cards.
         */
        if (viewModel != null) {

            viewModel.loadTasks();
        }
    }


    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        btnBackSmartPlan =
                findViewById(
                        R.id.btnBackSmartPlan
                );


        tvSmartPlanStatus =
                findViewById(
                        R.id.tvSmartPlanStatus
                );

        tvSmartPlanPeriod =
                findViewById(
                        R.id.tvSmartPlanPeriod
                );

        tvSmartPlanStatusDescription =
                findViewById(
                        R.id.tvSmartPlanStatusDescription
                );


        btnSmartPlanToday =
                findViewById(
                        R.id.btnSmartPlanToday
                );

        btnSmartPlanWeek =
                findViewById(
                        R.id.btnSmartPlanWeek
                );


        tvSmartPlanSectionTitle =
                findViewById(
                        R.id.tvSmartPlanSectionTitle
                );

        tvSmartPlanSessionCount =
                findViewById(
                        R.id.tvSmartPlanSessionCount
                );


        recyclerSmartPlan =
                findViewById(
                        R.id.recyclerSmartPlan
                );


        progressSmartPlan =
                findViewById(
                        R.id.progressSmartPlan
                );


        layoutSmartPlanEmpty =
                findViewById(
                        R.id.layoutSmartPlanEmpty
                );

        layoutSmartPlanError =
                findViewById(
                        R.id.layoutSmartPlanError
                );


        tvSmartPlanEmptyTitle =
                findViewById(
                        R.id.tvSmartPlanEmptyTitle
                );

        tvSmartPlanEmptyDescription =
                findViewById(
                        R.id.tvSmartPlanEmptyDescription
                );

        tvSmartPlanError =
                findViewById(
                        R.id.tvSmartPlanError
                );


        btnRetrySmartPlan =
                findViewById(
                        R.id.btnRetrySmartPlan
                );

        btnGenerateSmartPlan =
                findViewById(
                        R.id.btnGenerateSmartPlan
                );
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        smartPlanAdapter =
                new SmartPlanAdapter(
                        this::openTaskDetails
                );


        recyclerSmartPlan.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        recyclerSmartPlan.setAdapter(
                smartPlanAdapter
        );


        recyclerSmartPlan.setHasFixedSize(
                false
        );
    }


    // =========================================================
    // VIEW MODEL
    // =========================================================

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                SmartPlanViewModel.class
                        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBackSmartPlan.setOnClickListener(
                view -> finish()
        );


        btnSmartPlanToday.setOnClickListener(
                view ->
                        selectTodayView()
        );


        btnSmartPlanWeek.setOnClickListener(
                view ->
                        selectWeekView()
        );


        btnRetrySmartPlan.setOnClickListener(
                view -> {

                    viewModel.refreshSmartPlan();

                    viewModel.loadTasks();
                }
        );


        btnGenerateSmartPlan.setOnClickListener(
                view ->
                        handleGenerateButtonClick()
        );
    }


    // =========================================================
    // OPEN TASK DETAILS
    // =========================================================

    private void openTaskDetails(
            SmartPlanItem item
    ) {

        if (item == null
                || item.getTaskId() == null
                || item.getTaskId()
                .trim()
                .isEmpty()) {

            Toast.makeText(
                    this,
                    "Unable to open task.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        this,
                        TaskDetailsActivity.class
                );


        intent.putExtra(
                TaskDetailsActivity.EXTRA_TASK_ID,
                item.getTaskId()
        );


        startActivity(
                intent
        );
    }


    // =========================================================
    // GENERATE / REGENERATE
    // =========================================================

    private void handleGenerateButtonClick() {

        if (generating) {
            return;
        }


        if (currentPlan == null) {

            viewModel.generateSmartPlan();

        } else {

            viewModel.regenerateSmartPlan();
        }
    }


    // =========================================================
    // OBSERVE CURRENT PLAN
    // =========================================================

    private void observeCurrentPlan() {

        viewModel
                .getCurrentPlanState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }


                            switch (state.getStatus()) {

                                case LOADING:

                                    showLoading();

                                    break;


                                case SUCCESS:

                                    currentPlan =
                                            state.getData();


                                    updatePlanHeader();


                                    if (currentPlan == null) {

                                        currentItems =
                                                new ArrayList<>();

                                        smartPlanAdapter.setItems(
                                                new ArrayList<>()
                                        );


                                        updateSessionCount();

                                        showNoPlanState();
                                    }

                                    break;


                                case ERROR:

                                    showError(
                                            state.getMessage()
                                    );

                                    break;
                            }
                        }
                );
    }


    // =========================================================
    // OBSERVE ITEMS
    // =========================================================

    private void observeSmartPlanItems() {

        viewModel
                .getSmartPlanItemsState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }


                            switch (state.getStatus()) {

                                case LOADING:

                                    if (currentPlan != null
                                            && !generating) {

                                        showLoading();
                                    }

                                    break;


                                case SUCCESS:

                                    currentItems =
                                            state.getData() != null
                                                    ? new ArrayList<>(
                                                    state.getData()
                                            )
                                                    : new ArrayList<>();


                                    sortItems(
                                            currentItems
                                    );


                                    updateDisplayedItems();

                                    break;


                                case ERROR:

                                    if (!generating) {

                                        showError(
                                                state.getMessage()
                                        );
                                    }

                                    break;
                            }
                        }
                );
    }


    // =========================================================
    // OBSERVE TASKS
    // =========================================================

    private void observeTasks() {

        viewModel
                .getTasksState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }


                            switch (state.getStatus()) {

                                case LOADING:

                                    /*
                                     * Do not hide Smart Plan while
                                     * Task titles are loading.
                                     *
                                     * Existing session cards may
                                     * temporarily display the
                                     * fallback "Task session".
                                     */
                                    break;


                                case SUCCESS:

                                    List<Task> tasks =
                                            state.getData() != null
                                                    ? state.getData()
                                                    : new ArrayList<>();


                                    smartPlanAdapter.setTasks(
                                            tasks
                                    );

                                    break;


                                case ERROR:

                                    /*
                                     * Task title loading is secondary.
                                     *
                                     * Smart Plan sessions remain usable
                                     * even if this request fails.
                                     */
                                    smartPlanAdapter.setTasks(
                                            new ArrayList<>()
                                    );

                                    break;
                            }
                        }
                );
    }


    // =========================================================
    // OBSERVE GENERATION
    // =========================================================

    private void observeGeneration() {

        viewModel
                .getGenerationState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }


                            switch (state.getStatus()) {

                                case LOADING:

                                    setGenerating(
                                            true
                                    );

                                    showLoading();

                                    break;


                                case SUCCESS:

                                    boolean firstGeneration =
                                            currentPlan == null;


                                    setGenerating(
                                            false
                                    );


                                    SmartPlanGenerationRepository
                                            .GenerationResult result =
                                            state.getData();


                                    if (result == null) {

                                        showError(
                                                "Unable to generate Smart Plan."
                                        );

                                        return;
                                    }


                                    if (!result.isPlanCreated()) {

                                        handleNoGeneratedSessions();

                                        return;
                                    }


                                    Toast.makeText(
                                            this,
                                            firstGeneration
                                                    ? "Smart Plan generated."
                                                    : "Smart Plan regenerated.",
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    /*
                                     * Refresh Task map as well so every
                                     * new Smart Plan item can immediately
                                     * display its Task title.
                                     */
                                    viewModel.loadTasks();

                                    break;


                                case ERROR:

                                    setGenerating(
                                            false
                                    );


                                    String message =
                                            state.getMessage();


                                    if (message == null
                                            || message.trim().isEmpty()) {

                                        message =
                                                "Unable to generate Smart Plan.";
                                    }


                                    Toast.makeText(
                                            this,
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();


                                    restoreCurrentScreen();

                                    break;
                            }
                        }
                );
    }


    // =========================================================
    // NO GENERATED SESSIONS
    // =========================================================

    private void handleNoGeneratedSessions() {

        updatePlanHeader();

        updateSessionCount();


        if (currentPlan == null) {

            showEmptyState(
                    "Nothing to schedule",
                    "Smart Plan could not create any sessions from your current tasks, availability and planning settings."
            );

        } else {

            restoreCurrentScreen();
        }
    }


    // =========================================================
    // DISPLAY ITEMS
    // =========================================================

    private void updateDisplayedItems() {

        List<SmartPlanItem> visibleItems;


        if (weekViewSelected) {

            visibleItems =
                    new ArrayList<>(
                            currentItems
                    );

        } else {

            visibleItems =
                    getTodayItems();
        }


        smartPlanAdapter.setItems(
                visibleItems
        );


        updateSessionCount();


        if (currentPlan == null) {

            showNoPlanState();

            return;
        }


        if (visibleItems.isEmpty()) {

            if (weekViewSelected) {

                showEmptyState(
                        "No planned sessions",
                        "There are no sessions in the current Smart Plan."
                );

            } else {

                showEmptyState(
                        "Nothing planned for today",
                        "You have no Smart Plan sessions scheduled for today."
                );
            }

            return;
        }


        showContent();
    }


    // =========================================================
    // TODAY ITEMS
    // =========================================================

    private List<SmartPlanItem> getTodayItems() {

        List<SmartPlanItem> todayItems =
                new ArrayList<>();


        LocalDate today =
                LocalDate.now();


        for (SmartPlanItem item :
                currentItems) {

            if (item == null) {
                continue;
            }


            LocalDate itemDate =
                    parseSmartPlanItemDate(
                            item.getPlannedStart()
                    );


            if (today.equals(
                    itemDate
            )) {

                todayItems.add(
                        item
                );
            }
        }


        return todayItems;
    }


    // =========================================================
    // SORT ITEMS
    // =========================================================

    private void sortItems(
            List<SmartPlanItem> items
    ) {

        if (items == null
                || items.size() < 2) {

            return;
        }


        Collections.sort(
                items,
                new Comparator<SmartPlanItem>() {

                    @Override
                    public int compare(
                            SmartPlanItem first,
                            SmartPlanItem second
                    ) {

                        ZonedDateTime firstDate =
                                parseSmartPlanItemDateTime(
                                        first != null
                                                ? first.getPlannedStart()
                                                : null
                                );

                        ZonedDateTime secondDate =
                                parseSmartPlanItemDateTime(
                                        second != null
                                                ? second.getPlannedStart()
                                                : null
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


                        return firstDate.compareTo(
                                secondDate
                        );
                    }
                }
        );
    }


    // =========================================================
    // PLAN HEADER
    // =========================================================

    private void updatePlanHeader() {

        if (currentPlan == null) {

            tvSmartPlanStatus.setText(
                    "NOT GENERATED"
            );

            tvSmartPlanPeriod.setText(
                    "No active planning period"
            );

            tvSmartPlanStatusDescription.setText(
                    "Generate your first Smart Plan to create a schedule for the next seven days."
            );

            btnGenerateSmartPlan.setText(
                    "Generate Smart Plan"
            );

            return;
        }


        updatePlanPeriod(
                currentPlan
        );


        SmartPlanStatus status =
                currentPlan.getStatus();


        if (status == SmartPlanStatus.NEEDS_UPDATE) {

            tvSmartPlanStatus.setText(
                    "NEEDS UPDATE"
            );

            tvSmartPlanStatusDescription.setText(
                    "Something affecting your schedule has changed. Regenerate the plan to update your sessions."
            );

            btnGenerateSmartPlan.setText(
                    "Regenerate Smart Plan"
            );

            return;
        }


        if (status == SmartPlanStatus.ACTIVE) {

            tvSmartPlanStatus.setText(
                    "ACTIVE"
            );

            tvSmartPlanStatusDescription.setText(
                    "Your Smart Plan is ready and organized around your current tasks, events and availability."
            );

            btnGenerateSmartPlan.setText(
                    "Regenerate Smart Plan"
            );

            return;
        }


        tvSmartPlanStatus.setText(
                status != null
                        ? status.name()
                        : "UNKNOWN"
        );

        tvSmartPlanStatusDescription.setText(
                "This Smart Plan is not currently active."
        );
    }


    // =========================================================
    // PLAN PERIOD
    // =========================================================

    private void updatePlanPeriod(
            SmartPlan smartPlan
    ) {

        LocalDate start =
                parseLocalDate(
                        smartPlan.getPeriodStart()
                );

        LocalDate end =
                parseLocalDate(
                        smartPlan.getPeriodEnd()
                );


        if (start == null
                || end == null) {

            tvSmartPlanPeriod.setText(
                    "7-day Smart Plan"
            );

            return;
        }


        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "MMM d",
                        Locale.ENGLISH
                );


        tvSmartPlanPeriod.setText(
                start.format(
                        formatter
                )
                        + " – "
                        + end.format(
                        formatter
                )
        );
    }


    // =========================================================
    // TODAY / WEEK
    // =========================================================

    private void selectTodayView() {

        weekViewSelected =
                false;


        btnSmartPlanToday.setAlpha(
                1.0f
        );

        btnSmartPlanWeek.setAlpha(
                0.65f
        );


        btnSmartPlanToday.setEnabled(
                false
        );

        btnSmartPlanWeek.setEnabled(
                !generating
        );


        tvSmartPlanSectionTitle.setText(
                "TODAY'S PLAN"
        );


        updateDisplayedItems();
    }


    private void selectWeekView() {

        weekViewSelected =
                true;


        btnSmartPlanToday.setAlpha(
                0.65f
        );

        btnSmartPlanWeek.setAlpha(
                1.0f
        );


        btnSmartPlanToday.setEnabled(
                !generating
        );

        btnSmartPlanWeek.setEnabled(
                false
        );


        tvSmartPlanSectionTitle.setText(
                "WEEK PLAN"
        );


        updateDisplayedItems();
    }


    // =========================================================
    // SESSION COUNT
    // =========================================================

    private void updateSessionCount() {

        int count =
                weekViewSelected
                        ? currentItems.size()
                        : getTodayItems().size();


        tvSmartPlanSessionCount.setText(
                count
                        + (count == 1
                        ? " session"
                        : " sessions")
        );
    }


    // =========================================================
    // SCREEN STATES
    // =========================================================

    private void showLoading() {

        progressSmartPlan.setVisibility(
                View.VISIBLE
        );

        recyclerSmartPlan.setVisibility(
                View.GONE
        );

        layoutSmartPlanEmpty.setVisibility(
                View.GONE
        );

        layoutSmartPlanError.setVisibility(
                View.GONE
        );
    }


    private void showContent() {

        progressSmartPlan.setVisibility(
                View.GONE
        );

        recyclerSmartPlan.setVisibility(
                View.VISIBLE
        );

        layoutSmartPlanEmpty.setVisibility(
                View.GONE
        );

        layoutSmartPlanError.setVisibility(
                View.GONE
        );
    }


    private void showNoPlanState() {

        showEmptyState(
                "No Smart Plan yet",
                "Generate a Smart Plan to automatically organize your tasks around your availability and events."
        );
    }


    private void showEmptyState(
            String title,
            String description
    ) {

        progressSmartPlan.setVisibility(
                View.GONE
        );

        recyclerSmartPlan.setVisibility(
                View.GONE
        );

        layoutSmartPlanEmpty.setVisibility(
                View.VISIBLE
        );

        layoutSmartPlanError.setVisibility(
                View.GONE
        );


        tvSmartPlanEmptyTitle.setText(
                title
        );

        tvSmartPlanEmptyDescription.setText(
                description
        );
    }


    private void showError(
            String message
    ) {

        progressSmartPlan.setVisibility(
                View.GONE
        );

        recyclerSmartPlan.setVisibility(
                View.GONE
        );

        layoutSmartPlanEmpty.setVisibility(
                View.GONE
        );

        layoutSmartPlanError.setVisibility(
                View.VISIBLE
        );


        if (message == null
                || message.trim().isEmpty()) {

            message =
                    "Unable to load Smart Plan.";
        }


        tvSmartPlanError.setText(
                message
        );
    }


    private void restoreCurrentScreen() {

        updatePlanHeader();

        updateDisplayedItems();
    }


    // =========================================================
    // GENERATION STATE
    // =========================================================

    private void setGenerating(
            boolean generating
    ) {

        this.generating =
                generating;


        btnGenerateSmartPlan.setEnabled(
                !generating
        );


        if (generating) {

            btnGenerateSmartPlan.setText(
                    currentPlan == null
                            ? "Generating..."
                            : "Regenerating..."
            );


            btnSmartPlanToday.setEnabled(
                    false
            );

            btnSmartPlanWeek.setEnabled(
                    false
            );

        } else {

            updatePlanHeader();


            btnSmartPlanToday.setEnabled(
                    weekViewSelected
            );

            btnSmartPlanWeek.setEnabled(
                    !weekViewSelected
            );
        }
    }


    // =========================================================
    // DATE HELPERS
    // =========================================================

    private LocalDate parseLocalDate(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }


        try {

            return LocalDate.parse(
                    value.trim()
            );

        } catch (Exception exception) {

            return null;
        }
    }


    private LocalDate parseSmartPlanItemDate(
            String value
    ) {

        ZonedDateTime dateTime =
                parseSmartPlanItemDateTime(
                        value
                );


        return dateTime != null
                ? dateTime.toLocalDate()
                : null;
    }


    private ZonedDateTime parseSmartPlanItemDateTime(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }


        try {

            OffsetDateTime offsetDateTime =
                    OffsetDateTime.parse(
                            value.trim()
                    );


            return offsetDateTime
                    .atZoneSameInstant(
                            ZoneId.systemDefault()
                    );

        } catch (Exception ignored) {
        }


        try {

            return ZonedDateTime
                    .parse(
                            value.trim()
                    )
                    .withZoneSameInstant(
                            ZoneId.systemDefault()
                    );

        } catch (Exception exception) {

            return null;
        }
    }
}