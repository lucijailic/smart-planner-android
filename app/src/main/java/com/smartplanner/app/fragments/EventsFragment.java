package com.smartplanner.app.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.event.AddEditEventActivity;
import com.smartplanner.app.activities.event.EventDetailsActivity;
import com.smartplanner.app.adapters.EventAdapter;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.viewmodels.CategoriesViewModel;
import com.smartplanner.app.viewmodels.EventsViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsFragment extends Fragment {

    // =========================================================
    // ENUMS
    // =========================================================

    private enum QuickFilter {
        ALL,
        TODAY,
        UPCOMING,
        ONGOING,
        PAST
    }

    private enum SortOption {
        START_SOONEST,
        START_LATEST,
        NEWEST,
        OLDEST,
        TITLE_A_Z
    }

    private enum ImportanceFilter {
        ALL,
        IMPORTANT,
        NOT_IMPORTANT
    }

    private enum DateFilter {
        ANY,
        TODAY,
        NEXT_7_DAYS,
        NEXT_30_DAYS
    }

    // =========================================================
    // VIEW MODELS
    // =========================================================

    private EventsViewModel eventsViewModel;
    private CategoriesViewModel categoriesViewModel;

    // =========================================================
    // ADAPTER
    // =========================================================

    private EventAdapter eventAdapter;

    // =========================================================
    // VIEWS
    // =========================================================

    private RecyclerView rvEvents;

    private ProgressBar progressEvents;

    private LinearLayout layoutEmptyEvents;
    private LinearLayout layoutErrorEvents;

    private TextView tvEventsError;

    private EditText etEventSearch;

    private TextView chipAll;
    private TextView chipToday;
    private TextView chipUpcoming;
    private TextView chipOngoing;
    private TextView chipPast;

    private MaterialButton btnEventFilters;
    private MaterialButton btnSortEvents;
    private MaterialButton btnRetryEvents;

    private FloatingActionButton fabAddEvent;

    private TextView tvEmptyEventsTitle;
    private TextView tvEmptyEventsMessage;

    // =========================================================
    // CALENDAR VIEWS
    // =========================================================

    private TextView tvEventCalendarMonth;
    private TextView tvSelectedEventDate;
    private TextView tvViewAllEvents;

    private TextView btnPreviousEventWeek;
    private TextView btnNextEventWeek;

    private ImageButton btnEventCalendarPicker;

    private LinearLayout layoutEventWeekDays;

    // =========================================================
    // DATA
    // =========================================================

    private final List<Event> allEvents =
            new ArrayList<>();

    private final List<Category> categories =
            new ArrayList<>();

    // =========================================================
    // QUICK FILTER
    // =========================================================

    private QuickFilter selectedQuickFilter =
            QuickFilter.ALL;

    // =========================================================
    // SORT
    // =========================================================

    private SortOption selectedSortOption =
            SortOption.START_SOONEST;

    // =========================================================
    // ADVANCED FILTERS
    // =========================================================

    private String selectedCategoryFilterId =
            null;

    private ImportanceFilter selectedImportanceFilter =
            ImportanceFilter.ALL;

    private DateFilter selectedDateFilter =
            DateFilter.ANY;

    // =========================================================
    // SEARCH
    // =========================================================

    private String searchQuery =
            "";

    // =========================================================
    // CALENDAR STATE
    // =========================================================

    private final Calendar selectedEventDate =
            Calendar.getInstance();

    private final Calendar displayedWeekStart =
            Calendar.getInstance();

    private boolean showAllEventDates =
            false;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public EventsFragment() {

        super(
                R.layout.fragment_events
        );
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_events,
                container,
                false
        );
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

        initViews(
                view
        );

        setupEventCalendar();

        setupRecyclerView();

        setupViewModels();

        setupListeners();

        observeEvents();

        observeCategories();

        observeImportantAction();

        updateQuickFilterAppearance();

        updateSortButtonText();

        updateFiltersButtonText();
    }

    @Override
    public void onResume() {

        super.onResume();

        if (eventsViewModel != null) {

            eventsViewModel.loadEvents();
        }

        if (categoriesViewModel != null) {

            categoriesViewModel.loadCategories();
        }
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews(
            View view
    ) {

        rvEvents =
                view.findViewById(
                        R.id.rvEvents
                );

        progressEvents =
                view.findViewById(
                        R.id.progressEvents
                );

        layoutEmptyEvents =
                view.findViewById(
                        R.id.layoutEmptyEvents
                );

        layoutErrorEvents =
                view.findViewById(
                        R.id.layoutErrorEvents
                );

        tvEventsError =
                view.findViewById(
                        R.id.tvEventsError
                );

        etEventSearch =
                view.findViewById(
                        R.id.etEventSearch
                );

        chipAll =
                view.findViewById(
                        R.id.chipEventAll
                );

        chipToday =
                view.findViewById(
                        R.id.chipEventToday
                );

        chipUpcoming =
                view.findViewById(
                        R.id.chipEventUpcoming
                );

        chipOngoing =
                view.findViewById(
                        R.id.chipEventOngoing
                );

        chipPast =
                view.findViewById(
                        R.id.chipEventPast
                );

        btnEventFilters =
                view.findViewById(
                        R.id.btnEventFilters
                );

        btnSortEvents =
                view.findViewById(
                        R.id.btnSortEvents
                );

        btnRetryEvents =
                view.findViewById(
                        R.id.btnRetryEvents
                );

        fabAddEvent =
                view.findViewById(
                        R.id.fabAddEvent
                );

        tvEmptyEventsTitle =
                view.findViewById(
                        R.id.tvEmptyEventsTitle
                );

        tvEmptyEventsMessage =
                view.findViewById(
                        R.id.tvEmptyEventsMessage
                );

        tvEventCalendarMonth =
                view.findViewById(
                        R.id.tvEventCalendarMonth
                );

        tvSelectedEventDate =
                view.findViewById(
                        R.id.tvSelectedEventDate
                );

        tvViewAllEvents =
                view.findViewById(
                        R.id.tvViewAllEvents
                );

        btnPreviousEventWeek =
                view.findViewById(
                        R.id.btnPreviousEventWeek
                );

        btnNextEventWeek =
                view.findViewById(
                        R.id.btnNextEventWeek
                );

        btnEventCalendarPicker =
                view.findViewById(
                        R.id.btnEventCalendarPicker
                );

        layoutEventWeekDays =
                view.findViewById(
                        R.id.layoutEventWeekDays
                );
    }

    // =========================================================
    // EVENT CALENDAR
    // =========================================================

    private void setupEventCalendar() {

        Calendar today =
                Calendar.getInstance();

        selectedEventDate.setTimeInMillis(
                today.getTimeInMillis()
        );

        normalizeCalendarDay(
                selectedEventDate
        );

        displayedWeekStart.setTimeInMillis(
                selectedEventDate.getTimeInMillis()
        );

        moveToMonday(
                displayedWeekStart
        );

        showAllEventDates =
                false;

        renderEventWeek();

        updateSelectedEventDateLabel();
    }

    private void moveToMonday(
            Calendar calendar
    ) {

        while (calendar.get(
                Calendar.DAY_OF_WEEK
        ) != Calendar.MONDAY) {

            calendar.add(
                    Calendar.DAY_OF_MONTH,
                    -1
            );
        }

        normalizeCalendarDay(
                calendar
        );
    }

    private void normalizeCalendarDay(
            Calendar calendar
    ) {

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
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
    }

    private String getDisplayedMonthTitle() {

        Calendar firstDay =
                (Calendar) displayedWeekStart.clone();

        Calendar lastDay =
                (Calendar) displayedWeekStart.clone();

        lastDay.add(
                Calendar.DAY_OF_MONTH,
                6
        );

        SimpleDateFormat monthFormat =
                new SimpleDateFormat(
                        "MMMM",
                        Locale.ENGLISH
                );

        SimpleDateFormat monthYearFormat =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.ENGLISH
                );

        int firstYear =
                firstDay.get(
                        Calendar.YEAR
                );

        int lastYear =
                lastDay.get(
                        Calendar.YEAR
                );

        int firstMonth =
                firstDay.get(
                        Calendar.MONTH
                );

        int lastMonth =
                lastDay.get(
                        Calendar.MONTH
                );

        if (firstYear == lastYear
                && firstMonth == lastMonth) {

            return monthYearFormat.format(
                    firstDay.getTime()
            );
        }

        if (firstYear == lastYear) {

            return monthFormat.format(
                    firstDay.getTime()
            )
                    + " – "
                    + monthYearFormat.format(
                    lastDay.getTime()
            );
        }

        return monthYearFormat.format(
                firstDay.getTime()
        )
                + " – "
                + monthYearFormat.format(
                lastDay.getTime()
        );
    }

    private void renderEventWeek() {

        if (layoutEventWeekDays == null) {
            return;
        }

        layoutEventWeekDays.removeAllViews();

        tvEventCalendarMonth.setText(
                getDisplayedMonthTitle()
        );

        Calendar day =
                (Calendar) displayedWeekStart.clone();

        for (int i = 0;
             i < 7;
             i++) {

            Calendar calendarDay =
                    (Calendar) day.clone();

            View dayView =
                    createEventDayView(
                            calendarDay
                    );

            layoutEventWeekDays.addView(
                    dayView
            );

            day.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
        }
    }

    private View createEventDayView(
            Calendar day
    ) {

        LinearLayout container =
                new LinearLayout(
                        requireContext()
                );

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        0,
                        dpToPxInt(64),
                        1f
                );

        containerParams.setMargins(
                dpToPxInt(1),
                0,
                dpToPxInt(1),
                0
        );

        container.setLayoutParams(
                containerParams
        );

        boolean selected =
                !showAllEventDates
                        && isSameDay(
                        day,
                        selectedEventDate
                );

        boolean today =
                isSameDay(
                        day,
                        Calendar.getInstance()
                );

        boolean hasEvents =
                hasEventsOnDate(
                        day
                );

        container.setBackground(
                createCalendarDayBackground(
                        selected
                )
        );

        TextView dayName =
                new TextView(
                        requireContext()
                );

        dayName.setGravity(
                Gravity.CENTER
        );

        dayName.setTextSize(
                9
        );

        dayName.setText(
                new SimpleDateFormat(
                        "EEE",
                        Locale.ENGLISH
                )
                        .format(
                                day.getTime()
                        )
                        .toUpperCase(
                                Locale.ENGLISH
                        )
        );

        dayName.setTextColor(
                requireContext().getColor(
                        selected
                                ? R.color.white
                                : R.color.sp_text_secondary
                )
        );

        TextView dayNumber =
                new TextView(
                        requireContext()
                );

        dayNumber.setGravity(
                Gravity.CENTER
        );

        dayNumber.setTextSize(
                16
        );

        dayNumber.setTypeface(
                dayNumber.getTypeface(),
                android.graphics.Typeface.BOLD
        );

        dayNumber.setText(
                String.valueOf(
                        day.get(
                                Calendar.DAY_OF_MONTH
                        )
                )
        );

        if (selected) {

            dayNumber.setTextColor(
                    requireContext()
                            .getColor(
                                    R.color.white
                            )
            );

        } else if (today) {

            dayNumber.setTextColor(
                    requireContext()
                            .getColor(
                                    R.color.sp_teal_dark
                            )
            );

        } else {

            dayNumber.setTextColor(
                    requireContext()
                            .getColor(
                                    R.color.sp_text_primary
                            )
            );
        }

        View eventDot =
                new View(
                        requireContext()
                );

        LinearLayout.LayoutParams dotParams =
                new LinearLayout.LayoutParams(
                        dpToPxInt(5),
                        dpToPxInt(5)
                );

        dotParams.topMargin =
                dpToPxInt(1);

        eventDot.setLayoutParams(
                dotParams
        );

        eventDot.setBackground(
                createCalendarDotBackground(
                        selected
                )
        );

        eventDot.setVisibility(
                hasEvents
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        LinearLayout.LayoutParams dayNameParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPxInt(18)
                );

        LinearLayout.LayoutParams dayNumberParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPxInt(25)
                );

        container.addView(
                dayName,
                dayNameParams
        );

        container.addView(
                dayNumber,
                dayNumberParams
        );

        container.addView(
                eventDot
        );

        container.setOnClickListener(
                view -> {

                    selectedEventDate.setTimeInMillis(
                            day.getTimeInMillis()
                    );

                    normalizeCalendarDay(
                            selectedEventDate
                    );

                    showAllEventDates =
                            false;

                    selectedDateFilter =
                            DateFilter.ANY;

                    updateFiltersButtonText();

                    updateSelectedEventDateLabel();

                    renderEventWeek();

                    applyFilters();
                }
        );

        return container;
    }

    private GradientDrawable createCalendarDayBackground(
            boolean selected
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setCornerRadius(
                dpToPxInt(14)
        );

        drawable.setColor(
                selected
                        ? requireContext().getColor(
                        R.color.sp_teal
                )
                        : Color.TRANSPARENT
        );

        return drawable;
    }

    private GradientDrawable createCalendarDotBackground(
            boolean selected
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.OVAL
        );

        drawable.setColor(
                requireContext().getColor(
                        selected
                                ? R.color.white
                                : R.color.sp_teal
                )
        );

        return drawable;
    }

    private boolean hasEventsOnDate(
            Calendar date
    ) {

        for (Event event : allEvents) {

            if (event != null
                    && eventOccursOnDate(
                    event,
                    date
            )) {

                return true;
            }
        }

        return false;
    }

    private boolean eventOccursOnDate(
            Event event,
            Calendar date
    ) {

        if (event == null
                || date == null) {

            return false;
        }

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        if (start == null) {
            return false;
        }

        if (end == null) {
            end = start;
        }

        Calendar selectedDay =
                (Calendar) date.clone();

        Date startOfSelectedDay =
                getStartOfDay(
                        selectedDay
                );

        Date endOfSelectedDay =
                getEndOfDay(
                        selectedDay
                );

        return !end.before(
                startOfSelectedDay
        )
                && !start.after(
                endOfSelectedDay
        );
    }

    private boolean matchesCalendarDate(
            Event event
    ) {

        if (showAllEventDates) {
            return true;
        }

        return eventOccursOnDate(
                event,
                selectedEventDate
        );
    }

    private void updateSelectedEventDateLabel() {

        if (tvSelectedEventDate == null
                || tvViewAllEvents == null) {

            return;
        }

        if (showAllEventDates) {

            int count =
                    allEvents.size();

            tvSelectedEventDate.setText(
                    count
                            + (count == 1
                            ? " event"
                            : " events")
            );

            tvViewAllEvents.setText(
                    "Today"
            );

            return;
        }

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "EEEE, MMMM d",
                        Locale.ENGLISH
                );

        int count =
                countEventsOnDate(
                        selectedEventDate
                );

        tvSelectedEventDate.setText(
                format.format(
                        selectedEventDate.getTime()
                )
                        + " · "
                        + count
                        + (count == 1
                        ? " event"
                        : " events")
        );

        tvViewAllEvents.setText(
                "View all"
        );
    }

    private int countEventsOnDate(
            Calendar date
    ) {

        int count =
                0;

        for (Event event : allEvents) {

            if (event != null
                    && eventOccursOnDate(
                    event,
                    date
            )) {

                count++;
            }
        }

        return count;
    }

    private void showPreviousEventWeek() {

        displayedWeekStart.add(
                Calendar.DAY_OF_MONTH,
                -7
        );

        renderEventWeek();
    }

    private void showNextEventWeek() {

        displayedWeekStart.add(
                Calendar.DAY_OF_MONTH,
                7
        );

        renderEventWeek();
    }

    private void showEventDatePicker() {

        DatePickerDialog dialog =
                new DatePickerDialog(
                        requireContext(),
                        (view,
                         year,
                         month,
                         dayOfMonth) -> {

                            selectedEventDate.set(
                                    Calendar.YEAR,
                                    year
                            );

                            selectedEventDate.set(
                                    Calendar.MONTH,
                                    month
                            );

                            selectedEventDate.set(
                                    Calendar.DAY_OF_MONTH,
                                    dayOfMonth
                            );

                            normalizeCalendarDay(
                                    selectedEventDate
                            );

                            displayedWeekStart.setTimeInMillis(
                                    selectedEventDate.getTimeInMillis()
                            );

                            moveToMonday(
                                    displayedWeekStart
                            );

                            showAllEventDates =
                                    false;

                            selectedDateFilter =
                                    DateFilter.ANY;

                            updateFiltersButtonText();

                            renderEventWeek();

                            updateSelectedEventDateLabel();

                            applyFilters();
                        },
                        selectedEventDate.get(
                                Calendar.YEAR
                        ),
                        selectedEventDate.get(
                                Calendar.MONTH
                        ),
                        selectedEventDate.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    private void toggleAllEventDates() {

        if (showAllEventDates) {

            Calendar today =
                    Calendar.getInstance();

            selectedEventDate.setTimeInMillis(
                    today.getTimeInMillis()
            );

            normalizeCalendarDay(
                    selectedEventDate
            );

            displayedWeekStart.setTimeInMillis(
                    selectedEventDate.getTimeInMillis()
            );

            moveToMonday(
                    displayedWeekStart
            );

            showAllEventDates =
                    false;

        } else {

            showAllEventDates =
                    true;
        }

        selectedDateFilter =
                DateFilter.ANY;

        updateFiltersButtonText();

        renderEventWeek();

        updateSelectedEventDateLabel();

        applyFilters();
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        eventAdapter =
                new EventAdapter();

        eventAdapter.setOnEventClickListener(
                event -> {

                    if (event == null
                            || event.getId() == null
                            || event.getId().trim().isEmpty()) {

                        return;
                    }

                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    EventDetailsActivity.class
                            );

                    intent.putExtra(
                            EventDetailsActivity.EXTRA_EVENT_ID,
                            event.getId()
                    );

                    startActivity(
                            intent
                    );
                }
        );

        eventAdapter.setOnImportantClickListener(
                (event, newImportantState) -> {

                    if (event == null
                            || event.getId() == null
                            || event.getId().trim().isEmpty()) {

                        return;
                    }

                    eventsViewModel.updateImportant(
                            event.getId(),
                            newImportantState
                    );
                }
        );

        rvEvents.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        rvEvents.setAdapter(
                eventAdapter
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

        etEventSearch.addTextChangedListener(
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

                        searchQuery =
                                s == null
                                        ? ""
                                        : s.toString().trim();

                        applyFilters();
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

        chipToday.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.TODAY
                        )
        );

        chipUpcoming.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.UPCOMING
                        )
        );

        chipOngoing.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.ONGOING
                        )
        );

        chipPast.setOnClickListener(
                view ->
                        selectQuickFilter(
                                QuickFilter.PAST
                        )
        );

        btnRetryEvents.setOnClickListener(
                view ->
                        eventsViewModel.loadEvents()
        );

        btnEventFilters.setOnClickListener(
                view ->
                        showFiltersBottomSheet()
        );

        btnSortEvents.setOnClickListener(
                view ->
                        showSortBottomSheet()
        );

        fabAddEvent.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    AddEditEventActivity.class
                            );

                    startActivity(
                            intent
                    );
                }
        );

        btnPreviousEventWeek.setOnClickListener(
                view ->
                        showPreviousEventWeek()
        );

        btnNextEventWeek.setOnClickListener(
                view ->
                        showNextEventWeek()
        );

        btnEventCalendarPicker.setOnClickListener(
                view ->
                        showEventDatePicker()
        );

        tvEventCalendarMonth.setOnClickListener(
                view ->
                        showEventDatePicker()
        );

        tvViewAllEvents.setOnClickListener(
                view ->
                        toggleAllEventDates()
        );
    }

    private void selectQuickFilter(
            QuickFilter filter
    ) {

        selectedQuickFilter =
                filter;

        updateQuickFilterAppearance();

        applyFilters();
    }

    // =========================================================
    // EVENTS OBSERVER
    // =========================================================

    private void observeEvents() {

        eventsViewModel
                .getEventsState()
                .observe(
                        getViewLifecycleOwner(),
                        this::renderEventsState
                );
    }

    private void renderEventsState(
            UiState<List<Event>> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:

                showLoading();

                break;

            case SUCCESS:

                allEvents.clear();

                if (state.getData() != null) {

                    allEvents.addAll(
                            state.getData()
                    );
                }

                renderEventWeek();

                updateSelectedEventDateLabel();

                applyFilters();

                break;

            case ERROR:

                showError(
                        state.getMessage()
                );

                break;
        }
    }

    // =========================================================
    // CATEGORIES
    // =========================================================

    private void observeCategories() {

        categoriesViewModel
                .getCategoriesState()
                .observe(
                        getViewLifecycleOwner(),
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

                            validateSelectedCategoryFilter();

                            eventAdapter.setCategories(
                                    categories
                            );

                            applyFilters();
                        }
                );
    }

    private void validateSelectedCategoryFilter() {

        if (selectedCategoryFilterId == null) {
            return;
        }

        for (Category category : categories) {

            if (category != null
                    && category.getId() != null
                    && category.getId().equals(
                    selectedCategoryFilterId
            )) {

                return;
            }
        }

        selectedCategoryFilterId =
                null;

        updateFiltersButtonText();
    }

    // =========================================================
    // IMPORTANT OBSERVER
    // =========================================================

    private void observeImportantAction() {

        eventsViewModel
                .getImportantActionState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            switch (state.getStatus()) {

                                case SUCCESS:

                                    eventsViewModel.loadEvents();

                                    break;

                                case ERROR:

                                    String message =
                                            state.getMessage();

                                    if (message == null
                                            || message.trim().isEmpty()) {

                                        message =
                                                "Unable to update event.";
                                    }

                                    Toast.makeText(
                                            requireContext(),
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    break;

                                case LOADING:
                                default:

                                    break;
                            }
                        }
                );
    }

    // =========================================================
    // FILTER BOTTOM SHEET
    // =========================================================

    private void showFiltersBottomSheet() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );

        View content =
                getLayoutInflater().inflate(
                        R.layout.dialog_event_filters,
                        null
                );

        dialog.setContentView(
                content
        );

        AutoCompleteTextView actCategory =
                content.findViewById(
                        R.id.actEventFilterCategory
                );

        TextView chipImportanceAll =
                content.findViewById(
                        R.id.chipEventImportanceAll
                );

        TextView chipImportant =
                content.findViewById(
                        R.id.chipEventImportant
                );

        TextView chipNotImportant =
                content.findViewById(
                        R.id.chipEventNotImportant
                );

        TextView chipDateAny =
                content.findViewById(
                        R.id.chipEventDateAny
                );

        TextView chipDateToday =
                content.findViewById(
                        R.id.chipEventDateToday
                );

        TextView chipDate7 =
                content.findViewById(
                        R.id.chipEventDate7Days
                );

        TextView chipDate30 =
                content.findViewById(
                        R.id.chipEventDate30Days
                );

        MaterialButton btnReset =
                content.findViewById(
                        R.id.btnResetEventFilters
                );

        MaterialButton btnApply =
                content.findViewById(
                        R.id.btnApplyEventFilters
                );

        final String[] tempCategoryId = {
                selectedCategoryFilterId
        };

        final ImportanceFilter[] tempImportance = {
                selectedImportanceFilter
        };

        final DateFilter[] tempDate = {
                selectedDateFilter
        };

        List<String> categoryNames =
                new ArrayList<>();

        categoryNames.add(
                "All categories"
        );

        int currentCategoryIndex =
                0;

        for (Category category : categories) {

            if (category == null) {
                continue;
            }

            categoryNames.add(
                    category.getName()
            );

            if (selectedCategoryFilterId != null
                    && selectedCategoryFilterId.equals(
                    category.getId()
            )) {

                currentCategoryIndex =
                        categoryNames.size() - 1;
            }
        }

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );

        actCategory.setAdapter(
                categoryAdapter
        );

        actCategory.setText(
                categoryNames.get(
                        currentCategoryIndex
                ),
                false
        );

        actCategory.setOnItemClickListener(
                (parent,
                 view,
                 position,
                 id) -> {

                    if (position == 0) {

                        tempCategoryId[0] =
                                null;

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

                        tempCategoryId[0] =
                                category.getId();
                    }
                }
        );

        updateImportanceFilterChips(
                chipImportanceAll,
                chipImportant,
                chipNotImportant,
                tempImportance[0]
        );

        updateDateFilterChips(
                chipDateAny,
                chipDateToday,
                chipDate7,
                chipDate30,
                tempDate[0]
        );

        chipImportanceAll.setOnClickListener(
                view -> {

                    tempImportance[0] =
                            ImportanceFilter.ALL;

                    updateImportanceFilterChips(
                            chipImportanceAll,
                            chipImportant,
                            chipNotImportant,
                            tempImportance[0]
                    );
                }
        );

        chipImportant.setOnClickListener(
                view -> {

                    tempImportance[0] =
                            ImportanceFilter.IMPORTANT;

                    updateImportanceFilterChips(
                            chipImportanceAll,
                            chipImportant,
                            chipNotImportant,
                            tempImportance[0]
                    );
                }
        );

        chipNotImportant.setOnClickListener(
                view -> {

                    tempImportance[0] =
                            ImportanceFilter.NOT_IMPORTANT;

                    updateImportanceFilterChips(
                            chipImportanceAll,
                            chipImportant,
                            chipNotImportant,
                            tempImportance[0]
                    );
                }
        );

        chipDateAny.setOnClickListener(
                view -> {

                    tempDate[0] =
                            DateFilter.ANY;

                    updateDateFilterChips(
                            chipDateAny,
                            chipDateToday,
                            chipDate7,
                            chipDate30,
                            tempDate[0]
                    );
                }
        );

        chipDateToday.setOnClickListener(
                view -> {

                    tempDate[0] =
                            DateFilter.TODAY;

                    updateDateFilterChips(
                            chipDateAny,
                            chipDateToday,
                            chipDate7,
                            chipDate30,
                            tempDate[0]
                    );
                }
        );

        chipDate7.setOnClickListener(
                view -> {

                    tempDate[0] =
                            DateFilter.NEXT_7_DAYS;

                    updateDateFilterChips(
                            chipDateAny,
                            chipDateToday,
                            chipDate7,
                            chipDate30,
                            tempDate[0]
                    );
                }
        );

        chipDate30.setOnClickListener(
                view -> {

                    tempDate[0] =
                            DateFilter.NEXT_30_DAYS;

                    updateDateFilterChips(
                            chipDateAny,
                            chipDateToday,
                            chipDate7,
                            chipDate30,
                            tempDate[0]
                    );
                }
        );

        btnReset.setOnClickListener(
                view -> {

                    tempCategoryId[0] =
                            null;

                    tempImportance[0] =
                            ImportanceFilter.ALL;

                    tempDate[0] =
                            DateFilter.ANY;

                    actCategory.setText(
                            "All categories",
                            false
                    );

                    updateImportanceFilterChips(
                            chipImportanceAll,
                            chipImportant,
                            chipNotImportant,
                            tempImportance[0]
                    );

                    updateDateFilterChips(
                            chipDateAny,
                            chipDateToday,
                            chipDate7,
                            chipDate30,
                            tempDate[0]
                    );
                }
        );

        btnApply.setOnClickListener(
                view -> {

                    selectedCategoryFilterId =
                            tempCategoryId[0];

                    selectedImportanceFilter =
                            tempImportance[0];

                    selectedDateFilter =
                            tempDate[0];

                    /*
                     * Advanced date filter works independently
                     * from the selected calendar day.
                     */
                    if (selectedDateFilter
                            != DateFilter.ANY) {

                        showAllEventDates =
                                true;

                        renderEventWeek();

                        updateSelectedEventDateLabel();
                    }

                    updateFiltersButtonText();

                    applyFilters();

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    // =========================================================
    // FILTER CHIP APPEARANCE
    // =========================================================

    private void updateImportanceFilterChips(
            TextView all,
            TextView important,
            TextView notImportant,
            ImportanceFilter selected
    ) {

        setFilterChipSelected(
                all,
                selected
                        == ImportanceFilter.ALL
        );

        setFilterChipSelected(
                important,
                selected
                        == ImportanceFilter.IMPORTANT
        );

        setFilterChipSelected(
                notImportant,
                selected
                        == ImportanceFilter.NOT_IMPORTANT
        );
    }

    private void updateDateFilterChips(
            TextView any,
            TextView today,
            TextView next7,
            TextView next30,
            DateFilter selected
    ) {

        setFilterChipSelected(
                any,
                selected
                        == DateFilter.ANY
        );

        setFilterChipSelected(
                today,
                selected
                        == DateFilter.TODAY
        );

        setFilterChipSelected(
                next7,
                selected
                        == DateFilter.NEXT_7_DAYS
        );

        setFilterChipSelected(
                next30,
                selected
                        == DateFilter.NEXT_30_DAYS
        );
    }

    private void setFilterChipSelected(
            TextView chip,
            boolean selected
    ) {

        if (selected) {

            chip.setBackgroundResource(
                    R.drawable.bg_task_filter_chip_selected
            );

            chip.setTextColor(
                    Color.WHITE
            );

            chip.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

        } else {

            chip.setBackgroundResource(
                    R.drawable.bg_task_filter_chip
            );

            chip.setTextColor(
                    getResources().getColor(
                            R.color.sp_text_secondary,
                            null
                    )
            );

            chip.setTypeface(
                    null,
                    android.graphics.Typeface.NORMAL
            );
        }
    }

    // =========================================================
    // SORT BOTTOM SHEET
    // =========================================================

    private void showSortBottomSheet() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );

        View content =
                getLayoutInflater().inflate(
                        R.layout.dialog_event_sort,
                        null
                );

        dialog.setContentView(
                content
        );

        View rowSoonest =
                content.findViewById(
                        R.id.rowSortEventSoonest
                );

        View rowLatest =
                content.findViewById(
                        R.id.rowSortEventLatest
                );

        View rowNewest =
                content.findViewById(
                        R.id.rowSortEventNewest
                );

        View rowOldest =
                content.findViewById(
                        R.id.rowSortEventOldest
                );

        View rowTitle =
                content.findViewById(
                        R.id.rowSortEventTitle
                );

        TextView checkSoonest =
                content.findViewById(
                        R.id.checkSortEventSoonest
                );

        TextView checkLatest =
                content.findViewById(
                        R.id.checkSortEventLatest
                );

        TextView checkNewest =
                content.findViewById(
                        R.id.checkSortEventNewest
                );

        TextView checkOldest =
                content.findViewById(
                        R.id.checkSortEventOldest
                );

        TextView checkTitle =
                content.findViewById(
                        R.id.checkSortEventTitle
                );

        updateSortChecks(
                checkSoonest,
                checkLatest,
                checkNewest,
                checkOldest,
                checkTitle
        );

        rowSoonest.setOnClickListener(
                view -> {

                    selectSortOption(
                            SortOption.START_SOONEST
                    );

                    dialog.dismiss();
                }
        );

        rowLatest.setOnClickListener(
                view -> {

                    selectSortOption(
                            SortOption.START_LATEST
                    );

                    dialog.dismiss();
                }
        );

        rowNewest.setOnClickListener(
                view -> {

                    selectSortOption(
                            SortOption.NEWEST
                    );

                    dialog.dismiss();
                }
        );

        rowOldest.setOnClickListener(
                view -> {

                    selectSortOption(
                            SortOption.OLDEST
                    );

                    dialog.dismiss();
                }
        );

        rowTitle.setOnClickListener(
                view -> {

                    selectSortOption(
                            SortOption.TITLE_A_Z
                    );

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    private void updateSortChecks(
            TextView soonest,
            TextView latest,
            TextView newest,
            TextView oldest,
            TextView title
    ) {

        soonest.setVisibility(
                selectedSortOption
                        == SortOption.START_SOONEST
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        latest.setVisibility(
                selectedSortOption
                        == SortOption.START_LATEST
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        newest.setVisibility(
                selectedSortOption
                        == SortOption.NEWEST
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        oldest.setVisibility(
                selectedSortOption
                        == SortOption.OLDEST
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        title.setVisibility(
                selectedSortOption
                        == SortOption.TITLE_A_Z
                        ? View.VISIBLE
                        : View.INVISIBLE
        );
    }

    private void selectSortOption(
            SortOption option
    ) {

        selectedSortOption =
                option;

        updateSortButtonText();

        applyFilters();
    }

    // =========================================================
    // APPLY FILTERS
    // =========================================================

    private void applyFilters() {

        List<Event> filtered =
                new ArrayList<>();

        for (Event event : allEvents) {

            if (event == null) {
                continue;
            }

            if (!matchesCalendarDate(
                    event
            )) {

                continue;
            }

            if (!matchesSearch(
                    event
            )) {

                continue;
            }

            if (!matchesQuickFilter(
                    event
            )) {

                continue;
            }

            if (!matchesCategoryFilter(
                    event
            )) {

                continue;
            }

            if (!matchesImportanceFilter(
                    event
            )) {

                continue;
            }

            if (!matchesDateFilter(
                    event
            )) {

                continue;
            }

            filtered.add(
                    event
            );
        }

        sortEvents(
                filtered
        );

        eventAdapter.setEvents(
                filtered
        );

        progressEvents.setVisibility(
                View.GONE
        );

        layoutErrorEvents.setVisibility(
                View.GONE
        );

        if (filtered.isEmpty()) {

            rvEvents.setVisibility(
                    View.GONE
            );

            layoutEmptyEvents.setVisibility(
                    View.VISIBLE
            );

            updateEmptyState();

        } else {

            layoutEmptyEvents.setVisibility(
                    View.GONE
            );

            rvEvents.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (!showAllEventDates
                && selectedDateFilter
                == DateFilter.ANY) {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "EEEE, MMMM d",
                            Locale.ENGLISH
                    );

            tvEmptyEventsTitle.setText(
                    "No events for this day"
            );

            tvEmptyEventsMessage.setText(
                    "There are no events on "
                            + format.format(
                            selectedEventDate.getTime()
                    )
                            + "."
            );

        } else {

            tvEmptyEventsTitle.setText(
                    "No matching events"
            );

            tvEmptyEventsMessage.setText(
                    "Try changing your search or filters."
            );
        }
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private boolean matchesSearch(
            Event event
    ) {

        if (searchQuery == null
                || searchQuery.trim().isEmpty()) {

            return true;
        }

        String query =
                searchQuery
                        .trim()
                        .toLowerCase(
                                Locale.US
                        );

        if (containsText(
                event.getTitle(),
                query
        )) {

            return true;
        }

        if (containsText(
                event.getDescription(),
                query
        )) {

            return true;
        }

        if (containsText(
                event.getLocation(),
                query
        )) {

            return true;
        }

        Category category =
                findCategoryById(
                        event.getCategoryId()
                );

        return category != null
                && containsText(
                category.getName(),
                query
        );
    }

    private boolean containsText(
            String source,
            String query
    ) {

        return source != null
                && source
                .toLowerCase(
                        Locale.US
                )
                .contains(
                        query
                );
    }

    // =========================================================
    // QUICK FILTER
    // =========================================================

    private boolean matchesQuickFilter(
            Event event
    ) {

        switch (selectedQuickFilter) {

            case TODAY:

                return isToday(
                        event
                );

            case UPCOMING:

                return isUpcoming(
                        event
                );

            case ONGOING:

                return isOngoing(
                        event
                );

            case PAST:

                return isPast(
                        event
                );

            case ALL:
            default:

                return true;
        }
    }

    // =========================================================
    // CATEGORY FILTER
    // =========================================================

    private boolean matchesCategoryFilter(
            Event event
    ) {

        if (selectedCategoryFilterId == null) {

            return true;
        }

        return selectedCategoryFilterId.equals(
                event.getCategoryId()
        );
    }

    // =========================================================
    // IMPORTANCE FILTER
    // =========================================================

    private boolean matchesImportanceFilter(
            Event event
    ) {

        switch (selectedImportanceFilter) {

            case IMPORTANT:

                return event.isImportant();

            case NOT_IMPORTANT:

                return !event.isImportant();

            case ALL:
            default:

                return true;
        }
    }

    // =========================================================
    // DATE FILTER
    // =========================================================

    private boolean matchesDateFilter(
            Event event
    ) {

        switch (selectedDateFilter) {

            case TODAY:

                return isToday(
                        event
                );

            case NEXT_7_DAYS:

                return isWithinNextDays(
                        event,
                        7
                );

            case NEXT_30_DAYS:

                return isWithinNextDays(
                        event,
                        30
                );

            case ANY:
            default:

                return true;
        }
    }

    private boolean isWithinNextDays(
            Event event,
            int days
    ) {

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        if (start == null
                || end == null) {

            return false;
        }

        Calendar now =
                Calendar.getInstance();

        Calendar limit =
                Calendar.getInstance();

        limit.add(
                Calendar.DAY_OF_YEAR,
                days
        );

        Date rangeStart =
                now.getTime();

        Date rangeEnd =
                getEndOfDay(
                        limit
                );

        return !end.before(
                rangeStart
        )
                && !start.after(
                rangeEnd
        );
    }

    // =========================================================
    // EVENT STATE
    // =========================================================

    private boolean isToday(
            Event event
    ) {

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        if (start == null
                || end == null) {

            return false;
        }

        Calendar today =
                Calendar.getInstance();

        Date startOfToday =
                getStartOfDay(
                        today
                );

        Date endOfToday =
                getEndOfDay(
                        today
                );

        return !end.before(
                startOfToday
        )
                && !start.after(
                endOfToday
        );
    }

    private boolean isUpcoming(
            Event event
    ) {

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        return start != null
                && start.after(
                new Date()
        );
    }

    private boolean isOngoing(
            Event event
    ) {

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        if (start == null
                || end == null) {

            return false;
        }

        Date now =
                new Date();

        return !now.before(
                start
        )
                && !now.after(
                end
        );
    }

    private boolean isPast(
            Event event
    ) {

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        return end != null
                && end.before(
                new Date()
        );
    }

    // =========================================================
    // SORT
    // =========================================================

    private void sortEvents(
            List<Event> events
    ) {

        switch (selectedSortOption) {

            case START_LATEST:

                events.sort(
                        (first, second) ->
                                compareDatesDescending(
                                        first.getStartAt(),
                                        second.getStartAt()
                                )
                );

                break;

            case NEWEST:

                events.sort(
                        (first, second) ->
                                compareDatesDescending(
                                        first.getCreatedAt(),
                                        second.getCreatedAt()
                                )
                );

                break;

            case OLDEST:

                events.sort(
                        Comparator.comparing(
                                event ->
                                        safeDateForAscending(
                                                event.getCreatedAt()
                                        )
                        )
                );

                break;

            case TITLE_A_Z:

                events.sort(
                        Comparator.comparing(
                                event ->
                                        event.getTitle() == null
                                                ? ""
                                                : event
                                                .getTitle()
                                                .trim()
                                                .toLowerCase(
                                                        Locale.US
                                                )
                        )
                );

                break;

            case START_SOONEST:
            default:

                events.sort(
                        Comparator.comparing(
                                event ->
                                        safeDateForAscending(
                                                event.getStartAt()
                                        )
                        )
                );

                break;
        }
    }

    private int compareDatesDescending(
            String first,
            String second
    ) {

        Date firstDate =
                safeDateForDescending(
                        first
                );

        Date secondDate =
                safeDateForDescending(
                        second
                );

        return secondDate.compareTo(
                firstDate
        );
    }

    private Date safeDateForAscending(
            String value
    ) {

        Date date =
                parseSupabaseDate(
                        value
                );

        if (date == null) {

            return new Date(
                    Long.MAX_VALUE
            );
        }

        return date;
    }

    private Date safeDateForDescending(
            String value
    ) {

        Date date =
                parseSupabaseDate(
                        value
                );

        if (date == null) {

            return new Date(
                    Long.MIN_VALUE
            );
        }

        return date;
    }

    // =========================================================
    // BUTTON TEXT
    // =========================================================

    private void updateSortButtonText() {

        btnSortEvents.setText(
                "Sort"
        );
    }

    private void updateFiltersButtonText() {

        int activeFilters =
                0;

        if (selectedCategoryFilterId != null) {
            activeFilters++;
        }

        if (selectedImportanceFilter
                != ImportanceFilter.ALL) {

            activeFilters++;
        }

        if (selectedDateFilter
                != DateFilter.ANY) {

            activeFilters++;
        }

        if (activeFilters == 0) {

            btnEventFilters.setText(
                    "Filters"
            );

        } else {

            btnEventFilters.setText(
                    "Filters (" + activeFilters + ")"
            );
        }
    }

    // =========================================================
    // QUICK FILTER APPEARANCE
    // =========================================================

    private void updateQuickFilterAppearance() {

        setQuickChipSelected(
                chipAll,
                selectedQuickFilter
                        == QuickFilter.ALL
        );

        setQuickChipSelected(
                chipToday,
                selectedQuickFilter
                        == QuickFilter.TODAY
        );

        setQuickChipSelected(
                chipUpcoming,
                selectedQuickFilter
                        == QuickFilter.UPCOMING
        );

        setQuickChipSelected(
                chipOngoing,
                selectedQuickFilter
                        == QuickFilter.ONGOING
        );

        setQuickChipSelected(
                chipPast,
                selectedQuickFilter
                        == QuickFilter.PAST
        );
    }

    private void setQuickChipSelected(
            TextView chip,
            boolean selected
    ) {

        if (selected) {

            chip.setBackgroundResource(
                    R.drawable.bg_task_filter_chip_selected
            );

            chip.setTextColor(
                    Color.WHITE
            );

            chip.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

        } else {

            chip.setBackgroundResource(
                    R.drawable.bg_task_filter_chip
            );

            chip.setTextColor(
                    getResources().getColor(
                            R.color.sp_text_secondary,
                            null
                    )
            );

            chip.setTypeface(
                    null,
                    android.graphics.Typeface.NORMAL
            );
        }
    }

    // =========================================================
    // CATEGORY HELPERS
    // =========================================================

    private Category findCategoryById(
            String id
    ) {

        if (id == null) {
            return null;
        }

        for (Category category : categories) {

            if (category != null
                    && category.getId() != null
                    && category.getId().equals(
                    id
            )) {

                return category;
            }
        }

        return null;
    }

    // =========================================================
    // UI STATES
    // =========================================================

    private void showLoading() {

        progressEvents.setVisibility(
                View.VISIBLE
        );

        rvEvents.setVisibility(
                View.GONE
        );

        layoutEmptyEvents.setVisibility(
                View.GONE
        );

        layoutErrorEvents.setVisibility(
                View.GONE
        );
    }

    private void showError(
            String message
    ) {

        progressEvents.setVisibility(
                View.GONE
        );

        rvEvents.setVisibility(
                View.GONE
        );

        layoutEmptyEvents.setVisibility(
                View.GONE
        );

        layoutErrorEvents.setVisibility(
                View.VISIBLE
        );

        if (message == null
                || message.trim().isEmpty()) {

            message =
                    "Unable to load events.";
        }

        tvEventsError.setText(
                message
        );
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

    private Date getStartOfDay(
            Calendar source
    ) {

        Calendar calendar =
                (Calendar) source.clone();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
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

        return calendar.getTime();
    }

    private Date getEndOfDay(
            Calendar source
    ) {

        Calendar calendar =
                (Calendar) source.clone();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                23
        );

        calendar.set(
                Calendar.MINUTE,
                59
        );

        calendar.set(
                Calendar.SECOND,
                59
        );

        calendar.set(
                Calendar.MILLISECOND,
                999
        );

        return calendar.getTime();
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

                parser.setLenient(
                        false
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
    // DP
    // =========================================================

    private int dpToPxInt(
            int dp
    ) {

        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}