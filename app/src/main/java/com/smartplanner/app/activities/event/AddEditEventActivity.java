package com.smartplanner.app.activities.event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.smartplanner.app.R;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.viewmodels.CategoriesViewModel;
import com.smartplanner.app.viewmodels.EventsViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID =
            "event_id";

    private EventsViewModel eventsViewModel;
    private CategoriesViewModel categoriesViewModel;

    private ImageButton btnBackEvent;

    private TextView tvEventFormTitle;
    private TextView tvEventFormSubtitle;

    private TextInputEditText etEventTitle;
    private TextInputEditText etEventDescription;
    private TextInputEditText etEventLocation;

    private LinearLayout layoutEventCategory;

    private ImageView ivEventFormCategoryIcon;

    private TextView tvEventCategoryValue;

    private MaterialButton btnEventStartDate;
    private MaterialButton btnEventStartTime;
    private MaterialButton btnEventEndDate;
    private MaterialButton btnEventEndTime;
    private MaterialButton btnSaveEvent;

    private MaterialSwitch switchEventImportant;

    private AutoCompleteTextView actEventReminder;

    private ProgressBar progressSaveEvent;

    private final List<Category> categories =
            new ArrayList<>();

    private String selectedCategoryId =
            null;

    private ReminderType selectedReminder =
            ReminderType.NONE;

    private Calendar startCalendar;
    private Calendar endCalendar;

    private String eventId;

    private boolean editMode =
            false;

    private boolean eventLoaded =
            false;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_add_edit_event
        );

        eventId =
                getIntent().getStringExtra(
                        EXTRA_EVENT_ID
                );

        editMode =
                eventId != null
                        && !eventId.trim().isEmpty();

        initViews();

        setupViewModels();

        setupReminderDropdown();

        setupListeners();

        observeCategories();

        observeEventDetails();

        observeEventAction();

        setupInitialMode();

        categoriesViewModel.loadCategories();

        if (editMode) {

            eventsViewModel.loadEvent(
                    eventId
            );
        }
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        btnBackEvent =
                findViewById(
                        R.id.btnBackEvent
                );

        tvEventFormTitle =
                findViewById(
                        R.id.tvEventFormTitle
                );

        tvEventFormSubtitle =
                findViewById(
                        R.id.tvEventFormSubtitle
                );

        etEventTitle =
                findViewById(
                        R.id.etEventTitle
                );

        etEventDescription =
                findViewById(
                        R.id.etEventDescription
                );

        etEventLocation =
                findViewById(
                        R.id.etEventLocation
                );

        layoutEventCategory =
                findViewById(
                        R.id.layoutEventCategory
                );

        ivEventFormCategoryIcon =
                findViewById(
                        R.id.ivEventFormCategoryIcon
                );

        tvEventCategoryValue =
                findViewById(
                        R.id.tvEventCategoryValue
                );

        btnEventStartDate =
                findViewById(
                        R.id.btnEventStartDate
                );

        btnEventStartTime =
                findViewById(
                        R.id.btnEventStartTime
                );

        btnEventEndDate =
                findViewById(
                        R.id.btnEventEndDate
                );

        btnEventEndTime =
                findViewById(
                        R.id.btnEventEndTime
                );

        switchEventImportant =
                findViewById(
                        R.id.switchEventImportant
                );

        actEventReminder =
                findViewById(
                        R.id.actEventReminder
                );

        progressSaveEvent =
                findViewById(
                        R.id.progressSaveEvent
                );

        btnSaveEvent =
                findViewById(
                        R.id.btnSaveEvent
                );
    }

    private void setupViewModels() {

        eventsViewModel =
                new ViewModelProvider(this)
                        .get(
                                EventsViewModel.class
                        );

        categoriesViewModel =
                new ViewModelProvider(this)
                        .get(
                                CategoriesViewModel.class
                        );
    }

    private void setupInitialMode() {

        startCalendar =
                Calendar.getInstance();

        startCalendar.add(
                Calendar.HOUR_OF_DAY,
                1
        );

        startCalendar.set(
                Calendar.MINUTE,
                0
        );

        startCalendar.set(
                Calendar.SECOND,
                0
        );

        startCalendar.set(
                Calendar.MILLISECOND,
                0
        );

        endCalendar =
                (Calendar) startCalendar.clone();

        endCalendar.add(
                Calendar.HOUR_OF_DAY,
                1
        );

        if (editMode) {

            tvEventFormTitle.setText(
                    "Edit Event"
            );

            tvEventFormSubtitle.setText(
                    "Update event details"
            );

            btnSaveEvent.setText(
                    "Save Changes"
            );

        } else {

            tvEventFormTitle.setText(
                    "Add Event"
            );

            tvEventFormSubtitle.setText(
                    "Create a new event"
            );

            btnSaveEvent.setText(
                    "Create Event"
            );

            updateDateTimeButtons();
        }
    }

    // =========================================================
    // REMINDER
    // =========================================================

    private void setupReminderDropdown() {

        String[] reminders = {
                "No reminder",
                "10 minutes before",
                "30 minutes before",
                "1 hour before",
                "1 day before"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        reminders
                );

        actEventReminder.setAdapter(
                adapter
        );

        actEventReminder.setText(
                "No reminder",
                false
        );

        actEventReminder.setOnItemClickListener(
                (parent, view, position, id) -> {

                    switch (position) {

                        case 1:

                            selectedReminder =
                                    ReminderType.TEN_MINUTES;

                            break;

                        case 2:

                            selectedReminder =
                                    ReminderType.THIRTY_MINUTES;

                            break;

                        case 3:

                            selectedReminder =
                                    ReminderType.ONE_HOUR;

                            break;

                        case 4:

                            selectedReminder =
                                    ReminderType.ONE_DAY;

                            break;

                        case 0:
                        default:

                            selectedReminder =
                                    ReminderType.NONE;

                            break;
                    }
                }
        );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBackEvent.setOnClickListener(
                view -> finish()
        );

        layoutEventCategory.setOnClickListener(
                view -> showCategoryDialog()
        );

        btnEventStartDate.setOnClickListener(
                view -> showDatePicker(
                        true
                )
        );

        btnEventStartTime.setOnClickListener(
                view -> showTimePicker(
                        true
                )
        );

        btnEventEndDate.setOnClickListener(
                view -> showDatePicker(
                        false
                )
        );

        btnEventEndTime.setOnClickListener(
                view -> showTimePicker(
                        false
                )
        );

        btnSaveEvent.setOnClickListener(
                view -> saveEvent()
        );
    }

    // =========================================================
    // CATEGORIES
    // =========================================================

    private void observeCategories() {

        categoriesViewModel
                .getCategoriesState()
                .observe(
                        this,
                        state -> {

                            if (state == null
                                    || state.getStatus()
                                    != UiState.Status.SUCCESS) {

                                return;
                            }

                            categories.clear();

                            if (state.getData() != null) {

                                categories.addAll(
                                        state.getData()
                                );
                            }

                            updateSelectedCategoryView();
                        }
                );
    }

    private void showCategoryDialog() {

        List<String> categoryNames =
                new ArrayList<>();

        categoryNames.add(
                "No category"
        );

        for (Category category : categories) {

            if (category != null
                    && category.getName() != null) {

                categoryNames.add(
                        category.getName()
                );
            }
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                this
        )
                .setTitle(
                        "Select category"
                )
                .setItems(
                        categoryNames.toArray(
                                new String[0]
                        ),
                        (dialog, which) -> {

                            if (which == 0) {

                                selectedCategoryId =
                                        null;

                            } else {

                                Category selected =
                                        categories.get(
                                                which - 1
                                        );

                                selectedCategoryId =
                                        selected.getId();
                            }

                            updateSelectedCategoryView();
                        }
                )
                .show();
    }

    private void updateSelectedCategoryView() {

        if (selectedCategoryId == null
                || selectedCategoryId.trim().isEmpty()) {

            tvEventCategoryValue.setText(
                    "No category"
            );

            ivEventFormCategoryIcon.setImageResource(
                    R.drawable.ic_task_category_default
            );

            return;
        }

        Category selected =
                findCategoryById(
                        selectedCategoryId
                );

        if (selected == null) {

            tvEventCategoryValue.setText(
                    "No category"
            );

            ivEventFormCategoryIcon.setImageResource(
                    R.drawable.ic_task_category_default
            );

            return;
        }

        tvEventCategoryValue.setText(
                selected.getName()
        );

        ivEventFormCategoryIcon.setImageResource(
                getCategoryIcon(
                        selected
                )
        );
    }

    private Category findCategoryById(
            String categoryId
    ) {

        for (Category category : categories) {

            if (category != null
                    && category.getId() != null
                    && category.getId().equals(
                    categoryId
            )) {

                return category;
            }
        }

        return null;
    }

    private int getCategoryIcon(
            Category category
    ) {

        if (category == null) {

            return R.drawable.ic_task_category_default;
        }

        // Prvo pokušaj preko spremljene icon vrijednosti
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

        // Ako icon vrijednost nije jedna od standardnih,
        // pokušaj prepoznati kategoriju prema nazivu
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

    // =========================================================
    // DATE PICKERS
    // =========================================================

    private void showDatePicker(
            boolean start
    ) {

        Calendar target =
                start
                        ? startCalendar
                        : endCalendar;

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            target.set(
                                    Calendar.YEAR,
                                    year
                            );

                            target.set(
                                    Calendar.MONTH,
                                    month
                            );

                            target.set(
                                    Calendar.DAY_OF_MONTH,
                                    dayOfMonth
                            );

                            if (start
                                    && endCalendar.before(startCalendar)) {

                                endCalendar =
                                        (Calendar) startCalendar.clone();

                                endCalendar.add(
                                        Calendar.HOUR_OF_DAY,
                                        1
                                );
                            }

                            updateDateTimeButtons();
                        },
                        target.get(
                                Calendar.YEAR
                        ),
                        target.get(
                                Calendar.MONTH
                        ),
                        target.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    private void showTimePicker(
            boolean start
    ) {

        Calendar target =
                start
                        ? startCalendar
                        : endCalendar;

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            target.set(
                                    Calendar.HOUR_OF_DAY,
                                    hourOfDay
                            );

                            target.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            target.set(
                                    Calendar.SECOND,
                                    0
                            );

                            target.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (start
                                    && endCalendar.before(startCalendar)) {

                                endCalendar =
                                        (Calendar) startCalendar.clone();

                                endCalendar.add(
                                        Calendar.HOUR_OF_DAY,
                                        1
                                );
                            }

                            updateDateTimeButtons();
                        },
                        target.get(
                                Calendar.HOUR_OF_DAY
                        ),
                        target.get(
                                Calendar.MINUTE
                        ),
                        true
                );

        dialog.show();
    }

    private void updateDateTimeButtons() {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.ENGLISH
                );

        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.ENGLISH
                );

        btnEventStartDate.setText(
                dateFormat.format(
                        startCalendar.getTime()
                )
        );

        btnEventStartTime.setText(
                timeFormat.format(
                        startCalendar.getTime()
                )
        );

        btnEventEndDate.setText(
                dateFormat.format(
                        endCalendar.getTime()
                )
        );

        btnEventEndTime.setText(
                timeFormat.format(
                        endCalendar.getTime()
                )
        );
    }

    // =========================================================
    // EVENT DETAILS
    // =========================================================

    private void observeEventDetails() {

        eventsViewModel
                .getEventDetailsState()
                .observe(
                        this,
                        state -> {

                            if (state == null
                                    || !editMode) {

                                return;
                            }

                            switch (state.getStatus()) {

                                case LOADING:

                                    setFormEnabled(
                                            false
                                    );

                                    break;

                                case SUCCESS:

                                    setFormEnabled(
                                            true
                                    );

                                    if (state.getData() != null
                                            && !eventLoaded) {

                                        eventLoaded =
                                                true;

                                        populateEvent(
                                                state.getData()
                                        );
                                    }

                                    break;

                                case ERROR:

                                    setFormEnabled(
                                            true
                                    );

                                    Toast.makeText(
                                            this,
                                            state.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();

                                    break;
                            }
                        }
                );
    }

    private void populateEvent(
            Event event
    ) {

        etEventTitle.setText(
                event.getTitle()
        );

        etEventDescription.setText(
                event.getDescription()
        );

        etEventLocation.setText(
                event.getLocation()
        );

        selectedCategoryId =
                event.getCategoryId();

        switchEventImportant.setChecked(
                event.isImportant()
        );

        selectedReminder =
                event.getReminderType() != null
                        ? event.getReminderType()
                        : ReminderType.NONE;

        updateReminderText();

        Calendar parsedStart =
                parseCalendar(
                        event.getStartAt()
                );

        Calendar parsedEnd =
                parseCalendar(
                        event.getEndAt()
                );

        if (parsedStart != null) {

            startCalendar =
                    parsedStart;
        }

        if (parsedEnd != null) {

            endCalendar =
                    parsedEnd;
        }

        updateDateTimeButtons();

        updateSelectedCategoryView();
    }

    private void updateReminderText() {

        switch (selectedReminder) {

            case TEN_MINUTES:

                actEventReminder.setText(
                        "10 minutes before",
                        false
                );

                break;

            case THIRTY_MINUTES:

                actEventReminder.setText(
                        "30 minutes before",
                        false
                );

                break;

            case ONE_HOUR:

                actEventReminder.setText(
                        "1 hour before",
                        false
                );

                break;

            case ONE_DAY:

                actEventReminder.setText(
                        "1 day before",
                        false
                );

                break;

            case NONE:
            default:

                actEventReminder.setText(
                        "No reminder",
                        false
                );

                break;
        }
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void saveEvent() {

        String title =
                getText(
                        etEventTitle
                );

        String description =
                getText(
                        etEventDescription
                );

        String location =
                getText(
                        etEventLocation
                );

        if (title.isEmpty()) {

            etEventTitle.setError(
                    "Title is required."
            );

            etEventTitle.requestFocus();

            return;
        }

        if (title.length() > 150) {

            etEventTitle.setError(
                    "Title is too long."
            );

            etEventTitle.requestFocus();

            return;
        }

        if (startCalendar == null
                || endCalendar == null) {

            Toast.makeText(
                    this,
                    "Start and end date are required.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!endCalendar.after(
                startCalendar
        )) {

            Toast.makeText(
                    this,
                    "End time must be after start time.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String startAt =
                formatApiDate(
                        startCalendar.getTime()
                );

        String endAt =
                formatApiDate(
                        endCalendar.getTime()
                );

        if (editMode) {

            eventsViewModel.updateEvent(
                    eventId,
                    selectedCategoryId,
                    title,
                    description,
                    startAt,
                    endAt,
                    location,
                    switchEventImportant.isChecked(),
                    selectedReminder
            );

        } else {

            eventsViewModel.createEvent(
                    selectedCategoryId,
                    title,
                    description,
                    startAt,
                    endAt,
                    location,
                    switchEventImportant.isChecked(),
                    selectedReminder
            );
        }
    }

    private void observeEventAction() {

        eventsViewModel
                .getEventActionState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }

                            switch (state.getStatus()) {

                                case LOADING:

                                    progressSaveEvent.setVisibility(
                                            View.VISIBLE
                                    );

                                    setFormEnabled(
                                            false
                                    );

                                    break;

                                case SUCCESS:

                                    progressSaveEvent.setVisibility(
                                            View.GONE
                                    );

                                    setFormEnabled(
                                            true
                                    );

                                    Toast.makeText(
                                            this,
                                            editMode
                                                    ? "Event updated."
                                                    : "Event created.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    setResult(
                                            RESULT_OK
                                    );

                                    finish();

                                    break;

                                case ERROR:

                                    progressSaveEvent.setVisibility(
                                            View.GONE
                                    );

                                    setFormEnabled(
                                            true
                                    );

                                    String message =
                                            state.getMessage();

                                    if (message == null
                                            || message.trim().isEmpty()) {

                                        message =
                                                "Unable to save event.";
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

    private void setFormEnabled(
            boolean enabled
    ) {

        etEventTitle.setEnabled(
                enabled
        );

        etEventDescription.setEnabled(
                enabled
        );

        etEventLocation.setEnabled(
                enabled
        );

        layoutEventCategory.setEnabled(
                enabled
        );

        btnEventStartDate.setEnabled(
                enabled
        );

        btnEventStartTime.setEnabled(
                enabled
        );

        btnEventEndDate.setEnabled(
                enabled
        );

        btnEventEndTime.setEnabled(
                enabled
        );

        switchEventImportant.setEnabled(
                enabled
        );

        actEventReminder.setEnabled(
                enabled
        );

        btnSaveEvent.setEnabled(
                enabled
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

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

    private String formatApiDate(
            Date date
    ) {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ssXXX",
                        Locale.US
                );

        return format.format(
                date
        );
    }

    private Calendar parseCalendar(
            String value
    ) {

        Date date =
                parseSupabaseDate(
                        value
                );

        if (date == null) {

            return null;
        }

        Calendar calendar =
                Calendar.getInstance();

        calendar.setTime(
                date
        );

        return calendar;
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