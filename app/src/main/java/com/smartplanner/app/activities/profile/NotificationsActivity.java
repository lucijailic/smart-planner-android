package com.smartplanner.app.activities.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.smartplanner.app.R;
import com.smartplanner.app.models.NotificationPreferences;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.viewmodels.NotificationPreferencesViewModel;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedHashMap;
import java.util.Map;

public class NotificationsActivity
        extends AppCompatActivity {

    private static final String REMINDER_NONE =
            "NONE";

    private static final String REMINDER_TEN_MINUTES =
            "TEN_MINUTES";

    private static final String REMINDER_THIRTY_MINUTES =
            "THIRTY_MINUTES";

    private static final String REMINDER_ONE_HOUR =
            "ONE_HOUR";

    private static final String REMINDER_ONE_DAY =
            "ONE_DAY";

    private LinearLayout layoutNotificationsForm;
    private LinearLayout layoutNotificationsError;

    private ProgressBar progressNotificationsLoading;
    private ProgressBar progressSaveNotifications;

    private TextView tvNotificationsError;

    private MaterialSwitch switchNotificationsEnabled;

    private AutoCompleteTextView actTaskReminder;
    private AutoCompleteTextView actEventReminder;

    private MaterialButton btnSaveNotifications;
    private MaterialButton btnRetryNotifications;

    private NotificationPreferencesViewModel viewModel;

    private final Map<String, String> reminderLabelToValue =
            new LinkedHashMap<>();

    private final Map<String, String> reminderValueToLabel =
            new LinkedHashMap<>();

    private boolean preferencesLoaded = false;

    private TextInputLayout tilTaskReminder;
    private TextInputLayout tilEventReminder;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_notifications
        );

        initViews();
        setupReminderOptions();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {

        layoutNotificationsForm =
                findViewById(
                        R.id.layoutNotificationsForm
                );

        layoutNotificationsError =
                findViewById(
                        R.id.layoutNotificationsError
                );

        progressNotificationsLoading =
                findViewById(
                        R.id.progressNotificationsLoading
                );

        progressSaveNotifications =
                findViewById(
                        R.id.progressSaveNotifications
                );

        tvNotificationsError =
                findViewById(
                        R.id.tvNotificationsError
                );

        switchNotificationsEnabled =
                findViewById(
                        R.id.switchNotificationsEnabled
                );

        actTaskReminder =
                findViewById(
                        R.id.actTaskReminder
                );

        actEventReminder =
                findViewById(
                        R.id.actEventReminder
                );

        btnSaveNotifications =
                findViewById(
                        R.id.btnSaveNotifications
                );

        btnRetryNotifications =
                findViewById(
                        R.id.btnRetryNotifications
                );

        tilTaskReminder =
                findViewById(R.id.tilTaskReminder);

        tilEventReminder =
                findViewById(R.id.tilEventReminder);
    }

    private void setupReminderOptions() {

        addReminderOption(
                getString(R.string.reminder_none),
                REMINDER_NONE
        );

        addReminderOption(
                getString(R.string.reminder_10_minutes),
                REMINDER_TEN_MINUTES
        );

        addReminderOption(
                getString(R.string.reminder_30_minutes),
                REMINDER_THIRTY_MINUTES
        );

        addReminderOption(
                getString(R.string.reminder_1_hour),
                REMINDER_ONE_HOUR
        );

        addReminderOption(
                getString(R.string.reminder_1_day),
                REMINDER_ONE_DAY
        );

        String[] labels =
                reminderLabelToValue
                        .keySet()
                        .toArray(
                                new String[0]
                        );

        ArrayAdapter<String> taskAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        labels
                );

        ArrayAdapter<String> eventAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        labels
                );

        actTaskReminder.setAdapter(
                taskAdapter
        );

        actEventReminder.setAdapter(
                eventAdapter
        );
    }

    private void addReminderOption(
            String label,
            String value
    ) {

        reminderLabelToValue.put(
                label,
                value
        );

        reminderValueToLabel.put(
                value,
                label
        );
    }

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                NotificationPreferencesViewModel.class
                        );

        viewModel
                .getPreferencesState()
                .observe(
                        this,
                        this::renderPreferencesState
                );

        viewModel
                .getUpdatePreferencesState()
                .observe(
                        this,
                        this::renderUpdateState
                );

        viewModel.loadPreferences();
    }

    private void setupListeners() {

        btnRetryNotifications.setOnClickListener(
                view -> viewModel.loadPreferences()
        );

        btnSaveNotifications.setOnClickListener(
                view -> savePreferences()
        );

        switchNotificationsEnabled.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        updateReminderFieldsEnabledState(
                                isChecked
                        )
        );
    }

    private void renderPreferencesState(
            UiState<NotificationPreferences> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                showLoading();
                break;

            case SUCCESS:
                showPreferences(
                        state.getData()
                );
                break;

            case ERROR:
                showError(
                        state.getMessage()
                );
                break;
        }
    }

    private void renderUpdateState(
            UiState<NotificationPreferences> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                setSaving(true);
                break;

            case SUCCESS:

                setSaving(false);

                Toast.makeText(
                        this,
                        R.string.notification_preferences_saved,
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case ERROR:

                setSaving(false);

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            getString(
                                    R.string.notification_preferences_update_error
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

    private void showLoading() {

        progressNotificationsLoading.setVisibility(
                View.VISIBLE
        );

        layoutNotificationsForm.setVisibility(
                View.GONE
        );

        layoutNotificationsError.setVisibility(
                View.GONE
        );
    }

    private void showPreferences(
            NotificationPreferences preferences
    ) {

        if (preferences == null) {

            showError(null);
            return;
        }

        progressNotificationsLoading.setVisibility(
                View.GONE
        );

        layoutNotificationsError.setVisibility(
                View.GONE
        );

        layoutNotificationsForm.setVisibility(
                View.VISIBLE
        );

        switchNotificationsEnabled.setChecked(
                preferences.isNotificationsEnabled()
        );

        String taskLabel =
                reminderValueToLabel.get(
                        preferences.getDefaultTaskReminder()
                );

        String eventLabel =
                reminderValueToLabel.get(
                        preferences.getDefaultEventReminder()
                );

        if (taskLabel == null) {
            taskLabel =
                    getString(
                            R.string.reminder_1_day
                    );
        }

        if (eventLabel == null) {
            eventLabel =
                    getString(
                            R.string.reminder_30_minutes
                    );
        }

        actTaskReminder.setText(
                taskLabel,
                false
        );

        actEventReminder.setText(
                eventLabel,
                false
        );

        updateReminderFieldsEnabledState(
                preferences.isNotificationsEnabled()
        );

        preferencesLoaded = true;
    }

    private void showError(
            String message
    ) {

        progressNotificationsLoading.setVisibility(
                View.GONE
        );

        layoutNotificationsForm.setVisibility(
                View.GONE
        );

        layoutNotificationsError.setVisibility(
                View.VISIBLE
        );

        if (message == null
                || message.trim().isEmpty()) {

            tvNotificationsError.setText(
                    R.string.notification_preferences_error
            );

        } else {

            tvNotificationsError.setText(
                    message
            );
        }
    }

    private void savePreferences() {

        if (!preferencesLoaded) {
            return;
        }

        String taskReminder =
                reminderLabelToValue.get(
                        actTaskReminder
                                .getText()
                                .toString()
                );

        String eventReminder =
                reminderLabelToValue.get(
                        actEventReminder
                                .getText()
                                .toString()
                );

        if (taskReminder == null) {
            taskReminder =
                    REMINDER_ONE_DAY;
        }

        if (eventReminder == null) {
            eventReminder =
                    REMINDER_THIRTY_MINUTES;
        }

        viewModel.updatePreferences(
                switchNotificationsEnabled
                        .isChecked(),
                taskReminder,
                eventReminder
        );
    }

    private void updateReminderFieldsEnabledState(
            boolean enabled
    ) {

        // Disable/enable the complete Material input containers.
        // This also disables the dropdown end icon.
        tilTaskReminder.setEnabled(enabled);
        tilEventReminder.setEnabled(enabled);

        // Disable/enable the actual dropdown fields.
        actTaskReminder.setEnabled(enabled);
        actEventReminder.setEnabled(enabled);

        actTaskReminder.setClickable(enabled);
        actEventReminder.setClickable(enabled);

        actTaskReminder.setFocusable(enabled);
        actEventReminder.setFocusable(enabled);

        // Visually indicate that reminder options are unavailable.
        float alpha = enabled ? 1.0f : 0.35f;

        tilTaskReminder.setAlpha(alpha);
        tilEventReminder.setAlpha(alpha);
    }

    private void setSaving(
            boolean saving
    ) {

        progressSaveNotifications.setVisibility(
                saving
                        ? View.VISIBLE
                        : View.GONE
        );

        btnSaveNotifications.setEnabled(
                !saving
        );

        switchNotificationsEnabled.setEnabled(
                !saving
        );

        if (saving) {

            tilTaskReminder.setEnabled(false);
            tilEventReminder.setEnabled(false);

            actTaskReminder.setEnabled(false);
            actEventReminder.setEnabled(false);

        } else {

            updateReminderFieldsEnabledState(
                    switchNotificationsEnabled.isChecked()
            );
        }
    }
}