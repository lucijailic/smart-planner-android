package com.smartplanner.app.activities.event;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartplanner.app.R;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.viewmodels.CategoriesViewModel;
import com.smartplanner.app.viewmodels.EventsViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    // =========================================================
    // VIEW MODELS
    // =========================================================

    private EventsViewModel eventsViewModel;
    private CategoriesViewModel categoriesViewModel;

    // =========================================================
    // DATA
    // =========================================================

    private String eventId;
    private Event currentEvent;

    private final Map<String, Category> categoryMap =
            new HashMap<>();

    // =========================================================
    // VIEWS
    // =========================================================

    private MaterialButton btnBackEventDetails;

    private ProgressBar progressEventDetails;

    private LinearLayout layoutEventDetailsError;
    private LinearLayout layoutEventDetailsLocation;
    private LinearLayout layoutEventDetailsDescription;

    private NestedScrollView scrollEventDetails;

    private TextView tvEventDetailsError;
    private TextView tvEventDetailsImportant;
    private TextView tvEventDetailsTitle;
    private TextView tvEventDetailsCategory;
    private TextView tvEventDetailsStatus;
    private TextView tvEventDetailsStart;
    private TextView tvEventDetailsEnd;
    private TextView tvEventDetailsLocation;
    private TextView tvEventDetailsReminder;
    private TextView tvEventDetailsDescription;

    private FrameLayout layoutEventCategoryIcon;
    private ImageView ivEventDetailsCategoryIcon;

    private MaterialButton btnRetryEventDetails;
    private MaterialButton btnDeleteEvent;
    private MaterialButton btnEditEvent;

    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_event_details
        );

        eventId =
                getIntent().getStringExtra(
                        EXTRA_EVENT_ID
                );

        if (eventId == null
                || eventId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid event.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        initViews();
        setupViewModels();
        setupListeners();

        observeEvent();
        observeCategories();
        observeImportantAction();
        observeDeleteAction();

        categoriesViewModel.loadCategories();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (eventsViewModel != null
                && eventId != null
                && !eventId.trim().isEmpty()) {

            eventsViewModel.loadEvent(
                    eventId
            );
        }
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBackEventDetails =
                findViewById(
                        R.id.btnBackEventDetails
                );

        progressEventDetails =
                findViewById(
                        R.id.progressEventDetails
                );

        layoutEventDetailsError =
                findViewById(
                        R.id.layoutEventDetailsError
                );

        layoutEventDetailsLocation =
                findViewById(
                        R.id.layoutEventDetailsLocation
                );

        layoutEventDetailsDescription =
                findViewById(
                        R.id.layoutEventDetailsDescription
                );

        scrollEventDetails =
                findViewById(
                        R.id.scrollEventDetails
                );

        tvEventDetailsError =
                findViewById(
                        R.id.tvEventDetailsError
                );

        tvEventDetailsImportant =
                findViewById(
                        R.id.tvEventDetailsImportant
                );

        tvEventDetailsTitle =
                findViewById(
                        R.id.tvEventDetailsTitle
                );

        tvEventDetailsCategory =
                findViewById(
                        R.id.tvEventDetailsCategory
                );

        tvEventDetailsStatus =
                findViewById(
                        R.id.tvEventDetailsStatus
                );

        tvEventDetailsStart =
                findViewById(
                        R.id.tvEventDetailsStart
                );

        tvEventDetailsEnd =
                findViewById(
                        R.id.tvEventDetailsEnd
                );

        tvEventDetailsLocation =
                findViewById(
                        R.id.tvEventDetailsLocation
                );

        tvEventDetailsReminder =
                findViewById(
                        R.id.tvEventDetailsReminder
                );

        tvEventDetailsDescription =
                findViewById(
                        R.id.tvEventDetailsDescription
                );

        layoutEventCategoryIcon =
                findViewById(
                        R.id.layoutEventCategoryIcon
                );

        ivEventDetailsCategoryIcon =
                findViewById(
                        R.id.ivEventDetailsCategoryIcon
                );

        btnRetryEventDetails =
                findViewById(
                        R.id.btnRetryEventDetails
                );

        btnDeleteEvent =
                findViewById(
                        R.id.btnDeleteEvent
                );

        btnEditEvent =
                findViewById(
                        R.id.btnEditEvent
                );
    }

    // =========================================================
    // VIEW MODELS
    // =========================================================

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

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBackEventDetails.setOnClickListener(
                view ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );

        btnRetryEventDetails.setOnClickListener(
                view ->
                        eventsViewModel.loadEvent(
                                eventId
                        )
        );

        btnEditEvent.setOnClickListener(
                view ->
                        openEditEvent()
        );

        btnDeleteEvent.setOnClickListener(
                view ->
                        showDeleteConfirmation()
        );

        tvEventDetailsImportant.setOnClickListener(
                view -> {

                    if (currentEvent == null
                            || currentEvent.getId() == null) {

                        return;
                    }

                    eventsViewModel.updateImportant(
                            currentEvent.getId(),
                            !currentEvent.isImportant()
                    );
                }
        );
    }

    // =========================================================
    // EVENT OBSERVER
    // =========================================================

    private void observeEvent() {

        eventsViewModel
                .getEventDetailsState()
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

                                    if (state.getData() == null) {

                                        showError(
                                                "Event not found."
                                        );

                                        return;
                                    }

                                    currentEvent =
                                            state.getData();

                                    showEvent();

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
    // CATEGORIES OBSERVER
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

                            if (currentEvent != null) {

                                updateCategory();
                            }
                        }
                );
    }

    // =========================================================
    // IMPORTANT OBSERVER
    // =========================================================

    private void observeImportantAction() {

        eventsViewModel
                .getImportantActionState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }

                            switch (state.getStatus()) {

                                case LOADING:

                                    tvEventDetailsImportant.setEnabled(
                                            false
                                    );

                                    break;

                                case SUCCESS:

                                    tvEventDetailsImportant.setEnabled(
                                            true
                                    );

                                    if (state.getData() != null) {

                                        currentEvent =
                                                state.getData();

                                        updateImportant();
                                    }

                                    break;

                                case ERROR:

                                    tvEventDetailsImportant.setEnabled(
                                            true
                                    );

                                    Toast.makeText(
                                            this,
                                            state.getMessage() != null
                                                    ? state.getMessage()
                                                    : "Unable to update event.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    break;
                            }
                        }
                );
    }

    // =========================================================
    // DELETE OBSERVER
    // =========================================================

    private void observeDeleteAction() {

        eventsViewModel
                .getDeleteActionState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }

                            switch (state.getStatus()) {

                                case LOADING:

                                    btnDeleteEvent.setEnabled(
                                            false
                                    );

                                    btnEditEvent.setEnabled(
                                            false
                                    );

                                    break;

                                case SUCCESS:

                                    Toast.makeText(
                                            this,
                                            "Event deleted.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    setResult(
                                            RESULT_OK
                                    );

                                    finish();

                                    break;

                                case ERROR:

                                    btnDeleteEvent.setEnabled(
                                            true
                                    );

                                    btnEditEvent.setEnabled(
                                            true
                                    );

                                    Toast.makeText(
                                            this,
                                            state.getMessage() != null
                                                    ? state.getMessage()
                                                    : "Unable to delete event.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    break;
                            }
                        }
                );
    }

    // =========================================================
    // SHOW EVENT
    // =========================================================

    private void showEvent() {

        progressEventDetails.setVisibility(
                View.GONE
        );

        layoutEventDetailsError.setVisibility(
                View.GONE
        );

        scrollEventDetails.setVisibility(
                View.VISIBLE
        );

        String title =
                currentEvent.getTitle();

        if (title == null
                || title.trim().isEmpty()) {

            title =
                    "Untitled Event";
        }

        tvEventDetailsTitle.setText(
                title
        );

        tvEventDetailsStart.setText(
                formatDisplayDate(
                        currentEvent.getStartAt()
                )
        );

        tvEventDetailsEnd.setText(
                formatDisplayDate(
                        currentEvent.getEndAt()
                )
        );

        updateCategory();
        updateStatus();
        updateImportant();
        updateLocation();
        updateDescription();
        updateReminder();
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private void updateCategory() {

        if (currentEvent == null) {
            return;
        }

        String categoryId =
                currentEvent.getCategoryId();

        if (categoryId == null
                || categoryId.trim().isEmpty()) {

            tvEventDetailsCategory.setVisibility(
                    View.GONE
            );

            setDefaultCategoryAppearance();

            return;
        }

        Category category =
                categoryMap.get(
                        categoryId
                );

        if (category == null) {

            tvEventDetailsCategory.setVisibility(
                    View.GONE
            );

            setDefaultCategoryAppearance();

            return;
        }

        int categoryColor =
                parseCategoryColor(
                        category.getColor()
                );

        tvEventDetailsCategory.setVisibility(
                View.VISIBLE
        );

        String categoryName =
                category.getName();

        if (categoryName == null
                || categoryName.trim().isEmpty()) {

            categoryName =
                    "Category";
        }

        tvEventDetailsCategory.setText(
                categoryName
        );

        tvEventDetailsCategory.setTextColor(
                Color.WHITE
        );

        tvEventDetailsCategory.setBackground(
                createRoundedBackground(
                        categoryColor,
                        50
                )
        );

        ivEventDetailsCategoryIcon.setImageResource(
                getCategoryIcon(
                        category
                )
        );

        ivEventDetailsCategoryIcon.setColorFilter(
                categoryColor
        );

        layoutEventCategoryIcon.setBackground(
                createRoundedBackground(
                        makePastelColor(
                                categoryColor
                        ),
                        17
                )
        );
    }

    private void setDefaultCategoryAppearance() {

        int defaultColor =
                ContextCompat.getColor(
                        this,
                        R.color.sp_teal_dark
                );

        ivEventDetailsCategoryIcon.setImageResource(
                R.drawable.ic_task_category_default
        );

        ivEventDetailsCategoryIcon.setColorFilter(
                defaultColor
        );

        layoutEventCategoryIcon.setBackground(
                createRoundedBackground(
                        ContextCompat.getColor(
                                this,
                                R.color.sp_home_tasks_icon_bg
                        ),
                        17
                )
        );
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void updateStatus() {

        if (currentEvent == null) {
            return;
        }

        Date start =
                parseSupabaseDate(
                        currentEvent.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        currentEvent.getEndAt()
                );

        String status =
                "Upcoming";

        int backgroundColorResource =
                R.color.sp_home_events_icon_bg;

        int textColorResource =
                R.color.sp_home_blue;

        if (start != null
                && end != null) {

            Date now =
                    new Date();

            if (!now.before(start)
                    && !now.after(end)) {

                status =
                        "Ongoing";

                backgroundColorResource =
                        R.color.sp_home_tasks_icon_bg;

                textColorResource =
                        R.color.sp_teal_dark;

            } else if (now.after(end)) {

                status =
                        "Past";

                backgroundColorResource =
                        R.color.sp_border;

                textColorResource =
                        R.color.sp_text_secondary;
            }
        }

        tvEventDetailsStatus.setText(
                status
        );

        applyStatusBadgeColors(
                backgroundColorResource,
                textColorResource
        );
    }

    private void applyStatusBadgeColors(
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

        tvEventDetailsStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                        backgroundColor
                )
        );

        tvEventDetailsStatus.setTextColor(
                textColor
        );
    }

    // =========================================================
    // IMPORTANT
    // =========================================================

    private void updateImportant() {

        if (currentEvent == null) {
            return;
        }

        if (currentEvent.isImportant()) {

            tvEventDetailsImportant.setText(
                    "★"
            );

            tvEventDetailsImportant.setAlpha(
                    1f
            );

        } else {

            tvEventDetailsImportant.setText(
                    "☆"
            );

            tvEventDetailsImportant.setAlpha(
                    0.70f
            );
        }
    }

    // =========================================================
    // LOCATION
    // =========================================================

    private void updateLocation() {

        if (currentEvent == null) {
            return;
        }

        String location =
                currentEvent.getLocation();

        if (location == null
                || location.trim().isEmpty()) {

            layoutEventDetailsLocation.setVisibility(
                    View.GONE
            );

            return;
        }

        layoutEventDetailsLocation.setVisibility(
                View.VISIBLE
        );

        tvEventDetailsLocation.setText(
                location.trim()
        );
    }

    // =========================================================
    // DESCRIPTION
    // =========================================================

    private void updateDescription() {

        if (currentEvent == null) {
            return;
        }

        String description =
                currentEvent.getDescription();

        if (description == null
                || description.trim().isEmpty()) {

            layoutEventDetailsDescription.setVisibility(
                    View.GONE
            );

            return;
        }

        layoutEventDetailsDescription.setVisibility(
                View.VISIBLE
        );

        tvEventDetailsDescription.setText(
                description.trim()
        );
    }

    // =========================================================
    // REMINDER
    // =========================================================

    private void updateReminder() {

        if (currentEvent == null) {
            return;
        }

        ReminderType reminderType =
                currentEvent.getReminderType();

        if (reminderType == null) {

            reminderType =
                    ReminderType.NONE;
        }

        switch (reminderType) {

            case TEN_MINUTES:

                tvEventDetailsReminder.setText(
                        "10 minutes before"
                );

                break;

            case THIRTY_MINUTES:

                tvEventDetailsReminder.setText(
                        "30 minutes before"
                );

                break;

            case ONE_HOUR:

                tvEventDetailsReminder.setText(
                        "1 hour before"
                );

                break;

            case ONE_DAY:

                tvEventDetailsReminder.setText(
                        "1 day before"
                );

                break;

            case NONE:
            default:

                tvEventDetailsReminder.setText(
                        "No reminder"
                );

                break;
        }
    }

    // =========================================================
    // EDIT EVENT
    // =========================================================

    private void openEditEvent() {

        if (currentEvent == null
                || currentEvent.getId() == null
                || currentEvent.getId().trim().isEmpty()) {

            return;
        }

        Intent intent =
                new Intent(
                        this,
                        AddEditEventActivity.class
                );

        intent.putExtra(
                AddEditEventActivity.EXTRA_EVENT_ID,
                currentEvent.getId()
        );

        startActivity(
                intent
        );
    }

    // =========================================================
    // DELETE EVENT
    // =========================================================

    private void showDeleteConfirmation() {

        if (currentEvent == null
                || currentEvent.getId() == null) {

            return;
        }

        new MaterialAlertDialogBuilder(
                this
        )
                .setTitle(
                        "Delete Event"
                )
                .setMessage(
                        "Are you sure you want to delete this event? This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                eventsViewModel.deleteEvent(
                                        currentEvent.getId()
                                )
                )
                .show();
    }

    // =========================================================
    // UI STATES
    // =========================================================

    private void showLoading() {

        progressEventDetails.setVisibility(
                View.VISIBLE
        );

        scrollEventDetails.setVisibility(
                View.GONE
        );

        layoutEventDetailsError.setVisibility(
                View.GONE
        );
    }

    private void showError(
            String message
    ) {

        progressEventDetails.setVisibility(
                View.GONE
        );

        scrollEventDetails.setVisibility(
                View.GONE
        );

        layoutEventDetailsError.setVisibility(
                View.VISIBLE
        );

        if (message == null
                || message.trim().isEmpty()) {

            message =
                    "Unable to load event.";
        }

        tvEventDetailsError.setText(
                message
        );
    }

    // =========================================================
    // DATE
    // =========================================================

    private String formatDisplayDate(
            String value
    ) {

        Date date =
                parseSupabaseDate(
                        value
                );

        if (date == null) {

            return "Date not available";
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd MMM yyyy, HH:mm",
                        Locale.ENGLISH
                );

        return format.format(
                date
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

    // =========================================================
    // CATEGORY ICON
    // =========================================================

    private int getCategoryIcon(
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
    // CATEGORY COLOR
    // =========================================================

    private int parseCategoryColor(
            String color
    ) {

        if (color == null
                || color.trim().isEmpty()) {

            return Color.parseColor(
                    "#46C8BE"
            );
        }

        try {

            String formatted =
                    color.trim();

            if (!formatted.startsWith("#")) {

                formatted =
                        "#" + formatted;
            }

            return Color.parseColor(
                    formatted
            );

        } catch (IllegalArgumentException exception) {

            return Color.parseColor(
                    "#46C8BE"
            );
        }
    }

    private int makePastelColor(
            int color
    ) {

        int red =
                Color.red(
                        color
                );

        int green =
                Color.green(
                        color
                );

        int blue =
                Color.blue(
                        color
                );

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

    // =========================================================
    // BACKGROUND HELPERS
    // =========================================================

    private GradientDrawable createRoundedBackground(
            int color,
            int radiusDp
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                color
        );

        drawable.setCornerRadius(
                dpToPx(
                        radiusDp
                )
        );

        return drawable;
    }

    private float dpToPx(
            int dp
    ) {

        return dp
                * getResources()
                .getDisplayMetrics()
                .density;
    }
}