package com.smartplanner.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.smartplanner.app.MainActivity;
import com.smartplanner.app.R;
import com.smartplanner.app.activities.event.AddEditEventActivity;
import com.smartplanner.app.activities.event.EventDetailsActivity;
import com.smartplanner.app.activities.important.ImportantActivity;
import com.smartplanner.app.activities.task.AddEditTaskActivity;
import com.smartplanner.app.activities.task.TaskDetailsActivity;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.TaskPriority;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.viewmodels.EventsViewModel;
import com.smartplanner.app.viewmodels.TasksViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int MAX_TODAY_TASKS = 2;

    // =========================================================
    // UI - HEADER
    // =========================================================

    private TextView tvHomeGreeting;
    private TextView tvHomeDate;

    // =========================================================
    // UI - SUMMARY
    // =========================================================

    private TextView tvTodayTasksCount;
    private TextView tvTodayEventsCount;
    private TextView tvOverdueCount;

    // =========================================================
    // UI - TODAY'S TASKS
    // =========================================================

    private LinearLayout layoutTodayTasksContent;
    private TextView tvViewAllTasks;

    // =========================================================
    // UI - NEXT EVENT
    // =========================================================

    private MaterialCardView cardNextEvent;
    private TextView tvNextEventTitle;
    private TextView tvNextEventInfo;

    // =========================================================
    // UI - NEXT DEADLINE
    // =========================================================

    private MaterialCardView cardUpcomingDeadline;
    private TextView tvUpcomingDeadlineTitle;
    private TextView tvUpcomingDeadlineInfo;

    // =========================================================
    // UI - IMPORTANT
    // =========================================================

    private MaterialCardView cardImportant;
    private TextView tvImportantTitle;
    private TextView tvImportantCountdown;
    private TextView tvViewAllImportant;

    // =========================================================
    // UI - DAILY PROGRESS
    // =========================================================

    private TextView tvDailyProgress;
    private TextView tvDailyProgressPercent;
    private LinearProgressIndicator progressDaily;

    // =========================================================
    // UI - QUICK ACTIONS
    // =========================================================

    private MaterialButton btnQuickAddTask;
    private MaterialButton btnQuickAddEvent;

    // =========================================================
    // DATA
    // =========================================================

    private Event nextEvent;
    private Task nextDeadlineTask;

    private Task importantTask;
    private Event importantEvent;

    private List<Task> latestTasks = new ArrayList<>();
    private List<Event> latestEvents = new ArrayList<>();

    // =========================================================
    // VIEW MODELS
    // =========================================================

    private TasksViewModel tasksViewModel;
    private EventsViewModel eventsViewModel;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModels();
        setupListeners();
        observeData();

        updateHeader();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateHeader();
        loadDashboardData();
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews(View view) {

        // HEADER

        tvHomeGreeting =
                view.findViewById(R.id.tvHomeGreeting);

        tvHomeDate =
                view.findViewById(R.id.tvHomeDate);

        // SUMMARY

        tvTodayTasksCount =
                view.findViewById(R.id.tvTodayTasksCount);

        tvTodayEventsCount =
                view.findViewById(R.id.tvTodayEventsCount);

        tvOverdueCount =
                view.findViewById(R.id.tvOverdueCount);

        // TODAY'S TASKS

        layoutTodayTasksContent =
                view.findViewById(R.id.layoutTodayTasksContent);

        tvViewAllTasks =
                view.findViewById(R.id.tvViewAllTasks);

        // NEXT EVENT

        cardNextEvent =
                view.findViewById(R.id.cardNextEvent);

        tvNextEventTitle =
                view.findViewById(R.id.tvNextEventTitle);

        tvNextEventInfo =
                view.findViewById(R.id.tvNextEventInfo);

        // NEXT DEADLINE

        cardUpcomingDeadline =
                view.findViewById(R.id.cardUpcomingDeadline);

        tvUpcomingDeadlineTitle =
                view.findViewById(R.id.tvUpcomingDeadlineTitle);

        tvUpcomingDeadlineInfo =
                view.findViewById(R.id.tvUpcomingDeadlineInfo);

        // IMPORTANT

        cardImportant =
                view.findViewById(R.id.cardImportant);

        tvImportantTitle =
                view.findViewById(R.id.tvImportantTitle);

        tvImportantCountdown =
                view.findViewById(R.id.tvImportantCountdown);

        tvViewAllImportant =
                view.findViewById(R.id.tvViewAllImportant);

        // DAILY PROGRESS

        tvDailyProgress =
                view.findViewById(R.id.tvDailyProgress);

        tvDailyProgressPercent =
                view.findViewById(R.id.tvDailyProgressPercent);

        progressDaily =
                view.findViewById(R.id.progressDaily);

        // QUICK ACTIONS

        btnQuickAddTask =
                view.findViewById(R.id.btnQuickAddTask);

        btnQuickAddEvent =
                view.findViewById(R.id.btnQuickAddEvent);
    }

    private void setupViewModels() {

        tasksViewModel =
                new ViewModelProvider(this)
                        .get(TasksViewModel.class);

        eventsViewModel =
                new ViewModelProvider(this)
                        .get(EventsViewModel.class);
    }

    private void setupListeners() {

        tvViewAllTasks.setOnClickListener(
                v -> openTasksTab()
        );

        cardNextEvent.setOnClickListener(
                v -> openNextEventDetails()
        );

        cardUpcomingDeadline.setOnClickListener(
                v -> openNextDeadlineDetails()
        );

        cardImportant.setOnClickListener(
                v -> openImportantDetails()
        );

        tvViewAllImportant.setOnClickListener(
                v -> openImportantActivity()
        );

        btnQuickAddTask.setOnClickListener(
                v -> openAddTask()
        );

        btnQuickAddEvent.setOnClickListener(
                v -> openAddEvent()
        );
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void updateHeader() {

        Calendar calendar =
                Calendar.getInstance();

        int hour =
                calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour < 12) {

            greeting = "Good morning";

        } else if (hour < 18) {

            greeting = "Good afternoon";

        } else {

            greeting = "Good evening";
        }

        tvHomeGreeting.setText(greeting);

        SimpleDateFormat dateFormatter =
                new SimpleDateFormat(
                        "EEEE, MMMM d",
                        Locale.ENGLISH
                );

        tvHomeDate.setText(
                dateFormatter.format(new Date())
        );
    }

    // =========================================================
    // NAVIGATION
    // =========================================================

    private void openTasksTab() {

        if (!(requireActivity() instanceof MainActivity)) {
            return;
        }

        BottomNavigationView bottomNavigation =
                requireActivity().findViewById(
                        R.id.bottomNavigation
                );

        if (bottomNavigation != null) {

            bottomNavigation.setSelectedItemId(
                    R.id.nav_tasks
            );
        }
    }

    private void openTaskDetails(Task task) {

        if (task == null
                || task.getId() == null
                || task.getId().trim().isEmpty()) {

            return;
        }

        Intent intent =
                new Intent(
                        requireContext(),
                        TaskDetailsActivity.class
                );

        intent.putExtra(
                TaskDetailsActivity.EXTRA_TASK_ID,
                task.getId()
        );

        startActivity(intent);
    }

    private void openNextEventDetails() {

        if (nextEvent == null
                || nextEvent.getId() == null
                || nextEvent.getId().trim().isEmpty()) {

            return;
        }

        Intent intent =
                new Intent(
                        requireContext(),
                        EventDetailsActivity.class
                );

        intent.putExtra(
                EventDetailsActivity.EXTRA_EVENT_ID,
                nextEvent.getId()
        );

        startActivity(intent);
    }

    private void openNextDeadlineDetails() {

        if (nextDeadlineTask == null
                || nextDeadlineTask.getId() == null
                || nextDeadlineTask.getId().trim().isEmpty()) {

            return;
        }

        openTaskDetails(nextDeadlineTask);
    }

    private void openImportantDetails() {

        if (importantTask != null) {

            openTaskDetails(importantTask);

            return;
        }

        if (importantEvent == null
                || importantEvent.getId() == null
                || importantEvent.getId().trim().isEmpty()) {

            return;
        }

        Intent intent =
                new Intent(
                        requireContext(),
                        EventDetailsActivity.class
                );

        intent.putExtra(
                EventDetailsActivity.EXTRA_EVENT_ID,
                importantEvent.getId()
        );

        startActivity(intent);
    }

    private void openImportantActivity() {

        Intent intent =
                new Intent(
                        requireContext(),
                        ImportantActivity.class
                );

        startActivity(intent);
    }

    private void openAddTask() {

        Intent intent =
                new Intent(
                        requireContext(),
                        AddEditTaskActivity.class
                );

        startActivity(intent);
    }

    private void openAddEvent() {

        Intent intent =
                new Intent(
                        requireContext(),
                        AddEditEventActivity.class
                );

        startActivity(intent);
    }

    // =========================================================
    // OBSERVERS
    // =========================================================

    private void observeData() {

        tasksViewModel
                .getTasksState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                latestTasks =
                                        state.getData() != null
                                                ? state.getData()
                                                : new ArrayList<>();

                                updateTaskDashboard(
                                        latestTasks
                                );

                                updateImportantSection();
                            }
                        }
                );

        eventsViewModel
                .getEventsState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                latestEvents =
                                        state.getData() != null
                                                ? state.getData()
                                                : new ArrayList<>();

                                updateEventSummary(
                                        latestEvents
                                );

                                updateImportantSection();
                            }
                        }
                );
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadDashboardData() {

        tasksViewModel.loadTasks();
        eventsViewModel.loadEvents();
    }

    // =========================================================
    // TASK DASHBOARD
    // =========================================================

    private void updateTaskDashboard(
            List<Task> tasks
    ) {

        if (tasks == null) {

            tvTodayTasksCount.setText("0");
            tvOverdueCount.setText("0");

            showTodayTasks(
                    new ArrayList<>()
            );

            nextDeadlineTask = null;

            showNoNextDeadline();
            showEmptyDailyProgress();

            return;
        }

        int overdueCount = 0;

        Date now =
                new Date();

        List<Task> todayTasks =
                new ArrayList<>();

        Task closestDeadlineTask = null;
        Date closestDeadlineDate = null;

        for (Task task : tasks) {

            if (task == null) {
                continue;
            }

            Date deadline =
                    parseSupabaseDate(
                            task.getDeadline()
                    );

            boolean completed =
                    task.getStatus()
                            == TaskStatus.COMPLETED;

            if (deadline == null) {
                continue;
            }

            // TODAY'S TASKS

            if (!completed
                    && isSameDay(
                    deadline,
                    now
            )) {

                todayTasks.add(task);
            }

            // OVERDUE

            if (!completed
                    && deadline.before(now)) {

                overdueCount++;
            }

            // NEXT DEADLINE

            if (!completed
                    && deadline.after(now)) {

                if (closestDeadlineDate == null
                        || deadline.before(
                        closestDeadlineDate
                )) {

                    closestDeadlineTask =
                            task;

                    closestDeadlineDate =
                            deadline;
                }
            }
        }

        // SORT TODAY'S TASKS

        todayTasks.sort(
                Comparator.comparing(
                        task -> {

                            Date deadline =
                                    parseSupabaseDate(
                                            task.getDeadline()
                                    );

                            return deadline != null
                                    ? deadline
                                    : new Date(
                                    Long.MAX_VALUE
                            );
                        }
                )
        );

        tvTodayTasksCount.setText(
                String.valueOf(
                        todayTasks.size()
                )
        );

        tvOverdueCount.setText(
                String.valueOf(
                        overdueCount
                )
        );

        showTodayTasks(
                todayTasks
        );

        updateDailyProgress(
                tasks
        );

        if (closestDeadlineTask != null) {

            nextDeadlineTask =
                    closestDeadlineTask;

            showNextDeadline(
                    closestDeadlineTask
            );

        } else {

            nextDeadlineTask = null;

            showNoNextDeadline();
        }
    }

    // =========================================================
    // DAILY PROGRESS
    // =========================================================

    private void updateDailyProgress(
            List<Task> tasks
    ) {

        if (tasks == null
                || tasks.isEmpty()) {

            showEmptyDailyProgress();

            return;
        }

        Date now =
                new Date();

        int totalToday = 0;
        int completedToday = 0;

        for (Task task : tasks) {

            if (task == null) {
                continue;
            }

            Date deadline =
                    parseSupabaseDate(
                            task.getDeadline()
                    );

            if (deadline == null
                    || !isSameDay(
                    deadline,
                    now
            )) {

                continue;
            }

            totalToday++;

            if (task.getStatus()
                    == TaskStatus.COMPLETED) {

                completedToday++;
            }
        }

        if (totalToday == 0) {

            showEmptyDailyProgress();

            return;
        }

        int percentage =
                Math.round(
                        completedToday
                                * 100f
                                / totalToday
                );

        tvDailyProgress.setText(
                completedToday
                        + " of "
                        + totalToday
                        + " tasks completed"
        );

        tvDailyProgressPercent.setText(
                percentage + "%"
        );

        progressDaily.setProgress(
                percentage
        );
    }

    private void showEmptyDailyProgress() {

        tvDailyProgress.setText(
                "No tasks due today"
        );

        tvDailyProgressPercent.setText(
                "0%"
        );

        progressDaily.setProgress(0);
    }

    // =========================================================
    // NEXT DEADLINE
    // =========================================================

    private void showNextDeadline(
            Task task
    ) {

        if (task == null) {

            showNoNextDeadline();

            return;
        }

        String title =
                task.getTitle();

        if (title == null
                || title.trim().isEmpty()) {

            title = "Untitled task";
        }

        tvUpcomingDeadlineTitle.setText(
                title
        );

        tvUpcomingDeadlineInfo.setText(
                formatDeadline(
                        task.getDeadline()
                )
        );
    }

    private void showNoNextDeadline() {

        tvUpcomingDeadlineTitle.setText(
                "No deadlines"
        );

        tvUpcomingDeadlineInfo.setText(
                "Upcoming deadlines appear here."
        );
    }

    private String formatDeadline(
            String deadlineValue
    ) {

        Date deadline =
                parseSupabaseDate(
                        deadlineValue
                );

        if (deadline == null) {
            return "No deadline";
        }

        Date now =
                new Date();

        SimpleDateFormat timeFormatter =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        if (isSameDay(
                deadline,
                now
        )) {

            return "Today, "
                    + timeFormatter.format(
                    deadline
            );
        }

        Calendar tomorrow =
                Calendar.getInstance();

        tomorrow.setTime(now);

        tomorrow.add(
                Calendar.DAY_OF_YEAR,
                1
        );

        if (isSameDay(
                deadline,
                tomorrow.getTime()
        )) {

            return "Tomorrow, "
                    + timeFormatter.format(
                    deadline
            );
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "MMM d, HH:mm",
                        Locale.ENGLISH
                );

        return formatter.format(
                deadline
        );
    }

    // =========================================================
    // TODAY'S TASKS
    // =========================================================

    private void showTodayTasks(
            List<Task> todayTasks
    ) {

        layoutTodayTasksContent.removeAllViews();

        if (todayTasks == null
                || todayTasks.isEmpty()) {

            showTodayTasksEmptyState();

            return;
        }

        int count =
                Math.min(
                        todayTasks.size(),
                        MAX_TODAY_TASKS
                );

        for (int i = 0; i < count; i++) {

            Task task =
                    todayTasks.get(i);

            addTodayTaskRow(
                    task
            );

            if (i < count - 1) {

                addTaskDivider();
            }
        }
    }

    private void showTodayTasksEmptyState() {

        TextView emptyView =
                new TextView(
                        requireContext()
                );

        emptyView.setText(
                "No tasks due today."
        );

        emptyView.setTextSize(
                14
        );

        emptyView.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.sp_text_secondary
                )
        );

        int verticalPadding =
                dpToPx(2);

        emptyView.setPadding(
                0,
                verticalPadding,
                0,
                verticalPadding
        );

        layoutTodayTasksContent.addView(
                emptyView
        );
    }

    private void addTodayTaskRow(
            Task task
    ) {

        LinearLayout row =
                new LinearLayout(
                        requireContext()
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dpToPx(6),
                0,
                dpToPx(6)
        );

        row.setClickable(true);
        row.setFocusable(true);

        // PRIORITY DOT

        TextView indicator =
                new TextView(
                        requireContext()
                );

        indicator.setText("●");
        indicator.setTextSize(14);

        indicator.setGravity(
                android.view.Gravity.CENTER
        );

        indicator.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        getPriorityColor(
                                task.getPriority()
                        )
                )
        );

        LinearLayout.LayoutParams indicatorParams =
                new LinearLayout.LayoutParams(
                        dpToPx(26),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        row.addView(
                indicator,
                indicatorParams
        );

        // TEXT CONTAINER

        LinearLayout textContainer =
                new LinearLayout(
                        requireContext()
                );

        textContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams textContainerParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );

        // TITLE

        TextView title =
                new TextView(
                        requireContext()
                );

        title.setText(
                task.getTitle() != null
                        && !task.getTitle().trim().isEmpty()
                        ? task.getTitle()
                        : "Untitled task"
        );

        title.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.sp_text_primary
                )
        );

        title.setTextSize(14);

        title.setTypeface(
                title.getTypeface(),
                android.graphics.Typeface.BOLD
        );

        title.setSingleLine(true);

        title.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        textContainer.addView(title);

        // META

        TextView meta =
                new TextView(
                        requireContext()
                );

        meta.setText(
                buildTaskMeta(task)
        );

        meta.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.sp_text_secondary
                )
        );

        meta.setTextSize(12);

        LinearLayout.LayoutParams metaParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        metaParams.topMargin =
                dpToPx(3);

        textContainer.addView(
                meta,
                metaParams
        );

        row.addView(
                textContainer,
                textContainerParams
        );

        // TIME

        TextView time =
                new TextView(
                        requireContext()
                );

        time.setText(
                formatTaskTime(
                        task.getDeadline()
                )
        );

        time.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.sp_teal_dark
                )
        );

        time.setTextSize(12);

        time.setTypeface(
                time.getTypeface(),
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        timeParams.leftMargin =
                dpToPx(10);

        row.addView(
                time,
                timeParams
        );

        row.setOnClickListener(
                v -> openTaskDetails(task)
        );

        layoutTodayTasksContent.addView(
                row
        );
    }

    private void addTaskDivider() {

        View divider =
                new View(
                        requireContext()
                );

        divider.setBackgroundColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.sp_border
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );

        params.leftMargin =
                dpToPx(26);

        layoutTodayTasksContent.addView(
                divider,
                params
        );
    }

    // =========================================================
    // TASK DISPLAY HELPERS
    // =========================================================

    private String buildTaskMeta(
            Task task
    ) {

        TaskPriority priority =
                task.getPriority();

        if (priority == null) {
            return "Task";
        }

        switch (priority) {

            case HIGH:
                return "High priority";

            case MEDIUM:
                return "Medium priority";

            case LOW:
                return "Low priority";

            default:
                return "Task";
        }
    }

    private int getPriorityColor(
            TaskPriority priority
    ) {

        if (priority == TaskPriority.HIGH) {
            return R.color.sp_teal_dark;
        }

        if (priority == TaskPriority.MEDIUM) {
            return R.color.sp_teal;
        }

        return R.color.sp_text_secondary;
    }

    private String formatTaskTime(
            String deadlineValue
    ) {

        Date deadline =
                parseSupabaseDate(
                        deadlineValue
                );

        if (deadline == null) {
            return "";
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        return formatter.format(deadline);
    }

    // =========================================================
    // EVENT SUMMARY
    // =========================================================

    private void updateEventSummary(
            List<Event> events
    ) {

        if (events == null) {

            tvTodayEventsCount.setText("0");

            nextEvent = null;

            showNoNextEvent();

            return;
        }

        int todayCount = 0;

        Date now =
                new Date();

        Date startOfToday =
                getStartOfDay(now);

        Date startOfTomorrow =
                getStartOfTomorrow(now);

        Event ongoingEvent = null;
        Date ongoingStart = null;

        Event upcomingEvent = null;
        Date upcomingStart = null;

        for (Event event : events) {

            if (event == null) {
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

            // TODAY

            if (start.before(startOfTomorrow)
                    && end.after(startOfToday)) {

                todayCount++;
            }

            // ONGOING

            boolean isOngoing =
                    !start.after(now)
                            && !end.before(now);

            if (isOngoing) {

                if (ongoingStart == null
                        || start.before(
                        ongoingStart
                )) {

                    ongoingEvent =
                            event;

                    ongoingStart =
                            start;
                }

                continue;
            }

            // UPCOMING

            if (start.after(now)) {

                if (upcomingStart == null
                        || start.before(
                        upcomingStart
                )) {

                    upcomingEvent =
                            event;

                    upcomingStart =
                            start;
                }
            }
        }

        tvTodayEventsCount.setText(
                String.valueOf(todayCount)
        );

        if (ongoingEvent != null) {

            nextEvent =
                    ongoingEvent;

            showNextEvent(
                    ongoingEvent,
                    true
            );

        } else if (upcomingEvent != null) {

            nextEvent =
                    upcomingEvent;

            showNextEvent(
                    upcomingEvent,
                    false
            );

        } else {

            nextEvent = null;

            showNoNextEvent();
        }
    }

    // =========================================================
    // NEXT EVENT
    // =========================================================

    private void showNextEvent(
            Event event,
            boolean ongoing
    ) {

        if (event == null) {

            showNoNextEvent();

            return;
        }

        String title =
                event.getTitle();

        if (title == null
                || title.trim().isEmpty()) {

            title = "Untitled event";
        }

        tvNextEventTitle.setText(
                title
        );

        StringBuilder info =
                new StringBuilder();

        if (ongoing) {

            info.append("Ongoing");

        } else {

            String dateTime =
                    formatEventStart(
                            event.getStartAt()
                    );

            if (!dateTime.isEmpty()) {

                info.append(dateTime);
            }
        }

        String location =
                event.getLocation();

        if (location != null
                && !location.trim().isEmpty()) {

            if (info.length() > 0) {

                info.append(" • ");
            }

            info.append(
                    location.trim()
            );
        }

        if (info.length() == 0) {

            info.append(
                    "Event details"
            );
        }

        tvNextEventInfo.setText(
                info.toString()
        );
    }

    private void showNoNextEvent() {

        tvNextEventTitle.setText(
                "No upcoming events"
        );

        tvNextEventInfo.setText(
                "Your next event will appear here."
        );
    }

    private String formatEventStart(
            String startValue
    ) {

        Date start =
                parseSupabaseDate(
                        startValue
                );

        if (start == null) {
            return "";
        }

        Date now =
                new Date();

        SimpleDateFormat timeFormatter =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        if (isSameDay(
                start,
                now
        )) {

            return "Today, "
                    + timeFormatter.format(
                    start
            );
        }

        Calendar tomorrow =
                Calendar.getInstance();

        tomorrow.setTime(now);

        tomorrow.add(
                Calendar.DAY_OF_YEAR,
                1
        );

        if (isSameDay(
                start,
                tomorrow.getTime()
        )) {

            return "Tomorrow, "
                    + timeFormatter.format(
                    start
            );
        }

        SimpleDateFormat dateFormatter =
                new SimpleDateFormat(
                        "MMM d, HH:mm",
                        Locale.ENGLISH
                );

        return dateFormatter.format(start);
    }

    // =========================================================
    // IMPORTANT
    // =========================================================

    private void updateImportantSection() {

        Date now =
                new Date();

        Task closestImportantTask = null;
        Date closestImportantTaskDate = null;

        Task importantTaskWithoutDeadline = null;

        // IMPORTANT TASKS

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

            if (deadline == null) {

                if (importantTaskWithoutDeadline == null) {

                    importantTaskWithoutDeadline =
                            task;
                }

                continue;
            }

            if (closestImportantTaskDate == null
                    || deadline.before(
                    closestImportantTaskDate
            )) {

                closestImportantTask =
                        task;

                closestImportantTaskDate =
                        deadline;
            }
        }

        // IMPORTANT EVENTS

        Event closestImportantEvent = null;
        Date closestImportantEventDate = null;

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

            // Finished events are ignored

            if (end.before(now)) {

                continue;
            }

            Date relevantDate =
                    start.before(now)
                            ? now
                            : start;

            if (closestImportantEventDate == null
                    || relevantDate.before(
                    closestImportantEventDate
            )) {

                closestImportantEvent =
                        event;

                closestImportantEventDate =
                        relevantDate;
            }
        }

        // COMPARE TASK AND EVENT

        if (closestImportantTask != null
                && closestImportantEvent != null) {

            if (closestImportantTaskDate.before(
                    closestImportantEventDate
            )) {

                showImportantTask(
                        closestImportantTask
                );

            } else {

                showImportantEvent(
                        closestImportantEvent
                );
            }

            return;
        }

        if (closestImportantTask != null) {

            showImportantTask(
                    closestImportantTask
            );

            return;
        }

        if (closestImportantEvent != null) {

            showImportantEvent(
                    closestImportantEvent
            );

            return;
        }

        if (importantTaskWithoutDeadline != null) {

            showImportantTask(
                    importantTaskWithoutDeadline
            );

            return;
        }

        showNoImportantItem();
    }

    private void showImportantTask(
            Task task
    ) {

        importantTask =
                task;

        importantEvent =
                null;

        String title =
                task.getTitle();

        if (title == null
                || title.trim().isEmpty()) {

            title = "Untitled task";
        }

        tvImportantTitle.setText(
                title
        );

        Date deadline =
                parseSupabaseDate(
                        task.getDeadline()
                );

        if (deadline == null) {

            tvImportantCountdown.setText(
                    "Important task • No deadline"
            );

            return;
        }

        tvImportantCountdown.setText(
                buildCountdownText(
                        deadline,
                        true
                )
        );
    }

    private void showImportantEvent(
            Event event
    ) {

        importantEvent =
                event;

        importantTask =
                null;

        String title =
                event.getTitle();

        if (title == null
                || title.trim().isEmpty()) {

            title = "Untitled event";
        }

        tvImportantTitle.setText(
                title
        );

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        Date now =
                new Date();

        if (start != null
                && end != null
                && !start.after(now)
                && !end.before(now)) {

            tvImportantCountdown.setText(
                    "Event is ongoing"
            );

            return;
        }

        if (start == null) {

            tvImportantCountdown.setText(
                    "Important event"
            );

            return;
        }

        tvImportantCountdown.setText(
                buildCountdownText(
                        start,
                        false
                )
        );
    }

    private void showNoImportantItem() {

        importantTask = null;
        importantEvent = null;

        tvImportantTitle.setText(
                "No important items"
        );

        tvImportantCountdown.setText(
                "Important tasks and events will appear here."
        );
    }

    // =========================================================
    // COUNTDOWN
    // =========================================================

    private String buildCountdownText(
            Date targetDate,
            boolean task
    ) {

        if (targetDate == null) {
            return "";
        }

        Date now =
                new Date();

        long difference =
                targetDate.getTime()
                        - now.getTime();

        // OVERDUE

        if (difference < 0) {

            long overdueMinutes =
                    Math.abs(difference)
                            / (60 * 1000);

            long overdueDays =
                    overdueMinutes
                            / (24 * 60);

            if (overdueDays > 0) {

                return "Overdue by "
                        + overdueDays
                        + (
                        overdueDays == 1
                                ? " day"
                                : " days"
                );
            }

            long overdueHours =
                    overdueMinutes / 60;

            if (overdueHours > 0) {

                return "Overdue by "
                        + overdueHours
                        + (
                        overdueHours == 1
                                ? " hour"
                                : " hours"
                );
            }

            return "Overdue";
        }

        // FUTURE

        long totalMinutes =
                difference
                        / (60 * 1000);

        long days =
                totalMinutes
                        / (24 * 60);

        long hours =
                (
                        totalMinutes
                                % (24 * 60)
                ) / 60;

        long minutes =
                totalMinutes % 60;

        String prefix =
                task
                        ? "Task due in "
                        : "Event starts in ";

        if (days > 0) {

            return prefix
                    + days
                    + (
                    days == 1
                            ? " day"
                            : " days"
            )
                    + (
                    hours > 0
                            ? " " + hours + " h"
                            : ""
            );
        }

        if (hours > 0) {

            return prefix
                    + hours
                    + " h "
                    + minutes
                    + " min";
        }

        return prefix
                + Math.max(
                minutes,
                1
        )
                + " min";
    }

    // =========================================================
    // DATE HELPERS
    // =========================================================

    private boolean isSameDay(
            Date first,
            Date second
    ) {

        Calendar firstCalendar =
                Calendar.getInstance();

        firstCalendar.setTime(
                first
        );

        Calendar secondCalendar =
                Calendar.getInstance();

        secondCalendar.setTime(
                second
        );

        return firstCalendar.get(
                Calendar.YEAR
        )
                == secondCalendar.get(
                Calendar.YEAR
        )
                && firstCalendar.get(
                Calendar.DAY_OF_YEAR
        )
                == secondCalendar.get(
                Calendar.DAY_OF_YEAR
        );
    }

    private Date getStartOfDay(
            Date date
    ) {

        Calendar calendar =
                Calendar.getInstance();

        calendar.setTime(date);

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

    private Date getStartOfTomorrow(
            Date date
    ) {

        Calendar calendar =
                Calendar.getInstance();

        calendar.setTime(
                getStartOfDay(date)
        );

        calendar.add(
                Calendar.DAY_OF_YEAR,
                1
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

                return parser.parse(
                        value
                );

            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    // =========================================================
    // DIMENSIONS
    // =========================================================

    private int dpToPx(
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