package com.smartplanner.app.activities.smartplan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.smartplanner.app.R;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.UserAvailabilityRequest;
import com.smartplanner.app.models.enums.DayOfWeekType;
import com.smartplanner.app.viewmodels.AvailabilityViewModel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AvailabilityActivity
        extends AppCompatActivity {

    private static final String DEFAULT_START_TIME =
            "08:00";

    private static final String DEFAULT_END_TIME =
            "18:00";


    private AvailabilityViewModel viewModel;


    private TextView btnBackAvailability;

    private ProgressBar progressAvailability;
    private ProgressBar progressSaveAvailability;

    private NestedScrollView scrollAvailability;

    private LinearLayout layoutAvailabilityDays;
    private LinearLayout layoutAvailabilityError;

    private TextView tvAvailabilityError;

    private MaterialButton btnSaveAvailability;
    private MaterialButton btnRetryAvailability;


    private final Map<DayOfWeekType, DayRow> dayRows =
            new EnumMap<>(
                    DayOfWeekType.class
            );


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_availability
        );


        initViews();

        setupViewModel();

        createDayRows();

        setupListeners();

        observeAvailability();

        observeSaveAvailability();


        viewModel.loadAvailability();
    }


    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        btnBackAvailability =
                findViewById(
                        R.id.btnBackAvailability
                );

        progressAvailability =
                findViewById(
                        R.id.progressAvailability
                );

        progressSaveAvailability =
                findViewById(
                        R.id.progressSaveAvailability
                );

        scrollAvailability =
                findViewById(
                        R.id.scrollAvailability
                );

        layoutAvailabilityDays =
                findViewById(
                        R.id.layoutAvailabilityDays
                );

        layoutAvailabilityError =
                findViewById(
                        R.id.layoutAvailabilityError
                );

        tvAvailabilityError =
                findViewById(
                        R.id.tvAvailabilityError
                );

        btnSaveAvailability =
                findViewById(
                        R.id.btnSaveAvailability
                );

        btnRetryAvailability =
                findViewById(
                        R.id.btnRetryAvailability
                );
    }


    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                AvailabilityViewModel.class
                        );
    }


    // =========================================================
    // DAY ROWS
    // =========================================================

    private void createDayRows() {

        layoutAvailabilityDays.removeAllViews();

        dayRows.clear();


        for (DayOfWeekType day :
                DayOfWeekType.values()) {

            View rowView =
                    LayoutInflater
                            .from(this)
                            .inflate(
                                    R.layout.item_availability_day,
                                    layoutAvailabilityDays,
                                    false
                            );


            DayRow row =
                    new DayRow(
                            day,
                            rowView
                    );


            dayRows.put(
                    day,
                    row
            );


            layoutAvailabilityDays.addView(
                    rowView
            );


            setupDayRow(
                    row
            );
        }
    }


    private void setupDayRow(
            DayRow row
    ) {

        row.tvDay.setText(
                getDisplayDayName(
                        row.day
                )
        );


        row.switchDay.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    row.enabled =
                            isChecked;

                    updateDayRowEnabledState(
                            row
                    );
                }
        );


        row.btnStart.setOnClickListener(
                view ->
                        showTimeSelectionDialog(
                                row,
                                true
                        )
        );


        row.btnEnd.setOnClickListener(
                view ->
                        showTimeSelectionDialog(
                                row,
                                false
                        )
        );
    }


    // =========================================================
    // DEFAULT VALUES
    // =========================================================

    private void applyDefaultAvailability() {

        for (DayOfWeekType day :
                DayOfWeekType.values()) {

            DayRow row =
                    dayRows.get(
                            day
                    );


            if (row == null) {
                continue;
            }


            boolean enabled =
                    day != DayOfWeekType.SATURDAY
                            && day != DayOfWeekType.SUNDAY;


            setDayRowValues(
                    row,
                    enabled,
                    DEFAULT_START_TIME,
                    DEFAULT_END_TIME
            );
        }
    }


    // =========================================================
    // LOAD EXISTING VALUES
    // =========================================================

    private void populateAvailability(
            List<UserAvailability> availability
    ) {

        /*
         * First Smart Plan setup.
         *
         * Nothing exists yet in Supabase,
         * therefore show proposed default week.
         */
        if (availability == null
                || availability.isEmpty()) {

            applyDefaultAvailability();

            return;
        }


        /*
         * Start with defaults in case an old/incomplete
         * account is missing one of the seven rows.
         */
        applyDefaultAvailability();


        for (UserAvailability item :
                availability) {

            if (item == null
                    || item.getDayOfWeek() == null) {

                continue;
            }


            DayOfWeekType day;


            try {

                day =
                        DayOfWeekType.valueOf(
                                item
                                        .getDayOfWeek()
                                        .trim()
                                        .toUpperCase(
                                                Locale.US
                                        )
                        );

            } catch (Exception exception) {

                continue;
            }


            DayRow row =
                    dayRows.get(
                            day
                    );


            if (row == null) {
                continue;
            }


            String startTime =
                    normalizeDisplayTime(
                            item.getStartTime()
                    );

            String endTime =
                    normalizeDisplayTime(
                            item.getEndTime()
                    );


            if (startTime.isEmpty()) {

                startTime =
                        DEFAULT_START_TIME;
            }


            if (endTime.isEmpty()) {

                endTime =
                        DEFAULT_END_TIME;
            }


            setDayRowValues(
                    row,
                    item.isEnabled(),
                    startTime,
                    endTime
            );
        }
    }


    private void setDayRowValues(
            DayRow row,
            boolean enabled,
            String startTime,
            String endTime
    ) {

        row.enabled =
                enabled;

        row.startTime =
                startTime;

        row.endTime =
                endTime;


        row.switchDay.setChecked(
                enabled
        );

        row.btnStart.setText(
                startTime
        );

        row.btnEnd.setText(
                endTime
        );


        updateDayRowEnabledState(
                row
        );
    }


    private void updateDayRowEnabledState(
            DayRow row
    ) {

        boolean enabled =
                row.enabled;


        row.btnStart.setEnabled(
                enabled
        );

        row.btnEnd.setEnabled(
                enabled
        );


        row.layoutTimes.setAlpha(
                enabled
                        ? 1.0f
                        : 0.45f
        );
    }


    // =========================================================
    // TIME SELECTION
    // =========================================================

    private void showTimeSelectionDialog(
            DayRow row,
            boolean startTime
    ) {

        String currentValue =
                startTime
                        ? row.startTime
                        : row.endTime;


        String[] timeOptions =
                createTimeOptions();


        int selectedIndex =
                findTimeIndex(
                        timeOptions,
                        currentValue
                );


        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(
                        this
                );


        builder.setTitle(
                startTime
                        ? "Select start time"
                        : "Select end time"
        );


        builder.setSingleChoiceItems(
                timeOptions,
                selectedIndex,
                (dialog, which) -> {

                    String selected =
                            timeOptions[which];


                    if (startTime) {

                        row.startTime =
                                selected;

                        row.btnStart.setText(
                                selected
                        );

                    } else {

                        row.endTime =
                                selected;

                        row.btnEnd.setText(
                                selected
                        );
                    }


                    dialog.dismiss();
                }
        );


        builder.setNegativeButton(
                "Cancel",
                null
        );


        builder.show();
    }


    private String[] createTimeOptions() {

        List<String> times =
                new ArrayList<>();


        for (int hour = 0;
             hour < 24;
             hour++) {

            for (int minute = 0;
                 minute < 60;
                 minute += 15) {

                times.add(
                        String.format(
                                Locale.US,
                                "%02d:%02d",
                                hour,
                                minute
                        )
                );
            }
        }


        return times.toArray(
                new String[0]
        );
    }


    private int findTimeIndex(
            String[] options,
            String value
    ) {

        if (options == null
                || value == null) {

            return -1;
        }


        for (int i = 0;
             i < options.length;
             i++) {

            if (value.equals(
                    options[i]
            )) {

                return i;
            }
        }


        return -1;
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBackAvailability.setOnClickListener(
                view -> finish()
        );


        btnRetryAvailability.setOnClickListener(
                view ->
                        viewModel.loadAvailability()
        );


        btnSaveAvailability.setOnClickListener(
                view ->
                        saveAvailability()
        );
    }


    // =========================================================
    // OBSERVE LOAD
    // =========================================================

    private void observeAvailability() {

        viewModel
                .getAvailabilityState()
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

                                    populateAvailability(
                                            state.getData()
                                    );

                                    showContent();

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
    // OBSERVE SAVE
    // =========================================================

    private void observeSaveAvailability() {

        viewModel
                .getSaveAvailabilityState()
                .observe(
                        this,
                        state -> {

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


                                    if (state.getData() != null) {

                                        populateAvailability(
                                                state.getData()
                                        );
                                    }


                                    Toast.makeText(
                                            this,
                                            "Availability saved.",
                                            Toast.LENGTH_SHORT
                                    ).show();

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
                                                "Unable to save availability. Please try again.";
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
    // SAVE
    // =========================================================

    private void saveAvailability() {

        List<UserAvailabilityRequest> requests =
                new ArrayList<>();


        for (DayOfWeekType day :
                DayOfWeekType.values()) {

            DayRow row =
                    dayRows.get(
                            day
                    );


            if (row == null) {

                Toast.makeText(
                        this,
                        "Availability data is incomplete.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (!isValidTime(
                    row.startTime
            )
                    || !isValidTime(
                    row.endTime
            )) {

                Toast.makeText(
                        this,
                        "Please select valid times for "
                                + getDisplayDayName(day)
                                + ".",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            /*
             * Disabled days retain their times.
             *
             * Therefore the database still receives
             * a valid start/end interval.
             */
            if (timeToMinutes(
                    row.endTime
            )
                    <= timeToMinutes(
                    row.startTime
            )) {

                Toast.makeText(
                        this,
                        "End time must be after start time for "
                                + getDisplayDayName(day)
                                + ".",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            requests.add(
                    new UserAvailabilityRequest(
                            day.name(),
                            row.startTime,
                            row.endTime,
                            row.enabled
                    )
            );
        }


        viewModel.saveAvailability(
                requests
        );
    }


    // =========================================================
    // SCREEN STATES
    // =========================================================

    private void showLoading() {

        progressAvailability.setVisibility(
                View.VISIBLE
        );

        scrollAvailability.setVisibility(
                View.GONE
        );

        layoutAvailabilityError.setVisibility(
                View.GONE
        );
    }


    private void showContent() {

        progressAvailability.setVisibility(
                View.GONE
        );

        scrollAvailability.setVisibility(
                View.VISIBLE
        );

        layoutAvailabilityError.setVisibility(
                View.GONE
        );
    }


    private void showError(
            String message
    ) {

        progressAvailability.setVisibility(
                View.GONE
        );

        scrollAvailability.setVisibility(
                View.GONE
        );

        layoutAvailabilityError.setVisibility(
                View.VISIBLE
        );


        if (message == null
                || message.trim().isEmpty()) {

            message =
                    "Unable to load availability.";
        }


        tvAvailabilityError.setText(
                message
        );
    }


    private void setSaving(
            boolean saving
    ) {

        progressSaveAvailability.setVisibility(
                saving
                        ? View.VISIBLE
                        : View.GONE
        );


        btnSaveAvailability.setEnabled(
                !saving
        );


        btnSaveAvailability.setText(
                saving
                        ? "Saving..."
                        : "Save Availability"
        );


        for (DayRow row :
                dayRows.values()) {

            row.switchDay.setEnabled(
                    !saving
            );


            row.btnStart.setEnabled(
                    !saving
                            && row.enabled
            );


            row.btnEnd.setEnabled(
                    !saving
                            && row.enabled
            );
        }
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String normalizeDisplayTime(
            String value
    ) {

        if (value == null) {

            return "";
        }


        String normalized =
                value.trim();


        if (normalized.length() >= 5) {

            return normalized.substring(
                    0,
                    5
            );
        }


        return normalized;
    }


    private boolean isValidTime(
            String time
    ) {

        if (time == null
                || !time.matches(
                "^([01]\\d|2[0-3]):[0-5]\\d$"
        )) {

            return false;
        }


        try {

            int minute =
                    Integer.parseInt(
                            time.substring(
                                    3,
                                    5
                            )
                    );


            return minute % 15 == 0;

        } catch (Exception exception) {

            return false;
        }
    }


    private int timeToMinutes(
            String time
    ) {

        if (!isValidTime(time)) {

            return -1;
        }


        try {

            int hour =
                    Integer.parseInt(
                            time.substring(
                                    0,
                                    2
                            )
                    );

            int minute =
                    Integer.parseInt(
                            time.substring(
                                    3,
                                    5
                            )
                    );


            return hour * 60
                    + minute;

        } catch (Exception exception) {

            return -1;
        }
    }


    private String getDisplayDayName(
            DayOfWeekType day
    ) {

        switch (day) {

            case MONDAY:
                return "Monday";

            case TUESDAY:
                return "Tuesday";

            case WEDNESDAY:
                return "Wednesday";

            case THURSDAY:
                return "Thursday";

            case FRIDAY:
                return "Friday";

            case SATURDAY:
                return "Saturday";

            case SUNDAY:
            default:
                return "Sunday";
        }
    }


    // =========================================================
    // DAY ROW HOLDER
    // =========================================================

    private static class DayRow {

        private final DayOfWeekType day;

        private final TextView tvDay;

        private final MaterialSwitch switchDay;

        private final LinearLayout layoutTimes;

        private final MaterialButton btnStart;
        private final MaterialButton btnEnd;


        private boolean enabled;

        private String startTime =
                DEFAULT_START_TIME;

        private String endTime =
                DEFAULT_END_TIME;


        private DayRow(
                DayOfWeekType day,
                View view
        ) {

            this.day =
                    day;


            tvDay =
                    view.findViewById(
                            R.id.tvAvailabilityDay
                    );

            switchDay =
                    view.findViewById(
                            R.id.switchAvailabilityDay
                    );

            layoutTimes =
                    view.findViewById(
                            R.id.layoutAvailabilityTimes
                    );

            btnStart =
                    view.findViewById(
                            R.id.btnAvailabilityStart
                    );

            btnEnd =
                    view.findViewById(
                            R.id.btnAvailabilityEnd
                    );
        }
    }
}