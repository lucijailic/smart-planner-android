package com.smartplanner.app.activities.smartplan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.task.TaskDetailsActivity;
import com.smartplanner.app.adapters.SmartPlanAdapter;
import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;
import com.smartplanner.app.models.enums.SmartPlanStatus;
import com.smartplanner.app.repositories.SmartPlanGenerationRepository;
import com.smartplanner.app.viewmodels.SmartPlanViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SmartPlanActivity extends AppCompatActivity {

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

    private boolean sessionActionInProgress =
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

        observeSessionActions();


        selectTodayView();


        viewModel.loadCurrentSmartPlan();

        viewModel.loadTasks();
    }


    @Override
    protected void onResume() {

        super.onResume();


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
                        this::showSessionActions
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
    // SESSION ACTIONS BOTTOM SHEET
    // =========================================================

    private void showSessionActions(
            SmartPlanItem item
    ) {

        if (item == null) {
            return;
        }


        if (sessionActionInProgress) {

            Toast.makeText(
                    this,
                    "Please wait for the current action to finish.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(
                        this
                );


        View sheetView =
                getLayoutInflater().inflate(
                        R.layout.bottom_sheet_smart_plan_session_actions,
                        null
                );


        bottomSheetDialog.setContentView(
                sheetView
        );


        // =====================================================
        // SHEET VIEWS
        // =====================================================

        TextView tvSubtitle =
                sheetView.findViewById(
                        R.id.tvSessionActionsSubtitle
                );


        View actionViewTask =
                sheetView.findViewById(
                        R.id.actionViewTask
                );


        View actionMove =
                sheetView.findViewById(
                        R.id.actionMoveSession
                );


        View actionComplete =
                sheetView.findViewById(
                        R.id.actionCompleteSession
                );


        View actionSkip =
                sheetView.findViewById(
                        R.id.actionSkipSession
                );


        View actionRemove =
                sheetView.findViewById(
                        R.id.actionRemoveSession
                );


        View dividerRemove =
                sheetView.findViewById(
                        R.id.dividerRemoveSession
                );


        // =====================================================
        // STATUS-SPECIFIC CONTENT
        // =====================================================

        SmartPlanItemStatus status =
                item.getStatus();


        if (status == SmartPlanItemStatus.COMPLETED) {

            tvSubtitle.setText(
                    "This session has already been completed."
            );


            actionMove.setVisibility(
                    View.GONE
            );

            actionComplete.setVisibility(
                    View.GONE
            );

            actionSkip.setVisibility(
                    View.GONE
            );

            actionRemove.setVisibility(
                    View.GONE
            );

            dividerRemove.setVisibility(
                    View.GONE
            );

        } else if (status == SmartPlanItemStatus.SKIPPED) {

            tvSubtitle.setText(
                    "This session has been skipped."
            );


            actionMove.setVisibility(
                    View.GONE
            );

            actionComplete.setVisibility(
                    View.GONE
            );

            actionSkip.setVisibility(
                    View.GONE
            );

            actionRemove.setVisibility(
                    View.GONE
            );

            dividerRemove.setVisibility(
                    View.GONE
            );

        } else {

            tvSubtitle.setText(
                    "Choose what you want to do with this session."
            );
        }


        // =====================================================
        // VIEW TASK
        // =====================================================

        actionViewTask.setOnClickListener(
                view -> {

                    bottomSheetDialog.dismiss();

                    openTaskDetails(
                            item
                    );
                }
        );


        // =====================================================
        // MOVE
        // =====================================================

        actionMove.setOnClickListener(
                view -> {

                    bottomSheetDialog.dismiss();

                    startMoveSession(
                            item
                    );
                }
        );


        // =====================================================
        // COMPLETE
        // =====================================================

        actionComplete.setOnClickListener(
                view -> {

                    bottomSheetDialog.dismiss();

                    completeSession(
                            item
                    );
                }
        );


        // =====================================================
        // SKIP
        // =====================================================

        actionSkip.setOnClickListener(
                view -> {

                    bottomSheetDialog.dismiss();

                    skipSession(
                            item
                    );
                }
        );


        // =====================================================
        // REMOVE
        // =====================================================

        actionRemove.setOnClickListener(
                view -> {

                    bottomSheetDialog.dismiss();

                    showRemoveSessionConfirmation(
                            item
                    );
                }
        );


        // =====================================================
        // TRANSPARENT DEFAULT MATERIAL BACKGROUND
        // =====================================================

        bottomSheetDialog.setOnShowListener(
                dialog -> {

                    FrameLayout bottomSheet =
                            bottomSheetDialog.findViewById(
                                    com.google.android.material.R.id.design_bottom_sheet
                            );


                    if (bottomSheet != null) {

                        bottomSheet.setBackgroundColor(
                                Color.TRANSPARENT
                        );
                    }
                }
        );


        bottomSheetDialog.show();
    }


    // =========================================================
    // MOVE SESSION
    // =========================================================

    private void startMoveSession(
            SmartPlanItem item
    ) {

        if (!isValidSessionItem(
                item
        )) {

            showInvalidSessionMessage();

            return;
        }


        if (item.getStatus() != null
                && item.getStatus()
                != SmartPlanItemStatus.PLANNED) {

            Toast.makeText(
                    this,
                    "Only planned sessions can be moved.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        ZonedDateTime currentStart =
                parseSmartPlanItemDateTime(
                        item.getPlannedStart()
                );


        if (currentStart == null) {

            Toast.makeText(
                    this,
                    "Unable to read the current session time.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        showMoveDatePicker(
                item,
                currentStart
        );
    }


    // =========================================================
    // MOVE DATE PICKER
    // =========================================================

    private void showMoveDatePicker(
            SmartPlanItem item,
            ZonedDateTime currentStart
    ) {

        LocalDate currentDate =
                currentStart.toLocalDate();


        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            LocalDate selectedDate =
                                    LocalDate.of(
                                            year,
                                            month + 1,
                                            dayOfMonth
                                    );


                            showMoveTimePicker(
                                    item,
                                    currentStart,
                                    selectedDate
                            );
                        },
                        currentDate.getYear(),
                        currentDate.getMonthValue() - 1,
                        currentDate.getDayOfMonth()
                );


        applyMoveDateLimits(
                datePickerDialog
        );


        datePickerDialog.setTitle(
                "Select new date"
        );


        datePickerDialog.show();
    }


    // =========================================================
    // MOVE DATE LIMITS
    // =========================================================

    private void applyMoveDateLimits(
            DatePickerDialog datePickerDialog
    ) {

        if (datePickerDialog == null) {
            return;
        }


        LocalDate today =
                LocalDate.now();


        LocalDate minimumDate =
                today;


        LocalDate maximumDate =
                null;


        if (currentPlan != null) {

            LocalDate planStart =
                    parseLocalDate(
                            currentPlan.getPeriodStart()
                    );

            LocalDate planEnd =
                    parseLocalDate(
                            currentPlan.getPeriodEnd()
                    );


            if (planStart != null
                    && planStart.isAfter(
                    minimumDate
            )) {

                minimumDate =
                        planStart;
            }


            maximumDate =
                    planEnd;
        }


        datePickerDialog
                .getDatePicker()
                .setMinDate(
                        localDateToMillis(
                                minimumDate
                        )
                );


        if (maximumDate != null) {

            datePickerDialog
                    .getDatePicker()
                    .setMaxDate(
                            localDateToMillis(
                                    maximumDate
                            )
                    );
        }
    }


    // =========================================================
    // MOVE TIME PICKER
    // =========================================================

    private void showMoveTimePicker(
            SmartPlanItem item,
            ZonedDateTime currentStart,
            LocalDate selectedDate
    ) {

        int initialHour =
                currentStart.getHour();

        int initialMinute =
                currentStart.getMinute();


        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            if (minute % 15 != 0) {

                                Toast.makeText(
                                        this,
                                        "Please choose a time in 15-minute intervals.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            LocalTime selectedTime =
                                    LocalTime.of(
                                            hourOfDay,
                                            minute
                                    );


                            ZonedDateTime newStart =
                                    LocalDateTime.of(
                                                    selectedDate,
                                                    selectedTime
                                            )
                                            .atZone(
                                                    ZoneId.systemDefault()
                                            );


                            confirmMoveSession(
                                    item,
                                    newStart
                            );
                        },
                        initialHour,
                        initialMinute,
                        true
                );


        timePickerDialog.setTitle(
                "Select new start time"
        );


        timePickerDialog.show();
    }


    // =========================================================
    // CONFIRM MOVE
    // =========================================================

    private void confirmMoveSession(
            SmartPlanItem item,
            ZonedDateTime newStart
    ) {

        if (item == null
                || newStart == null) {

            return;
        }


        if (newStart.isBefore(
                ZonedDateTime.now()
        )) {

            Toast.makeText(
                    this,
                    "A session cannot be moved to the past.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(
                        "EEE, MMM d",
                        Locale.ENGLISH
                );


        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern(
                        "HH:mm",
                        Locale.ENGLISH
                );


        String message =
                "Move this session to "
                        + newStart.format(
                        dateFormatter
                )
                        + " at "
                        + newStart.format(
                        timeFormatter
                )
                        + "?";


        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "Move session?"
                )
                .setMessage(
                        message
                )
                .setPositiveButton(
                        "Move",
                        (dialog, which) ->
                                viewModel.moveSession(
                                        item.getId(),
                                        newStart
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // LOCAL DATE -> MILLIS
    // =========================================================

    private long localDateToMillis(
            LocalDate date
    ) {

        Calendar calendar =
                Calendar.getInstance();


        calendar.set(
                Calendar.YEAR,
                date.getYear()
        );

        calendar.set(
                Calendar.MONTH,
                date.getMonthValue() - 1
        );

        calendar.set(
                Calendar.DAY_OF_MONTH,
                date.getDayOfMonth()
        );

        calendar.set(
                Calendar.HOUR_OF_DAY,
                12
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );


        return calendar.getTimeInMillis();
    }


    // =========================================================
    // COMPLETE SESSION
    // =========================================================

    private void completeSession(
            SmartPlanItem item
    ) {

        if (!isValidSessionItem(
                item
        )) {

            showInvalidSessionMessage();

            return;
        }


        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "Complete session?"
                )
                .setMessage(
                        "Mark this Smart Plan session as completed?"
                )
                .setPositiveButton(
                        "Complete",
                        (dialog, which) ->
                                viewModel.completeSession(
                                        item.getId()
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // SKIP SESSION
    // =========================================================

    private void skipSession(
            SmartPlanItem item
    ) {

        if (!isValidSessionItem(
                item
        )) {

            showInvalidSessionMessage();

            return;
        }


        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "Skip session?"
                )
                .setMessage(
                        "This session will remain in your Smart Plan history, but it will not count as completed work."
                )
                .setPositiveButton(
                        "Skip",
                        (dialog, which) ->
                                viewModel.skipSession(
                                        item.getId()
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // REMOVE SESSION
    // =========================================================

    private void showRemoveSessionConfirmation(
            SmartPlanItem item
    ) {

        if (!isValidSessionItem(
                item
        )) {

            showInvalidSessionMessage();

            return;
        }


        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        "Remove session?"
                )
                .setMessage(
                        "This session will be removed from the Smart Plan."
                )
                .setPositiveButton(
                        "Remove",
                        (dialog, which) ->
                                viewModel.removeSession(
                                        item.getId()
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // SESSION ITEM VALIDATION
    // =========================================================

    private boolean isValidSessionItem(
            SmartPlanItem item
    ) {

        return item != null
                && item.getId() != null
                && !item.getId()
                .trim()
                .isEmpty();
    }


    private void showInvalidSessionMessage() {

        Toast.makeText(
                this,
                "Unable to update this session.",
                Toast.LENGTH_SHORT
        ).show();
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

        if (generating
                || sessionActionInProgress) {

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
                                            && !generating
                                            && !sessionActionInProgress) {

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

                                    if (!generating
                                            && !sessionActionInProgress) {

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
    // OBSERVE SESSION ACTIONS
    // =========================================================

    private void observeSessionActions() {

        viewModel
                .getActionState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }


                            switch (state.getStatus()) {

                                case LOADING:

                                    setSessionActionInProgress(
                                            true
                                    );

                                    break;


                                case SUCCESS:

                                    setSessionActionInProgress(
                                            false
                                    );


                                    SmartPlanViewModel.SessionActionResult result =
                                            state.getData();


                                    String successMessage =
                                            "Smart Plan session updated.";


                                    if (result != null
                                            && result.getActionType() != null) {

                                        switch (result.getActionType()) {

                                            case COMPLETE:

                                                successMessage =
                                                        "Session completed.";

                                                break;


                                            case SKIP:

                                                successMessage =
                                                        "Session skipped.";

                                                break;


                                            case REMOVE:

                                                successMessage =
                                                        "Session removed.";

                                                break;


                                            case MOVE:

                                                successMessage =
                                                        "Session moved.";

                                                break;
                                        }
                                    }


                                    Toast.makeText(
                                            this,
                                            successMessage,
                                            Toast.LENGTH_SHORT
                                    ).show();


                                    viewModel.refreshSmartPlan();

                                    viewModel.loadTasks();

                                    break;


                                case ERROR:

                                    setSessionActionInProgress(
                                            false
                                    );


                                    String message =
                                            state.getMessage();


                                    if (message == null
                                            || message.trim().isEmpty()) {

                                        message =
                                                "Unable to update Smart Plan session.";
                                    }


                                    Toast.makeText(
                                            this,
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    break;
                            }
                        }
                );
    }


    // =========================================================
    // SESSION ACTION STATE
    // =========================================================

    private void setSessionActionInProgress(
            boolean inProgress
    ) {

        sessionActionInProgress =
                inProgress;


        btnGenerateSmartPlan.setEnabled(
                !inProgress
                        && !generating
        );


        updateViewModeButtons();
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


        updateViewModeButtons();


        tvSmartPlanSectionTitle.setText(
                "TODAY'S PLAN"
        );


        updateDisplayedItems();
    }


    private void selectWeekView() {

        weekViewSelected =
                true;


        updateViewModeButtons();


        tvSmartPlanSectionTitle.setText(
                "WEEK PLAN"
        );


        updateDisplayedItems();
    }


    // =========================================================
    // UPDATE TODAY / WEEK BUTTON STYLE
    // =========================================================

    private void updateViewModeButtons() {

        int selectedBackground =
                ContextCompat.getColor(
                        this,
                        R.color.sp_teal_deep
                );


        int unselectedBackground =
                ContextCompat.getColor(
                        this,
                        R.color.sp_surface_soft
                );


        int selectedText =
                ContextCompat.getColor(
                        this,
                        R.color.white
                );


        int unselectedText =
                ContextCompat.getColor(
                        this,
                        R.color.sp_text_secondary
                );


        if (weekViewSelected) {

            // TODAY - UNSELECTED

            btnSmartPlanToday.setBackgroundTintList(
                    ColorStateList.valueOf(
                            unselectedBackground
                    )
            );


            btnSmartPlanToday.setTextColor(
                    unselectedText
            );


            // WEEK - SELECTED

            btnSmartPlanWeek.setBackgroundTintList(
                    ColorStateList.valueOf(
                            selectedBackground
                    )
            );


            btnSmartPlanWeek.setTextColor(
                    selectedText
            );

        } else {

            // TODAY - SELECTED

            btnSmartPlanToday.setBackgroundTintList(
                    ColorStateList.valueOf(
                            selectedBackground
                    )
            );


            btnSmartPlanToday.setTextColor(
                    selectedText
            );


            // WEEK - UNSELECTED

            btnSmartPlanWeek.setBackgroundTintList(
                    ColorStateList.valueOf(
                            unselectedBackground
                    )
            );


            btnSmartPlanWeek.setTextColor(
                    unselectedText
            );
        }


        boolean controlsEnabled =
                !generating
                        && !sessionActionInProgress;


        btnSmartPlanToday.setEnabled(
                controlsEnabled
        );

        btnSmartPlanWeek.setEnabled(
                controlsEnabled
        );


        btnSmartPlanToday.setAlpha(
                controlsEnabled
                        ? 1.0f
                        : 0.55f
        );

        btnSmartPlanWeek.setAlpha(
                controlsEnabled
                        ? 1.0f
                        : 0.55f
        );
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
                        && !sessionActionInProgress
        );


        if (generating) {

            btnGenerateSmartPlan.setText(
                    currentPlan == null
                            ? "Generating..."
                            : "Regenerating..."
            );

        } else {

            updatePlanHeader();
        }


        updateViewModeButtons();
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