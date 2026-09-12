package com.smartplanner.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.smartplanner.app.R;
import com.smartplanner.app.models.CategoryTaskCount;
import com.smartplanner.app.models.DailyTaskCount;
import com.smartplanner.app.models.StatisticsSummary;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.StatisticsPeriod;
import com.smartplanner.app.viewmodels.StatisticsViewModel;

import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;

    private MaterialButtonToggleGroup toggleStatisticsPeriod;
    private MaterialButton btnStatisticsWeek;
    private MaterialButton btnStatisticsMonth;

    private NestedScrollView scrollStatistics;

    private View progressStatistics;
    private LinearLayout layoutStatisticsError;
    private TextView tvStatisticsError;
    private MaterialButton btnRetryStatistics;

    private TextView tvStatisticsTotal;
    private TextView tvStatisticsCompleted;
    private TextView tvStatisticsActive;
    private TextView tvStatisticsOverdue;

    private TextView tvStatisticsCompletionRate;
    private LinearProgressIndicator progressStatisticsCompletion;

    private LinearLayout layoutStatisticsDailyChart;
    private LinearLayout layoutStatisticsCategories;

    private TextView tvStatisticsProductiveDay;
    private TextView tvStatisticsAverage;

    private UiState.Status summaryStatus;
    private UiState.Status dailyStatus;
    private UiState.Status categoryStatus;

    public StatisticsFragment() {
        super(R.layout.fragment_statistics);
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

        initViews(view);
        setupViewModel();
        setupListeners();
        observeStatistics();

        statisticsViewModel.loadStatistics();
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews(
            View view
    ) {

        toggleStatisticsPeriod =
                view.findViewById(
                        R.id.toggleStatisticsPeriod
                );

        btnStatisticsWeek =
                view.findViewById(
                        R.id.btnStatisticsWeek
                );

        btnStatisticsMonth =
                view.findViewById(
                        R.id.btnStatisticsMonth
                );

        scrollStatistics =
                view.findViewById(
                        R.id.scrollStatistics
                );

        progressStatistics =
                view.findViewById(
                        R.id.progressStatistics
                );

        layoutStatisticsError =
                view.findViewById(
                        R.id.layoutStatisticsError
                );

        tvStatisticsError =
                view.findViewById(
                        R.id.tvStatisticsError
                );

        btnRetryStatistics =
                view.findViewById(
                        R.id.btnRetryStatistics
                );

        tvStatisticsTotal =
                view.findViewById(
                        R.id.tvStatisticsTotal
                );

        tvStatisticsCompleted =
                view.findViewById(
                        R.id.tvStatisticsCompleted
                );

        tvStatisticsActive =
                view.findViewById(
                        R.id.tvStatisticsActive
                );

        tvStatisticsOverdue =
                view.findViewById(
                        R.id.tvStatisticsOverdue
                );

        tvStatisticsCompletionRate =
                view.findViewById(
                        R.id.tvStatisticsCompletionRate
                );

        progressStatisticsCompletion =
                view.findViewById(
                        R.id.progressStatisticsCompletion
                );

        layoutStatisticsDailyChart =
                view.findViewById(
                        R.id.layoutStatisticsDailyChart
                );

        layoutStatisticsCategories =
                view.findViewById(
                        R.id.layoutStatisticsCategories
                );

        tvStatisticsProductiveDay =
                view.findViewById(
                        R.id.tvStatisticsProductiveDay
                );

        tvStatisticsAverage =
                view.findViewById(
                        R.id.tvStatisticsAverage
                );
    }

    private void setupViewModel() {

        statisticsViewModel =
                new ViewModelProvider(this)
                        .get(
                                StatisticsViewModel.class
                        );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        toggleStatisticsPeriod.addOnButtonCheckedListener(
                (
                        group,
                        checkedId,
                        isChecked
                ) -> {

                    if (!isChecked) {
                        return;
                    }

                    if (checkedId
                            == R.id.btnStatisticsWeek) {

                        statisticsViewModel.setPeriod(
                                StatisticsPeriod.WEEK
                        );

                    } else if (checkedId
                            == R.id.btnStatisticsMonth) {

                        statisticsViewModel.setPeriod(
                                StatisticsPeriod.MONTH
                        );
                    }

                    /*
                     * Kada korisnik promijeni period,
                     * vraćamo Statistics sadržaj na vrh.
                     *
                     * Header i period selector ostaju fiksni,
                     * pa korisnik odmah vidi nove summary podatke.
                     */
                    scrollStatistics.post(
                            () ->
                                    scrollStatistics.smoothScrollTo(
                                            0,
                                            0
                                    )
                    );
                }
        );

        btnRetryStatistics.setOnClickListener(
                v ->
                        statisticsViewModel.loadStatistics()
        );
    }

    // =========================================================
    // OBSERVERS
    // =========================================================

    private void observeStatistics() {

        statisticsViewModel
                .getSummaryState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            summaryStatus =
                                    state.getStatus();

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                StatisticsSummary summary =
                                        state.getData();

                                if (summary != null) {

                                    showSummary(
                                            summary
                                    );
                                }

                            } else if (state.getStatus()
                                    == UiState.Status.ERROR) {

                                showStatisticsError(
                                        state.getMessage()
                                );
                            }

                            updateScreenState();
                        }
                );

        statisticsViewModel
                .getDailyCountsState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            dailyStatus =
                                    state.getStatus();

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                showDailyChart(
                                        state.getData()
                                );

                            } else if (state.getStatus()
                                    == UiState.Status.ERROR) {

                                showStatisticsError(
                                        state.getMessage()
                                );
                            }

                            updateScreenState();
                        }
                );

        statisticsViewModel
                .getCategoryCountsState()
                .observe(
                        getViewLifecycleOwner(),
                        state -> {

                            if (state == null) {
                                return;
                            }

                            categoryStatus =
                                    state.getStatus();

                            if (state.getStatus()
                                    == UiState.Status.SUCCESS) {

                                showCategories(
                                        state.getData()
                                );

                            } else if (state.getStatus()
                                    == UiState.Status.ERROR) {

                                showCategoryError(
                                        state.getMessage()
                                );
                            }

                            updateScreenState();
                        }
                );
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    private void showSummary(
            StatisticsSummary summary
    ) {

        tvStatisticsTotal.setText(
                String.valueOf(
                        summary.getTotalTasks()
                )
        );

        tvStatisticsCompleted.setText(
                String.valueOf(
                        summary.getCompletedTasks()
                )
        );

        tvStatisticsActive.setText(
                String.valueOf(
                        summary.getActiveTasks()
                )
        );

        tvStatisticsOverdue.setText(
                String.valueOf(
                        summary.getOverdueTasks()
                )
        );

        Double completionRate =
                summary.getCompletionRate();

        if (completionRate == null) {

            tvStatisticsCompletionRate.setText(
                    "N/A"
            );

            progressStatisticsCompletion.setProgress(
                    0
            );

        } else {

            int roundedRate =
                    (int) Math.round(
                            completionRate
                    );

            tvStatisticsCompletionRate.setText(
                    roundedRate + "%"
            );

            progressStatisticsCompletion.setProgress(
                    roundedRate
            );
        }

        String productiveDay =
                summary.getMostProductiveDay();

        tvStatisticsProductiveDay.setText(
                productiveDay == null
                        || productiveDay.trim().isEmpty()
                        ? "N/A"
                        : productiveDay
        );

        tvStatisticsAverage.setText(
                String.format(
                        Locale.ENGLISH,
                        "%.1f",
                        summary.getAverageCompletedPerDay()
                )
        );
    }

    // =========================================================
    // DAILY CHART
    // =========================================================

    private void showDailyChart(
            List<DailyTaskCount> dailyCounts
    ) {

        layoutStatisticsDailyChart.removeAllViews();

        if (dailyCounts == null
                || dailyCounts.isEmpty()) {

            addEmptyText(
                    layoutStatisticsDailyChart,
                    "No completed tasks yet."
            );

            return;
        }

        int maxCount = 0;

        for (DailyTaskCount item : dailyCounts) {

            if (item != null
                    && item.getCount() > maxCount) {

                maxCount =
                        item.getCount();
            }
        }

        for (DailyTaskCount item : dailyCounts) {

            if (item == null) {
                continue;
            }

            addDailyBar(
                    item,
                    maxCount
            );
        }
    }

    private void addDailyBar(
            DailyTaskCount item,
            int maxCount
    ) {

        boolean weekMode =
                statisticsViewModel.getSelectedPeriod()
                        == StatisticsPeriod.WEEK;

        LinearLayout column =
                new LinearLayout(
                        requireContext()
                );

        column.setOrientation(
                LinearLayout.VERTICAL
        );

        column.setGravity(
                Gravity.BOTTOM
                        | Gravity.CENTER_HORIZONTAL
        );

        LinearLayout.LayoutParams columnParams;

        if (weekMode) {

            /*
             * WEEK:
             * svih 7 dana ravnomjerno popunjavaju
             * cijelu dostupnu širinu.
             */
            columnParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1f
                    );

        } else {

            /*
             * MONTH:
             * dani ostaju fiksne širine pa se
             * graf može horizontalno scrollati.
             */
            columnParams =
                    new LinearLayout.LayoutParams(
                            dp(40),
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );

            columnParams.setMargins(
                    dp(2),
                    0,
                    dp(2),
                    0
            );
        }

        column.setLayoutParams(
                columnParams
        );

        // -----------------------------------------------------
        // COUNT
        // -----------------------------------------------------

        TextView countText =
                new TextView(
                        requireContext()
                );

        countText.setText(
                String.valueOf(
                        item.getCount()
                )
        );

        countText.setTextSize(
                weekMode
                        ? 10
                        : 9
        );

        countText.setTextColor(
                getResources().getColor(
                        R.color.sp_text_secondary,
                        requireContext().getTheme()
                )
        );

        countText.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(20)
                );

        countText.setLayoutParams(
                countParams
        );

        column.addView(
                countText
        );

        // -----------------------------------------------------
        // BAR
        // -----------------------------------------------------

        View bar =
                new View(
                        requireContext()
                );

        int maxBarHeight =
                dp(78);

        int minBarHeight =
                dp(4);

        int barHeight;

        if (maxCount <= 0
                || item.getCount() <= 0) {

            barHeight =
                    minBarHeight;

        } else {

            barHeight =
                    Math.max(
                            minBarHeight,
                            (int) (
                                    maxBarHeight
                                            * (
                                            item.getCount()
                                                    / (double) maxCount
                                    )
                            )
                    );
        }

        int barWidth =
                weekMode
                        ? dp(22)
                        : dp(14);

        LinearLayout.LayoutParams barParams =
                new LinearLayout.LayoutParams(
                        barWidth,
                        barHeight
                );

        barParams.gravity =
                Gravity.CENTER_HORIZONTAL;

        bar.setLayoutParams(
                barParams
        );

        bar.setBackgroundColor(
                getResources().getColor(
                        R.color.sp_teal,
                        requireContext().getTheme()
                )
        );

        column.addView(
                bar
        );

        // -----------------------------------------------------
        // LABEL
        // -----------------------------------------------------

        TextView labelText =
                new TextView(
                        requireContext()
                );

        labelText.setText(
                item.getLabel()
        );

        labelText.setTextSize(
                weekMode
                        ? 11
                        : 9
        );

        labelText.setTextColor(
                getResources().getColor(
                        R.color.sp_text_secondary,
                        requireContext().getTheme()
                )
        );

        labelText.setGravity(
                Gravity.CENTER
        );

        labelText.setMaxLines(
                1
        );

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(26)
                );

        labelText.setLayoutParams(
                labelParams
        );

        column.addView(
                labelText
        );

        layoutStatisticsDailyChart.addView(
                column
        );
    }

    // =========================================================
    // CATEGORY COUNTS
    // =========================================================

    private void showCategories(
            List<CategoryTaskCount> categoryCounts
    ) {

        layoutStatisticsCategories.removeAllViews();

        if (categoryCounts == null
                || categoryCounts.isEmpty()) {

            addEmptyText(
                    layoutStatisticsCategories,
                    "No categories available."
            );

            return;
        }

        int totalTasks = 0;

        for (CategoryTaskCount item
                : categoryCounts) {

            if (item != null) {

                totalTasks +=
                        item.getTaskCount();
            }
        }

        for (CategoryTaskCount item
                : categoryCounts) {

            if (item == null) {
                continue;
            }

            addCategoryRow(
                    item,
                    totalTasks
            );
        }
    }

    private void addCategoryRow(
            CategoryTaskCount item,
            int totalTasks
    ) {

        LinearLayout container =
                new LinearLayout(
                        requireContext()
                );

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        containerParams.setMargins(
                0,
                dp(5),
                0,
                dp(10)
        );

        container.setLayoutParams(
                containerParams
        );

        LinearLayout row =
                new LinearLayout(
                        requireContext()
                );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        // -----------------------------------------------------
        // COLOR INDICATOR
        // -----------------------------------------------------

        View colorDot =
                new View(
                        requireContext()
                );

        LinearLayout.LayoutParams dotParams =
                new LinearLayout.LayoutParams(
                        dp(10),
                        dp(10)
                );

        dotParams.setMargins(
                0,
                0,
                dp(10),
                0
        );

        colorDot.setLayoutParams(
                dotParams
        );

        int categoryColor =
                getCategoryColor(
                        item.getCategoryColor()
                );

        colorDot.setBackgroundColor(
                categoryColor
        );

        row.addView(
                colorDot
        );

        // -----------------------------------------------------
        // CATEGORY NAME
        // -----------------------------------------------------

        TextView nameText =
                new TextView(
                        requireContext()
                );

        String categoryName =
                item.getCategoryName();

        nameText.setText(
                categoryName == null
                        || categoryName.trim().isEmpty()
                        ? "Uncategorized"
                        : categoryName
        );

        nameText.setTextSize(
                14
        );

        nameText.setTextColor(
                getResources().getColor(
                        R.color.sp_text_primary,
                        requireContext().getTheme()
                )
        );

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        nameText.setLayoutParams(
                nameParams
        );

        row.addView(
                nameText
        );

        // -----------------------------------------------------
        // TASK COUNT
        // -----------------------------------------------------

        TextView countText =
                new TextView(
                        requireContext()
                );

        countText.setText(
                String.valueOf(
                        item.getTaskCount()
                )
        );

        countText.setTextSize(
                14
        );

        countText.setTextColor(
                getResources().getColor(
                        R.color.sp_text_secondary,
                        requireContext().getTheme()
                )
        );

        row.addView(
                countText
        );

        container.addView(
                row
        );

        // -----------------------------------------------------
        // CATEGORY PROGRESS
        // -----------------------------------------------------

        LinearProgressIndicator progress =
                new LinearProgressIndicator(
                        requireContext()
                );

        progress.setMax(
                100
        );

        int percentage = 0;

        if (totalTasks > 0) {

            percentage =
                    (int) Math.round(
                            item.getTaskCount()
                                    * 100.0
                                    / totalTasks
                    );
        }

        progress.setProgress(
                percentage
        );

        progress.setIndicatorColor(
                categoryColor
        );

        progress.setTrackThickness(
                dp(6)
        );

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(6)
                );

        progressParams.setMargins(
                0,
                dp(8),
                0,
                0
        );

        progress.setLayoutParams(
                progressParams
        );

        container.addView(
                progress
        );

        layoutStatisticsCategories.addView(
                container
        );
    }

    // =========================================================
    // SCREEN STATE
    // =========================================================

    private void updateScreenState() {

        boolean loading =
                summaryStatus == UiState.Status.LOADING
                        || dailyStatus == UiState.Status.LOADING
                        || categoryStatus == UiState.Status.LOADING;

        if (loading) {

            progressStatistics.setVisibility(
                    View.VISIBLE
            );

            scrollStatistics.setVisibility(
                    View.GONE
            );

            layoutStatisticsError.setVisibility(
                    View.GONE
            );

            return;
        }

        boolean mainError =
                summaryStatus == UiState.Status.ERROR
                        || dailyStatus == UiState.Status.ERROR;

        if (mainError) {

            progressStatistics.setVisibility(
                    View.GONE
            );

            scrollStatistics.setVisibility(
                    View.GONE
            );

            layoutStatisticsError.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        if (summaryStatus == UiState.Status.SUCCESS
                && dailyStatus == UiState.Status.SUCCESS) {

            progressStatistics.setVisibility(
                    View.GONE
            );

            layoutStatisticsError.setVisibility(
                    View.GONE
            );

            scrollStatistics.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void showStatisticsError(
            String message
    ) {

        tvStatisticsError.setText(
                message == null
                        || message.trim().isEmpty()
                        ? "Unable to load statistics."
                        : message
        );
    }

    private void showCategoryError(
            String message
    ) {

        layoutStatisticsCategories.removeAllViews();

        addEmptyText(
                layoutStatisticsCategories,
                message == null
                        || message.trim().isEmpty()
                        ? "Unable to load categories."
                        : message
        );
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private void addEmptyText(
            LinearLayout parent,
            String text
    ) {

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setText(
                text
        );

        textView.setTextSize(
                14
        );

        textView.setTextColor(
                getResources().getColor(
                        R.color.sp_text_secondary,
                        requireContext().getTheme()
                )
        );

        textView.setGravity(
                Gravity.CENTER
        );

        textView.setPadding(
                dp(10),
                dp(16),
                dp(10),
                dp(16)
        );

        parent.addView(
                textView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private int getCategoryColor(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return getResources().getColor(
                    R.color.sp_text_secondary,
                    requireContext().getTheme()
            );
        }

        try {

            return Color.parseColor(
                    value.trim()
            );

        } catch (IllegalArgumentException exception) {

            return getResources().getColor(
                    R.color.sp_text_secondary,
                    requireContext().getTheme()
            );
        }
    }

    private int dp(
            int value
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }
}