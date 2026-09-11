package com.smartplanner.app.activities.important;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.event.EventDetailsActivity;
import com.smartplanner.app.activities.task.TaskDetailsActivity;
import com.smartplanner.app.adapters.ImportantAdapter;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.ImportantItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.viewmodels.EventsViewModel;
import com.smartplanner.app.viewmodels.TasksViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImportantActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private TextView tvImportantCount;

    private RecyclerView recyclerImportant;

    private ProgressBar progressImportant;

    private LinearLayout layoutImportantEmpty;
    private LinearLayout layoutImportantError;

    private TextView tvImportantError;

    private MaterialButton btnRetryImportant;

    // =========================================================
    // ADAPTER
    // =========================================================

    private ImportantAdapter adapter;

    // =========================================================
    // VIEW MODELS
    // =========================================================

    private TasksViewModel tasksViewModel;
    private EventsViewModel eventsViewModel;

    // =========================================================
    // DATA
    // =========================================================

    private List<Task> latestTasks =
            new ArrayList<>();

    private List<Event> latestEvents =
            new ArrayList<>();

    private boolean tasksLoaded =
            false;

    private boolean eventsLoaded =
            false;

    private boolean taskLoadError =
            false;

    private boolean eventLoadError =
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
                R.layout.activity_important
        );

        initViews();
        setupRecyclerView();
        setupViewModels();
        setupListeners();
        observeData();

        loadData();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (tasksViewModel != null
                && eventsViewModel != null) {

            loadData();
        }
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        findViewById(
                R.id.btnBackImportant
        ).setOnClickListener(
                v -> finish()
        );

        tvImportantCount =
                findViewById(
                        R.id.tvImportantCount
                );

        recyclerImportant =
                findViewById(
                        R.id.recyclerImportant
                );

        progressImportant =
                findViewById(
                        R.id.progressImportant
                );

        layoutImportantEmpty =
                findViewById(
                        R.id.layoutImportantEmpty
                );

        layoutImportantError =
                findViewById(
                        R.id.layoutImportantError
                );

        tvImportantError =
                findViewById(
                        R.id.tvImportantError
                );

        btnRetryImportant =
                findViewById(
                        R.id.btnRetryImportant
                );
    }

    private void setupRecyclerView() {

        adapter =
                new ImportantAdapter(
                        this::openImportantItem
                );

        recyclerImportant.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );

        recyclerImportant.setAdapter(
                adapter
        );
    }

    private void setupViewModels() {

        tasksViewModel =
                new ViewModelProvider(this)
                        .get(
                                TasksViewModel.class
                        );

        eventsViewModel =
                new ViewModelProvider(this)
                        .get(
                                EventsViewModel.class
                        );
    }

    private void setupListeners() {

        btnRetryImportant.setOnClickListener(
                v -> loadData()
        );
    }

    // =========================================================
    // OBSERVERS
    // =========================================================

    private void observeData() {

        tasksViewModel
                .getTasksState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.LOADING) {

                                tasksLoaded =
                                        false;

                                taskLoadError =
                                        false;

                                updateLoadingState();

                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                latestTasks =
                                        state.getData() != null
                                                ? state.getData()
                                                : new ArrayList<>();

                                tasksLoaded =
                                        true;

                                taskLoadError =
                                        false;

                                updateContentIfReady();

                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.ERROR) {

                                tasksLoaded =
                                        true;

                                taskLoadError =
                                        true;

                                updateContentIfReady();
                            }
                        }
                );

        eventsViewModel
                .getEventsState()
                .observe(
                        this,
                        state -> {

                            if (state == null) {
                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.LOADING) {

                                eventsLoaded =
                                        false;

                                eventLoadError =
                                        false;

                                updateLoadingState();

                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                latestEvents =
                                        state.getData() != null
                                                ? state.getData()
                                                : new ArrayList<>();

                                eventsLoaded =
                                        true;

                                eventLoadError =
                                        false;

                                updateContentIfReady();

                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.ERROR) {

                                eventsLoaded =
                                        true;

                                eventLoadError =
                                        true;

                                updateContentIfReady();
                            }
                        }
                );
    }

    // =========================================================
    // LOAD
    // =========================================================

    private void loadData() {

        tasksLoaded =
                false;

        eventsLoaded =
                false;

        taskLoadError =
                false;

        eventLoadError =
                false;

        showLoading();

        tasksViewModel.loadTasks();
        eventsViewModel.loadEvents();
    }

    private void updateLoadingState() {

        if (!tasksLoaded
                || !eventsLoaded) {

            showLoading();
        }
    }

    private void updateContentIfReady() {

        if (!tasksLoaded
                || !eventsLoaded) {

            return;
        }

        if (taskLoadError
                || eventLoadError) {

            showError();

            return;
        }

        List<ImportantItem> importantItems =
                buildImportantItems();

        showContent(
                importantItems
        );
    }

    // =========================================================
    // BUILD IMPORTANT ITEMS
    // =========================================================

    private List<ImportantItem> buildImportantItems() {

        List<ImportantItem> result =
                new ArrayList<>();

        Date now =
                new Date();

        // =====================================================
        // TASKS
        // =====================================================

        for (Task task : latestTasks) {

            if (task == null
                    || !task.isImportant()) {

                continue;
            }

            if (task.getStatus()
                    == TaskStatus.COMPLETED) {

                continue;
            }

            Date deadline =
                    parseSupabaseDate(
                            task.getDeadline()
                    );

            result.add(
                    ImportantItem.fromTask(
                            task,
                            deadline
                    )
            );
        }

        // =====================================================
        // EVENTS
        // =====================================================

        for (Event event : latestEvents) {

            if (event == null
                    || !event.isImportant()) {

                continue;
            }

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

                continue;
            }

            /*
             * Finished events no longer belong in the active
             * Important list.
             */
            if (end.before(
                    now
            )) {

                continue;
            }

            Date sortDate =
                    start.before(
                            now
                    )
                            ? now
                            : start;

            result.add(
                    ImportantItem.fromEvent(
                            event,
                            sortDate
                    )
            );
        }

        // =====================================================
        // SORT
        // =====================================================

        result.sort(
                new Comparator<ImportantItem>() {

                    @Override
                    public int compare(
                            ImportantItem first,
                            ImportantItem second
                    ) {

                        Date firstDate =
                                first.getSortDate();

                        Date secondDate =
                                second.getSortDate();

                        if (firstDate == null
                                && secondDate == null) {

                            return compareTitles(
                                    first,
                                    second
                            );
                        }

                        if (firstDate == null) {
                            return 1;
                        }

                        if (secondDate == null) {
                            return -1;
                        }

                        int dateComparison =
                                firstDate.compareTo(
                                        secondDate
                                );

                        if (dateComparison != 0) {

                            return dateComparison;
                        }

                        return compareTitles(
                                first,
                                second
                        );
                    }
                }
        );

        return result;
    }

    private int compareTitles(
            ImportantItem first,
            ImportantItem second
    ) {

        String firstTitle =
                first.getTitle();

        String secondTitle =
                second.getTitle();

        if (firstTitle == null) {
            firstTitle = "";
        }

        if (secondTitle == null) {
            secondTitle = "";
        }

        return firstTitle.compareToIgnoreCase(
                secondTitle
        );
    }

    // =========================================================
    // UI STATE
    // =========================================================

    private void showLoading() {

        progressImportant.setVisibility(
                View.VISIBLE
        );

        recyclerImportant.setVisibility(
                View.GONE
        );

        layoutImportantEmpty.setVisibility(
                View.GONE
        );

        layoutImportantError.setVisibility(
                View.GONE
        );
    }

    private void showContent(
            List<ImportantItem> items
    ) {

        progressImportant.setVisibility(
                View.GONE
        );

        layoutImportantError.setVisibility(
                View.GONE
        );

        int count =
                items != null
                        ? items.size()
                        : 0;

        tvImportantCount.setText(
                count
                        + (
                        count == 1
                                ? " important item"
                                : " important items"
                )
        );

        if (items == null
                || items.isEmpty()) {

            adapter.setItems(
                    new ArrayList<>()
            );

            recyclerImportant.setVisibility(
                    View.GONE
            );

            layoutImportantEmpty.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        layoutImportantEmpty.setVisibility(
                View.GONE
        );

        recyclerImportant.setVisibility(
                View.VISIBLE
        );

        adapter.setItems(
                items
        );
    }

    private void showError() {

        progressImportant.setVisibility(
                View.GONE
        );

        recyclerImportant.setVisibility(
                View.GONE
        );

        layoutImportantEmpty.setVisibility(
                View.GONE
        );

        layoutImportantError.setVisibility(
                View.VISIBLE
        );

        tvImportantError.setText(
                "Unable to load important items."
        );

        tvImportantCount.setText(
                "Important items"
        );
    }

    // =========================================================
    // NAVIGATION
    // =========================================================

    private void openImportantItem(
            ImportantItem item
    ) {

        if (item == null) {
            return;
        }

        if (item.getType()
                == ImportantItem.Type.TASK) {

            Task task =
                    item.getTask();

            if (task == null
                    || task.getId() == null
                    || task.getId().trim().isEmpty()) {

                return;
            }

            Intent intent =
                    new Intent(
                            this,
                            TaskDetailsActivity.class
                    );

            intent.putExtra(
                    TaskDetailsActivity.EXTRA_TASK_ID,
                    task.getId()
            );

            startActivity(
                    intent
            );

            return;
        }

        Event event =
                item.getEvent();

        if (event == null
                || event.getId() == null
                || event.getId().trim().isEmpty()) {

            return;
        }

        Intent intent =
                new Intent(
                        this,
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

    // =========================================================
    // DATE
    // =========================================================

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