package com.smartplanner.app.activities.smartplan;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.smartplanner.app.R;
import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.viewmodels.PlanningPreferencesViewModel;

public class PlanningPreferencesActivity extends AppCompatActivity {

    private static final int DEFAULT_MAX_DAILY_MINUTES = 240;
    private static final int DEFAULT_SESSION_MINUTES = 60;
    private static final int DEFAULT_BREAK_MINUTES = 15;

    private TextView btnBackPlanningPreferences;

    private AutoCompleteTextView dropdownDailyWorkload;
    private AutoCompleteTextView dropdownSessionLength;
    private AutoCompleteTextView dropdownBreakLength;

    private MaterialButton btnSavePlanningPreferences;

    private ProgressBar progressPlanningPreferences;

    private View contentPlanningPreferences;
    private View layoutPlanningPreferencesError;

    private TextView tvPlanningPreferencesError;

    private MaterialButton btnRetryPlanningPreferences;

    private PlanningPreferencesViewModel viewModel;

    private PlanningPreferences currentPreferences;

    private boolean preferencesExist = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_preferences);

        initViews();
        setupDropdowns();
        setupViewModel();
        setupListeners();
        observeStates();

        viewModel.loadPreferences();
    }


    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        btnBackPlanningPreferences =
                findViewById(R.id.btnBackPlanningPreferences);

        dropdownDailyWorkload =
                findViewById(R.id.dropdownDailyWorkload);

        dropdownSessionLength =
                findViewById(R.id.dropdownSessionLength);

        dropdownBreakLength =
                findViewById(R.id.dropdownBreakLength);

        btnSavePlanningPreferences =
                findViewById(R.id.btnSavePlanningPreferences);

        progressPlanningPreferences =
                findViewById(R.id.progressPlanningPreferences);

        contentPlanningPreferences =
                findViewById(R.id.contentPlanningPreferences);

        layoutPlanningPreferencesError =
                findViewById(R.id.layoutPlanningPreferencesError);

        tvPlanningPreferencesError =
                findViewById(R.id.tvPlanningPreferencesError);

        btnRetryPlanningPreferences =
                findViewById(R.id.btnRetryPlanningPreferences);
    }


    // =========================================================
    // DROPDOWNS
    // =========================================================

    private void setupDropdowns() {

        String[] workloadOptions = {
                "2 hours",
                "3 hours",
                "4 hours",
                "5 hours",
                "6 hours",
                "7 hours",
                "8 hours"
        };

        String[] sessionOptions = {
                "30 minutes",
                "45 minutes",
                "60 minutes",
                "75 minutes",
                "90 minutes",
                "120 minutes"
        };

        String[] breakOptions = {
                "No break",
                "15 minutes",
                "30 minutes",
                "45 minutes",
                "60 minutes"
        };


        ArrayAdapter<String> workloadAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        workloadOptions
                );

        ArrayAdapter<String> sessionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        sessionOptions
                );

        ArrayAdapter<String> breakAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        breakOptions
                );


        dropdownDailyWorkload.setAdapter(
                workloadAdapter
        );

        dropdownSessionLength.setAdapter(
                sessionAdapter
        );

        dropdownBreakLength.setAdapter(
                breakAdapter
        );


        dropdownDailyWorkload.setKeyListener(null);
        dropdownSessionLength.setKeyListener(null);
        dropdownBreakLength.setKeyListener(null);
    }


    // =========================================================
    // VIEW MODEL
    // =========================================================

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                PlanningPreferencesViewModel.class
                        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBackPlanningPreferences.setOnClickListener(
                view -> finish()
        );


        btnRetryPlanningPreferences.setOnClickListener(
                view -> viewModel.loadPreferences()
        );


        btnSavePlanningPreferences.setOnClickListener(
                view -> savePreferences()
        );
    }


    // =========================================================
    // OBSERVERS
    // =========================================================

    private void observeStates() {

        viewModel.getPreferencesState()
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
                                    showContent();

                                    currentPreferences =
                                            state.getData();

                                    preferencesExist =
                                            currentPreferences != null;

                                    if (currentPreferences == null) {

                                        showDefaultValues();

                                    } else {

                                        showCurrentPreferences(
                                                currentPreferences
                                        );
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


        viewModel.getSavePreferencesState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }

                            switch (state.getStatus()) {

                                case LOADING:
                                    setSaving(true);
                                    break;

                                case SUCCESS:

                                    setSaving(false);

                                    currentPreferences =
                                            state.getData();

                                    preferencesExist =
                                            currentPreferences != null;

                                    Toast.makeText(
                                            PlanningPreferencesActivity.this,
                                            "Planning preferences saved.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    break;

                                case ERROR:

                                    setSaving(false);

                                    Toast.makeText(
                                            PlanningPreferencesActivity.this,
                                            state.getMessage() != null
                                                    ? state.getMessage()
                                                    : "Unable to save planning preferences.",
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

    private void savePreferences() {

        int maxDailyMinutes =
                parseWorkloadMinutes(
                        dropdownDailyWorkload
                                .getText()
                                .toString()
                );

        int preferredSessionMinutes =
                parseMinutes(
                        dropdownSessionLength
                                .getText()
                                .toString()
                );

        int breakMinutes =
                parseBreakMinutes(
                        dropdownBreakLength
                                .getText()
                                .toString()
                );


        if (maxDailyMinutes <= 0) {

            Toast.makeText(
                    this,
                    "Select a maximum daily workload.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (preferredSessionMinutes <= 0) {

            Toast.makeText(
                    this,
                    "Select a preferred session length.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (breakMinutes < 0) {

            Toast.makeText(
                    this,
                    "Select a break duration.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (preferencesExist) {

            viewModel.updatePreferences(
                    maxDailyMinutes,
                    preferredSessionMinutes,
                    breakMinutes
            );

        } else {

            viewModel.createPreferences(
                    maxDailyMinutes,
                    preferredSessionMinutes,
                    breakMinutes
            );
        }
    }


    // =========================================================
    // DISPLAY DATA
    // =========================================================

    private void showDefaultValues() {

        setWorkloadValue(
                DEFAULT_MAX_DAILY_MINUTES
        );

        setSessionValue(
                DEFAULT_SESSION_MINUTES
        );

        setBreakValue(
                DEFAULT_BREAK_MINUTES
        );
    }


    private void showCurrentPreferences(
            PlanningPreferences preferences
    ) {

        setWorkloadValue(
                preferences.getMaxDailyMinutes()
        );

        setSessionValue(
                preferences.getPreferredSessionMinutes()
        );

        setBreakValue(
                preferences.getBreakMinutes()
        );
    }


    private void setWorkloadValue(
            int minutes
    ) {

        if (minutes % 60 == 0) {

            int hours = minutes / 60;

            dropdownDailyWorkload.setText(
                    hours + (hours == 1
                            ? " hour"
                            : " hours"),
                    false
            );

        } else {

            dropdownDailyWorkload.setText(
                    formatMinutes(minutes),
                    false
            );
        }
    }


    private void setSessionValue(
            int minutes
    ) {

        dropdownSessionLength.setText(
                minutes + " minutes",
                false
        );
    }


    private void setBreakValue(
            int minutes
    ) {

        if (minutes == 0) {

            dropdownBreakLength.setText(
                    "No break",
                    false
            );

        } else {

            dropdownBreakLength.setText(
                    minutes + " minutes",
                    false
            );
        }
    }


    // =========================================================
    // PARSING
    // =========================================================

    private int parseWorkloadMinutes(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return -1;
        }


        String normalized =
                value.trim().toLowerCase();


        if (normalized.contains("hour")) {

            try {

                int hours =
                        Integer.parseInt(
                                normalized
                                        .split(" ")[0]
                        );

                return hours * 60;

            } catch (Exception exception) {

                return -1;
            }
        }


        return parseMinutes(
                normalized
        );
    }


    private int parseMinutes(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return -1;
        }


        try {

            return Integer.parseInt(
                    value
                            .trim()
                            .split(" ")[0]
            );

        } catch (Exception exception) {

            return -1;
        }
    }


    private int parseBreakMinutes(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return -1;
        }


        if ("No break".equalsIgnoreCase(
                value.trim()
        )) {

            return 0;
        }


        return parseMinutes(
                value
        );
    }


    private String formatMinutes(
            int totalMinutes
    ) {

        int hours =
                totalMinutes / 60;

        int minutes =
                totalMinutes % 60;


        if (hours <= 0) {

            return minutes + " minutes";
        }


        if (minutes == 0) {

            return hours
                    + (hours == 1
                    ? " hour"
                    : " hours");
        }


        return hours
                + " h "
                + minutes
                + " min";
    }


    // =========================================================
    // UI STATES
    // =========================================================

    private void showLoading() {

        progressPlanningPreferences.setVisibility(
                View.VISIBLE
        );

        contentPlanningPreferences.setVisibility(
                View.GONE
        );

        layoutPlanningPreferencesError.setVisibility(
                View.GONE
        );
    }


    private void showContent() {

        progressPlanningPreferences.setVisibility(
                View.GONE
        );

        contentPlanningPreferences.setVisibility(
                View.VISIBLE
        );

        layoutPlanningPreferencesError.setVisibility(
                View.GONE
        );
    }


    private void showError(
            String message
    ) {

        progressPlanningPreferences.setVisibility(
                View.GONE
        );

        contentPlanningPreferences.setVisibility(
                View.GONE
        );

        layoutPlanningPreferencesError.setVisibility(
                View.VISIBLE
        );


        tvPlanningPreferencesError.setText(
                message != null
                        ? message
                        : "Unable to load planning preferences."
        );
    }


    private void setSaving(
            boolean saving
    ) {

        btnSavePlanningPreferences.setEnabled(
                !saving
        );

        dropdownDailyWorkload.setEnabled(
                !saving
        );

        dropdownSessionLength.setEnabled(
                !saving
        );

        dropdownBreakLength.setEnabled(
                !saving
        );


        if (saving) {

            btnSavePlanningPreferences.setText(
                    "Saving..."
            );

        } else {

            btnSavePlanningPreferences.setText(
                    "Save Preferences"
            );
        }
    }
}