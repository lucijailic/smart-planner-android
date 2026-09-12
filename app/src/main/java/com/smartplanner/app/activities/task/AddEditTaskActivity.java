package com.smartplanner.app.activities.task;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.notifications.NotificationSettingsManager;
import com.smartplanner.app.notifications.TaskReminderManager;
import com.smartplanner.app.viewmodels.CategoriesViewModel;
import com.smartplanner.app.viewmodels.TasksViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID =
            "task_id";

    private TextInputLayout tilTaskTitle;
    private TextInputLayout tilTaskCategory;
    private TextInputLayout tilTaskReminder;
    private TextInputLayout tilTaskDeadline;

    private TextInputEditText etTaskTitle;
    private TextInputEditText etTaskDescription;
    private TextInputEditText etTaskDeadline;

    private AutoCompleteTextView actTaskCategory;
    private AutoCompleteTextView actTaskDuration;
    private AutoCompleteTextView actTaskReminder;

    private MaterialButtonToggleGroup groupTaskPriority;

    private MaterialButton btnPriorityLow;
    private MaterialButton btnPriorityMedium;
    private MaterialButton btnPriorityHigh;

    private MaterialSwitch switchTaskImportant;

    private MaterialButton btnSaveTask;
    private MaterialButton btnClearTaskDeadline;

    private TextView tvTaskFormTitle;
    private TextView tvTaskFormSubtitle;

    private CategoriesViewModel categoriesViewModel;
    private TasksViewModel tasksViewModel;

    private final List<Category> categories =
            new ArrayList<>();

    private String taskId;

    private boolean editMode =
            false;

    private Task currentTask;

    private boolean categoriesLoaded =
            false;

    private String selectedCategoryId =
            null;

    private TaskPriority selectedPriority =
            TaskPriority.MEDIUM;

    private ReminderType selectedReminderType =
            ReminderType.NONE;

    /*
     * Default reminder dohvaćen iz Notification Settingsa.
     * Koristi se samo za novi Task.
     */
    private ReminderType defaultTaskReminderType =
            ReminderType.ONE_DAY;

    /*
     * Pratimo je li korisnik ručno promijenio reminder.
     * Time sprječavamo da default prepiše korisnikov izbor.
     */
    private boolean reminderManuallySelected =
            false;

    private Integer selectedEstimatedDuration =
            null;

    private Calendar selectedDeadline =
            null;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_add_edit_task
        );

        taskId =
                getIntent().getStringExtra(
                        EXTRA_TASK_ID
                );

        editMode =
                taskId != null
                        && !taskId.trim().isEmpty();

        initViews();

        setupViewModels();

        setupMode();

        setupGeneralListeners();
        setupCategoryListeners();
        setupPriorityListener();
        setupDurationDropdown();
        setupReminderDropdown();

        setupDefaultReminder();

        setupDeadlinePicker();

        updatePriorityAppearance();
        updateDeadlineClearButton();
        updateReminderAvailability();

        observeCategories();
        observeTaskDetails();
        observeTaskAction();

        categoriesViewModel.loadCategories();

        if (editMode) {

            tasksViewModel.loadTask(
                    taskId
            );
        }
    }

    // =========================================================
    // VIEWS
    // =========================================================

    private void initViews() {

        tilTaskTitle =
                findViewById(
                        R.id.tilTaskTitle
                );

        tilTaskCategory =
                findViewById(
                        R.id.tilTaskCategory
                );

        tilTaskReminder =
                findViewById(
                        R.id.tilTaskReminder
                );

        tilTaskDeadline =
                findViewById(
                        R.id.tilTaskDeadline
                );

        etTaskTitle =
                findViewById(
                        R.id.etTaskTitle
                );

        etTaskDescription =
                findViewById(
                        R.id.etTaskDescription
                );

        etTaskDeadline =
                findViewById(
                        R.id.etTaskDeadline
                );

        actTaskCategory =
                findViewById(
                        R.id.actTaskCategory
                );

        actTaskDuration =
                findViewById(
                        R.id.actTaskDuration
                );

        actTaskReminder =
                findViewById(
                        R.id.actTaskReminder
                );

        groupTaskPriority =
                findViewById(
                        R.id.groupTaskPriority
                );

        btnPriorityLow =
                findViewById(
                        R.id.btnPriorityLow
                );

        btnPriorityMedium =
                findViewById(
                        R.id.btnPriorityMedium
                );

        btnPriorityHigh =
                findViewById(
                        R.id.btnPriorityHigh
                );

        switchTaskImportant =
                findViewById(
                        R.id.switchTaskImportant
                );

        btnSaveTask =
                findViewById(
                        R.id.btnSaveTask
                );

        btnClearTaskDeadline =
                findViewById(
                        R.id.btnClearTaskDeadline
                );

        tvTaskFormTitle =
                findViewById(
                        R.id.tvTaskFormTitle
                );

        tvTaskFormSubtitle =
                findViewById(
                        R.id.tvTaskFormSubtitle
                );
    }

    // =========================================================
    // VIEW MODELS
    // =========================================================

    private void setupViewModels() {

        categoriesViewModel =
                new ViewModelProvider(this)
                        .get(
                                CategoriesViewModel.class
                        );

        tasksViewModel =
                new ViewModelProvider(this)
                        .get(
                                TasksViewModel.class
                        );
    }

    // =========================================================
    // MODE
    // =========================================================

    private void setupMode() {

        if (editMode) {

            tvTaskFormTitle.setText(
                    "Edit Task"
            );

            tvTaskFormSubtitle.setText(
                    "Update task information"
            );

            btnSaveTask.setText(
                    "Save Changes"
            );

        } else {

            tvTaskFormTitle.setText(
                    R.string.add_task_title
            );

            tvTaskFormSubtitle.setText(
                    R.string.add_task_subtitle
            );

            btnSaveTask.setText(
                    R.string.save_task
            );
        }
    }

    // =========================================================
    // DEFAULT REMINDER
    // =========================================================

    private void setupDefaultReminder() {

        if (editMode) {
            return;
        }

        defaultTaskReminderType =
                NotificationSettingsManager
                        .getDefaultTaskReminder(
                                this
                        );

        if (defaultTaskReminderType == null) {

            defaultTaskReminderType =
                    ReminderType.NONE;
        }
    }

    // =========================================================
    // GENERAL LISTENERS
    // =========================================================

    private void setupGeneralListeners() {

        View btnBack =
                findViewById(
                        R.id.btnTaskBack
                );

        btnBack.setOnClickListener(
                view -> finish()
        );

        btnSaveTask.setOnClickListener(
                view -> attemptSaveTask()
        );

        btnClearTaskDeadline.setOnClickListener(
                view -> clearDeadline()
        );
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private void setupCategoryListeners() {

        actTaskCategory.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position == 0) {

                        selectedCategoryId =
                                null;

                        updateCategoryIcon(
                                null
                        );

                        return;
                    }

                    int categoryPosition =
                            position - 1;

                    if (categoryPosition >= 0
                            && categoryPosition < categories.size()) {

                        Category category =
                                categories.get(
                                        categoryPosition
                                );

                        selectedCategoryId =
                                category.getId();

                        updateCategoryIcon(
                                category
                        );
                    }
                }
        );

        actTaskCategory.setOnClickListener(
                view ->
                        actTaskCategory.showDropDown()
        );
    }

    private void updateCategoryIcon(
            Category category
    ) {

        int drawableResource =
                getCategoryIconResource(
                        category
                );

        tilTaskCategory.setStartIconDrawable(
                drawableResource
        );

        int iconColor =
                getCategoryColor(
                        category
                );

        tilTaskCategory.setStartIconTintList(
                ColorStateList.valueOf(
                        iconColor
                )
        );
    }

    private int getCategoryIconResource(
            Category category
    ) {

        if (category == null) {

            return R.drawable.ic_task_category_default;
        }

        String icon =
                category.getIcon();

        if (icon != null
                && !icon.trim().isEmpty()) {

            String normalizedIcon =
                    icon.trim()
                            .toLowerCase(
                                    Locale.US
                            );

            switch (normalizedIcon) {

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

        if (name == null
                || name.trim().isEmpty()) {

            return R.drawable.ic_task_category_default;
        }

        String normalizedName =
                name.trim()
                        .toLowerCase(
                                Locale.US
                        );

        if (normalizedName.contains("work")
                || normalizedName.contains("job")
                || normalizedName.contains("business")) {

            return R.drawable.ic_task_category_work;
        }

        if (normalizedName.contains("personal")
                || normalizedName.contains("home")) {

            return R.drawable.ic_task_category_personal;
        }

        if (normalizedName.contains("health")
                || normalizedName.contains("fitness")
                || normalizedName.contains("gym")
                || normalizedName.contains("sport")) {

            return R.drawable.ic_task_category_health;
        }

        if (normalizedName.contains("study")
                || normalizedName.contains("university")
                || normalizedName.contains("school")
                || normalizedName.contains("college")) {

            return R.drawable.ic_task_category_study;
        }

        if (normalizedName.contains("shopping")
                || normalizedName.contains("shop")
                || normalizedName.contains("groceries")) {

            return R.drawable.ic_task_category_shopping;
        }

        return R.drawable.ic_task_category_default;
    }

    private int getCategoryColor(
            Category category
    ) {

        if (category == null
                || category.getColor() == null
                || category.getColor()
                .trim()
                .isEmpty()) {

            return Color.parseColor(
                    "#7A8B9D"
            );
        }

        try {

            String color =
                    category.getColor()
                            .trim();

            if (!color.startsWith("#")) {

                color =
                        "#" + color;
            }

            return Color.parseColor(
                    color
            );

        } catch (IllegalArgumentException exception) {

            return Color.parseColor(
                    "#7A8B9D"
            );
        }
    }

    // =========================================================
    // PRIORITY
    // =========================================================

    private void setupPriorityListener() {

        groupTaskPriority.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {

                    if (!isChecked) {
                        return;
                    }

                    if (checkedId
                            == R.id.btnPriorityLow) {

                        selectedPriority =
                                TaskPriority.LOW;

                    } else if (
                            checkedId
                                    == R.id.btnPriorityHigh
                    ) {

                        selectedPriority =
                                TaskPriority.HIGH;

                    } else {

                        selectedPriority =
                                TaskPriority.MEDIUM;
                    }

                    updatePriorityAppearance();
                }
        );
    }

    private void updatePriorityAppearance() {

        int textColor =
                Color.parseColor(
                        "#183B5B"
                );

        int selectedBackground;

        switch (selectedPriority) {

            case LOW:

                selectedBackground =
                        Color.parseColor(
                                "#DDF6E8"
                        );

                break;

            case HIGH:

                selectedBackground =
                        Color.parseColor(
                                "#FFE0E3"
                        );

                break;

            case MEDIUM:
            default:

                selectedBackground =
                        Color.parseColor(
                                "#FFF0D5"
                        );

                break;
        }

        int transparent =
                Color.TRANSPARENT;

        btnPriorityLow.setBackgroundTintList(
                ColorStateList.valueOf(
                        selectedPriority == TaskPriority.LOW
                                ? selectedBackground
                                : transparent
                )
        );

        btnPriorityMedium.setBackgroundTintList(
                ColorStateList.valueOf(
                        selectedPriority == TaskPriority.MEDIUM
                                ? selectedBackground
                                : transparent
                )
        );

        btnPriorityHigh.setBackgroundTintList(
                ColorStateList.valueOf(
                        selectedPriority == TaskPriority.HIGH
                                ? selectedBackground
                                : transparent
                )
        );

        btnPriorityLow.setTextColor(
                textColor
        );

        btnPriorityMedium.setTextColor(
                textColor
        );

        btnPriorityHigh.setTextColor(
                textColor
        );
    }

    // =========================================================
    // DURATION
    // =========================================================

    private void setupDurationDropdown() {

        List<String> durationLabels =
                getDurationLabels();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        durationLabels
                );

        actTaskDuration.setAdapter(
                adapter
        );

        actTaskDuration.setText(
                durationLabels.get(0),
                false
        );

        actTaskDuration.setOnClickListener(
                view ->
                        actTaskDuration.showDropDown()
        );

        actTaskDuration.setOnItemClickListener(
                (parent, view, position, id) -> {

                    switch (position) {

                        case 1:
                            selectedEstimatedDuration = 15;
                            break;

                        case 2:
                            selectedEstimatedDuration = 30;
                            break;

                        case 3:
                            selectedEstimatedDuration = 45;
                            break;

                        case 4:
                            selectedEstimatedDuration = 60;
                            break;

                        case 5:
                            selectedEstimatedDuration = 90;
                            break;

                        case 6:
                            selectedEstimatedDuration = 120;
                            break;

                        case 7:
                            selectedEstimatedDuration = 180;
                            break;

                        case 0:
                        default:
                            selectedEstimatedDuration = null;
                            break;
                    }
                }
        );
    }

    private List<String> getDurationLabels() {

        List<String> durationLabels =
                new ArrayList<>();

        durationLabels.add(
                getString(
                        R.string.task_duration_not_set
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_15
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_30
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_45
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_60
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_90
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_120
                )
        );

        durationLabels.add(
                getString(
                        R.string.task_duration_180
                )
        );

        return durationLabels;
    }

    // =========================================================
    // REMINDER
    // =========================================================

    private void setupReminderDropdown() {

        List<String> reminderLabels =
                getReminderLabels();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        reminderLabels
                );

        actTaskReminder.setAdapter(
                adapter
        );

        actTaskReminder.setText(
                reminderLabels.get(0),
                false
        );

        actTaskReminder.setOnTouchListener(
                (view, event) -> {

                    if (selectedDeadline == null) {

                        actTaskReminder.dismissDropDown();

                        return true;
                    }

                    return false;
                }
        );

        actTaskReminder.setOnClickListener(
                view -> {

                    if (selectedDeadline == null) {

                        actTaskReminder.dismissDropDown();

                        return;
                    }

                    actTaskReminder.showDropDown();
                }
        );

        actTaskReminder.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (selectedDeadline == null) {

                        selectedReminderType =
                                ReminderType.NONE;

                        actTaskReminder.setText(
                                reminderLabels.get(0),
                                false
                        );

                        actTaskReminder.dismissDropDown();

                        return;
                    }

                    reminderManuallySelected =
                            true;

                    switch (position) {

                        case 1:

                            selectedReminderType =
                                    ReminderType.TEN_MINUTES;

                            break;

                        case 2:

                            selectedReminderType =
                                    ReminderType.THIRTY_MINUTES;

                            break;

                        case 3:

                            selectedReminderType =
                                    ReminderType.ONE_HOUR;

                            break;

                        case 4:

                            selectedReminderType =
                                    ReminderType.ONE_DAY;

                            break;

                        case 0:
                        default:

                            selectedReminderType =
                                    ReminderType.NONE;

                            break;
                    }
                }
        );
    }

    private List<String> getReminderLabels() {

        List<String> reminderLabels =
                new ArrayList<>();

        reminderLabels.add(
                "No reminder"
        );

        reminderLabels.add(
                "10 minutes before"
        );

        reminderLabels.add(
                "30 minutes before"
        );

        reminderLabels.add(
                "1 hour before"
        );

        reminderLabels.add(
                "1 day before"
        );

        return reminderLabels;
    }

    // =========================================================
    // DEADLINE
    // =========================================================

    private void setupDeadlinePicker() {

        etTaskDeadline.setOnClickListener(
                view -> showDatePicker()
        );

        tilTaskDeadline.setEndIconOnClickListener(
                view -> showDatePicker()
        );
    }

    private void showDatePicker() {

        Calendar initialCalendar =
                selectedDeadline != null
                        ? selectedDeadline
                        : Calendar.getInstance();

        int year =
                initialCalendar.get(
                        Calendar.YEAR
                );

        int month =
                initialCalendar.get(
                        Calendar.MONTH
                );

        int day =
                initialCalendar.get(
                        Calendar.DAY_OF_MONTH
                );

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (datePicker,
                         selectedYear,
                         selectedMonth,
                         selectedDay) -> {

                            Calendar calendar =
                                    Calendar.getInstance();

                            calendar.set(
                                    Calendar.YEAR,
                                    selectedYear
                            );

                            calendar.set(
                                    Calendar.MONTH,
                                    selectedMonth
                            );

                            calendar.set(
                                    Calendar.DAY_OF_MONTH,
                                    selectedDay
                            );

                            showTimePicker(
                                    calendar
                            );
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }

    private void showTimePicker(
            Calendar calendar
    ) {

        int hour =
                selectedDeadline != null
                        ? selectedDeadline.get(
                        Calendar.HOUR_OF_DAY
                )
                        : 12;

        int minute =
                selectedDeadline != null
                        ? selectedDeadline.get(
                        Calendar.MINUTE
                )
                        : 0;

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        (timePicker,
                         selectedHour,
                         selectedMinute) -> {

                            calendar.set(
                                    Calendar.HOUR_OF_DAY,
                                    selectedHour
                            );

                            calendar.set(
                                    Calendar.MINUTE,
                                    selectedMinute
                            );

                            calendar.set(
                                    Calendar.SECOND,
                                    0
                            );

                            calendar.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            selectedDeadline =
                                    calendar;

                            updateDeadlineDisplay();

                            updateDeadlineClearButton();

                            updateReminderAvailability();
                        },
                        hour,
                        minute,
                        true
                );

        timePickerDialog.show();
    }

    private void clearDeadline() {

        selectedDeadline =
                null;

        selectedReminderType =
                ReminderType.NONE;

        /*
         * Ako korisnik ponovno doda deadline,
         * default reminder se ponovno može primijeniti.
         */
        if (!editMode) {

            reminderManuallySelected =
                    false;
        }

        etTaskDeadline.setText(
                ""
        );

        actTaskReminder.dismissDropDown();

        actTaskReminder.setText(
                getReminderLabels().get(0),
                false
        );

        updateDeadlineClearButton();

        updateReminderAvailability();
    }

    private void updateDeadlineDisplay() {

        if (selectedDeadline == null) {

            etTaskDeadline.setText(
                    ""
            );

            return;
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM yyyy, HH:mm",
                        Locale.ENGLISH
                );

        etTaskDeadline.setText(
                formatter.format(
                        selectedDeadline.getTime()
                )
        );
    }

    private void updateDeadlineClearButton() {

        boolean hasDeadline =
                selectedDeadline != null;

        btnClearTaskDeadline.setVisibility(
                hasDeadline
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        btnClearTaskDeadline.setEnabled(
                hasDeadline
        );

        btnClearTaskDeadline.setClickable(
                hasDeadline
        );

        btnClearTaskDeadline.setAlpha(
                hasDeadline
                        ? 1.0f
                        : 0.35f
        );
    }

    private void updateReminderAvailability() {

        boolean hasDeadline =
                selectedDeadline != null;

        if (hasDeadline) {

            /*
             * Za novi Task primjenjujemo default reminder
             * samo dok korisnik nije ručno odabrao drugi.
             */
            if (!editMode
                    && !reminderManuallySelected) {

                selectedReminderType =
                        defaultTaskReminderType != null
                                ? defaultTaskReminderType
                                : ReminderType.NONE;

                populateReminder();
            }

            tilTaskReminder.setEnabled(
                    true
            );

            actTaskReminder.setEnabled(
                    true
            );

            actTaskReminder.setClickable(
                    true
            );

            actTaskReminder.setAlpha(
                    1.0f
            );

        } else {

            selectedReminderType =
                    ReminderType.NONE;

            List<String> reminderLabels =
                    getReminderLabels();

            actTaskReminder.dismissDropDown();

            actTaskReminder.setText(
                    reminderLabels.get(0),
                    false
            );

            tilTaskReminder.setEnabled(
                    false
            );

            actTaskReminder.setEnabled(
                    false
            );

            actTaskReminder.setClickable(
                    false
            );

            actTaskReminder.setAlpha(
                    0.55f
            );
        }
    }

    // =========================================================
    // CATEGORIES OBSERVER
    // =========================================================

    private void observeCategories() {

        categoriesViewModel
                .getCategoriesState()
                .observe(
                        this,
                        this::renderCategoriesState
                );
    }

    private void renderCategoriesState(
            UiState<List<Category>> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                actTaskCategory.setEnabled(
                        false
                );

                break;

            case SUCCESS:

                actTaskCategory.setEnabled(
                        true
                );

                setupCategoryDropdown(
                        state.getData()
                );

                categoriesLoaded =
                        true;

                populateCategoryIfPossible();

                break;

            case ERROR:

                actTaskCategory.setEnabled(
                        true
                );

                showCategoryError(
                        state.getMessage()
                );

                break;
        }
    }

    private void setupCategoryDropdown(
            List<Category> result
    ) {

        categories.clear();

        if (result != null) {

            categories.addAll(
                    result
            );
        }

        List<String> categoryNames =
                new ArrayList<>();

        categoryNames.add(
                "No category"
        );

        for (Category category : categories) {

            categoryNames.add(
                    category.getName()
            );
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );

        actTaskCategory.setAdapter(
                adapter
        );

        if (!editMode
                && selectedCategoryId == null) {

            actTaskCategory.setText(
                    "No category",
                    false
            );

            updateCategoryIcon(
                    null
            );
        }
    }

    private void showCategoryError(
            String message
    ) {

        String errorMessage =
                message;

        if (errorMessage == null
                || errorMessage.trim().isEmpty()) {

            errorMessage =
                    "Unable to load categories.";
        }

        Toast.makeText(
                this,
                errorMessage,
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================================================
    // TASK OBSERVER
    // =========================================================

    private void observeTaskDetails() {

        tasksViewModel
                .getTaskDetailsState()
                .observe(
                        this,
                        this::renderTaskDetailsState
                );
    }

    private void renderTaskDetailsState(
            UiState<Task> state
    ) {

        if (!editMode
                || state == null) {

            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                setFormEnabled(
                        false
                );

                break;

            case SUCCESS:

                currentTask =
                        state.getData();

                if (currentTask == null) {

                    Toast.makeText(
                            this,
                            "Task not found.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();

                    return;
                }

                populateTaskForm(
                        currentTask
                );

                setFormEnabled(
                        true
                );

                updateDeadlineClearButton();

                updateReminderAvailability();

                break;

            case ERROR:

                setFormEnabled(
                        true
                );

                updateDeadlineClearButton();

                updateReminderAvailability();

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Unable to load task.";
                }

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void populateTaskForm(
            Task task
    ) {

        etTaskTitle.setText(
                task.getTitle()
        );

        etTaskDescription.setText(
                task.getDescription() != null
                        ? task.getDescription()
                        : ""
        );

        selectedCategoryId =
                task.getCategoryId();

        selectedPriority =
                task.getPriority() != null
                        ? task.getPriority()
                        : TaskPriority.MEDIUM;

        selectedEstimatedDuration =
                task.getEstimatedDuration();

        selectedReminderType =
                task.getReminderType() != null
                        ? task.getReminderType()
                        : ReminderType.NONE;

        switchTaskImportant.setChecked(
                task.isImportant()
        );

        populatePriority();

        populateDuration();

        populateDeadline(
                task.getDeadline()
        );

        updateDeadlineClearButton();

        updateReminderAvailability();

        if (selectedDeadline != null) {

            populateReminder();
        }

        populateCategoryIfPossible();
    }

    private void populatePriority() {

        switch (selectedPriority) {

            case LOW:

                groupTaskPriority.check(
                        R.id.btnPriorityLow
                );

                break;

            case HIGH:

                groupTaskPriority.check(
                        R.id.btnPriorityHigh
                );

                break;

            case MEDIUM:
            default:

                groupTaskPriority.check(
                        R.id.btnPriorityMedium
                );

                break;
        }

        updatePriorityAppearance();
    }

    private void populateDuration() {

        List<String> labels =
                getDurationLabels();

        int position =
                0;

        if (selectedEstimatedDuration != null) {

            switch (selectedEstimatedDuration) {

                case 15:
                    position = 1;
                    break;

                case 30:
                    position = 2;
                    break;

                case 45:
                    position = 3;
                    break;

                case 60:
                    position = 4;
                    break;

                case 90:
                    position = 5;
                    break;

                case 120:
                    position = 6;
                    break;

                case 180:
                    position = 7;
                    break;

                default:
                    position = 0;
                    break;
            }
        }

        actTaskDuration.setText(
                labels.get(position),
                false
        );
    }

    private void populateReminder() {

        List<String> labels =
                getReminderLabels();

        int position;

        switch (selectedReminderType) {

            case TEN_MINUTES:
                position = 1;
                break;

            case THIRTY_MINUTES:
                position = 2;
                break;

            case ONE_HOUR:
                position = 3;
                break;

            case ONE_DAY:
                position = 4;
                break;

            case NONE:
            default:
                position = 0;
                break;
        }

        actTaskReminder.setText(
                labels.get(position),
                false
        );
    }

    private void populateDeadline(
            String deadline
    ) {

        if (deadline == null
                || deadline.trim().isEmpty()) {

            selectedDeadline =
                    null;

            updateDeadlineDisplay();

            return;
        }

        Date parsedDate =
                parseSupabaseDate(
                        deadline
                );

        if (parsedDate == null) {

            selectedDeadline =
                    null;

            etTaskDeadline.setText(
                    deadline
            );

            return;
        }

        selectedDeadline =
                Calendar.getInstance();

        selectedDeadline.setTime(
                parsedDate
        );

        updateDeadlineDisplay();
    }

    private void populateCategoryIfPossible() {

        if (!editMode
                || currentTask == null
                || !categoriesLoaded) {

            return;
        }

        if (selectedCategoryId == null
                || selectedCategoryId.trim().isEmpty()) {

            actTaskCategory.setText(
                    "No category",
                    false
            );

            updateCategoryIcon(
                    null
            );

            return;
        }

        for (Category category : categories) {

            if (category == null) {
                continue;
            }

            if (selectedCategoryId.equals(
                    category.getId()
            )) {

                actTaskCategory.setText(
                        category.getName(),
                        false
                );

                updateCategoryIcon(
                        category
                );

                return;
            }
        }

        selectedCategoryId =
                null;

        actTaskCategory.setText(
                "No category",
                false
        );

        updateCategoryIcon(
                null
        );
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void attemptSaveTask() {

        clearErrors();

        String title =
                getText(
                        etTaskTitle
                );

        String description =
                getText(
                        etTaskDescription
                );

        if (title.isEmpty()) {

            tilTaskTitle.setError(
                    getString(
                            R.string.task_title_required
                    )
            );

            etTaskTitle.requestFocus();

            return;
        }

        if (title.length() > 150) {

            tilTaskTitle.setError(
                    getString(
                            R.string.task_title_too_long
                    )
            );

            etTaskTitle.requestFocus();

            return;
        }

        if (description.length() > 500) {

            Toast.makeText(
                    this,
                    "Description can contain a maximum of 500 characters.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ReminderType reminderType;

        if (selectedDeadline == null) {

            reminderType =
                    ReminderType.NONE;

        } else {

            reminderType =
                    selectedReminderType != null
                            ? selectedReminderType
                            : ReminderType.NONE;
        }

        setSaving(
                true
        );

        if (editMode) {

            if (currentTask == null) {

                setSaving(
                        false
                );

                Toast.makeText(
                        this,
                        "Task is not ready yet.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            TaskStatus status =
                    currentTask.getStatus() != null
                            ? currentTask.getStatus()
                            : TaskStatus.TO_DO;

            tasksViewModel.updateTask(
                    taskId,
                    selectedCategoryId,
                    title,
                    description.isEmpty()
                            ? null
                            : description,
                    selectedPriority,
                    status,
                    getDeadlineForApi(),
                    selectedEstimatedDuration,
                    switchTaskImportant.isChecked(),
                    reminderType,
                    currentTask.getCompletedAt()
            );

        } else {

            tasksViewModel.createTask(
                    selectedCategoryId,
                    title,
                    description.isEmpty()
                            ? null
                            : description,
                    selectedPriority,
                    getDeadlineForApi(),
                    selectedEstimatedDuration,
                    switchTaskImportant.isChecked(),
                    reminderType
            );
        }
    }

    private void observeTaskAction() {

        tasksViewModel
                .getTaskActionState()
                .observe(
                        this,
                        this::renderTaskActionState
                );
    }

    private void renderTaskActionState(
            UiState<Task> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                setSaving(
                        true
                );

                break;

            case SUCCESS:

                setSaving(
                        false
                );

                Task savedTask =
                        state.getData();

                if (savedTask != null) {

                    TaskReminderManager.updateTaskReminder(
                            this,
                            savedTask
                    );
                }

                Toast.makeText(
                        this,
                        editMode
                                ? "Task updated."
                                : getString(
                                R.string.task_saved
                        ),
                        Toast.LENGTH_SHORT
                ).show();

                setResult(
                        RESULT_OK
                );

                finish();

                break;

            case ERROR:

                setSaving(
                        false
                );

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            getString(
                                    R.string.task_save_error
                            );
                }

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    // =========================================================
    // FORM STATE
    // =========================================================

    private void setSaving(
            boolean saving
    ) {

        btnSaveTask.setEnabled(
                !saving
        );

        if (saving) {

            btnSaveTask.setText(
                    "Saving..."
            );

        } else {

            btnSaveTask.setText(
                    editMode
                            ? "Save Changes"
                            : getString(
                            R.string.save_task
                    )
            );
        }
    }

    private void setFormEnabled(
            boolean enabled
    ) {

        etTaskTitle.setEnabled(
                enabled
        );

        etTaskDescription.setEnabled(
                enabled
        );

        etTaskDeadline.setEnabled(
                enabled
        );

        actTaskCategory.setEnabled(
                enabled
        );

        actTaskDuration.setEnabled(
                enabled
        );

        groupTaskPriority.setEnabled(
                enabled
        );

        btnPriorityLow.setEnabled(
                enabled
        );

        btnPriorityMedium.setEnabled(
                enabled
        );

        btnPriorityHigh.setEnabled(
                enabled
        );

        switchTaskImportant.setEnabled(
                enabled
        );

        btnSaveTask.setEnabled(
                enabled
        );

        if (!enabled) {

            btnClearTaskDeadline.setEnabled(
                    false
            );

            btnClearTaskDeadline.setClickable(
                    false
            );

            tilTaskReminder.setEnabled(
                    false
            );

            actTaskReminder.setEnabled(
                    false
            );

        } else {

            updateDeadlineClearButton();

            updateReminderAvailability();
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void clearErrors() {

        tilTaskTitle.setError(
                null
        );
    }

    private String getText(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }

    private String getDeadlineForApi() {

        if (selectedDeadline == null) {
            return null;
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ssXXX",
                        Locale.US
                );

        formatter.setTimeZone(
                TimeZone.getDefault()
        );

        return formatter.format(
                selectedDeadline.getTime()
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
}