package com.smartplanner.app.activities.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.models.NotificationPreferences;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.viewmodels.NotificationPreferencesViewModel;

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

    private TextView tvNotificationHeroTitle;
    private TextView tvNotificationHeroDescription;

    private ImageView ivNotificationHeroIcon;

    private MaterialCardView cardNotificationHero;
    private MaterialCardView cardTaskReminder;
    private MaterialCardView cardEventReminder;

    private MaterialSwitch switchNotificationsEnabled;

    private TextInputLayout tilTaskReminder;
    private TextInputLayout tilEventReminder;

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

        cardNotificationHero =
                findViewById(
                        R.id.cardNotificationHero
                );

        tvNotificationHeroTitle =
                findViewById(
                        R.id.tvNotificationHeroTitle
                );

        tvNotificationHeroDescription =
                findViewById(
                        R.id.tvNotificationHeroDescription
                );

        ivNotificationHeroIcon =
                findViewById(
                        R.id.ivNotificationHeroIcon
                );

        cardTaskReminder =
                findViewById(
                        R.id.cardTaskReminder
                );

        cardEventReminder =
                findViewById(
                        R.id.cardEventReminder
                );

        switchNotificationsEnabled =
                findViewById(
                        R.id.switchNotificationsEnabled
                );

        tilTaskReminder =
                findViewById(
                        R.id.tilTaskReminder
                );

        tilEventReminder =
                findViewById(
                        R.id.tilEventReminder
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
                view ->
                        viewModel.loadPreferences()
        );

        btnSaveNotifications.setOnClickListener(
                view ->
                        savePreferences()
        );

        switchNotificationsEnabled.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    updateNotificationUi(
                            isChecked
                    );
                }
        );
    }

    private void updateNotificationUi(
            boolean enabled
    ) {

        updateHeroState(enabled);

        updateReminderFieldsEnabledState(
                enabled
        );
    }

    private void updateHeroState(
            boolean enabled
    ) {

        if (enabled) {

            tvNotificationHeroTitle.setText(
                    R.string.notifications_hero_title
            );

            tvNotificationHeroDescription.setText(
                    R.string.notifications_hero_description
            );

            ivNotificationHeroIcon.setImageTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    this,
                                    R.color.sp_teal_deep
                            )
                    )
            );

            cardNotificationHero.setAlpha(
                    1.0f
            );

        } else {

            tvNotificationHeroTitle.setText(
                    R.string.notifications_off_title
            );

            tvNotificationHeroDescription.setText(
                    R.string.notifications_off_description
            );

            ivNotificationHeroIcon.setImageTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    this,
                                    R.color.sp_text_hint
                            )
                    )
            );

            cardNotificationHero.setAlpha(
                    0.82f
            );
        }
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

        updateNotificationUi(
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

        tilTaskReminder.setEnabled(
                enabled
        );

        tilEventReminder.setEnabled(
                enabled
        );

        actTaskReminder.setEnabled(
                enabled
        );

        actEventReminder.setEnabled(
                enabled
        );

        actTaskReminder.setClickable(
                enabled
        );

        actEventReminder.setClickable(
                enabled
        );

        actTaskReminder.setFocusable(
                enabled
        );

        actEventReminder.setFocusable(
                enabled
        );

        float cardAlpha =
                enabled
                        ? 1.0f
                        : 0.42f;

        cardTaskReminder.setAlpha(
                cardAlpha
        );

        cardEventReminder.setAlpha(
                cardAlpha
        );
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

            tilTaskReminder.setEnabled(
                    false
            );

            tilEventReminder.setEnabled(
                    false
            );

            actTaskReminder.setEnabled(
                    false
            );

            actEventReminder.setEnabled(
                    false
            );

            cardTaskReminder.setAlpha(
                    0.42f
            );

            cardEventReminder.setAlpha(
                    0.42f
            );

        } else {

            updateNotificationUi(
                    switchNotificationsEnabled
                            .isChecked()
            );
        }
    }
}