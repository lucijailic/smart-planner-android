package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.CategoryTaskCount;
import com.smartplanner.app.models.DailyTaskCount;
import com.smartplanner.app.models.StatisticsSummary;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.StatisticsPeriod;
import com.smartplanner.app.models.enums.TaskStatus;
import com.smartplanner.app.repositories.CategoryRepository;
import com.smartplanner.app.repositories.TaskRepository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    private final MutableLiveData<UiState<StatisticsSummary>> summaryState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<List<DailyTaskCount>>> dailyCountsState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<List<CategoryTaskCount>>> categoryCountsState =
            new MutableLiveData<>();

    private StatisticsPeriod selectedPeriod =
            StatisticsPeriod.WEEK;

    private List<Task> tasks = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private boolean tasksLoaded = false;
    private boolean categoriesLoaded = false;

    private String tasksError;
    private String categoriesError;

    public StatisticsViewModel(
            @NonNull Application application
    ) {
        super(application);

        taskRepository =
                new TaskRepository(application);

        categoryRepository =
                new CategoryRepository(application);
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public LiveData<UiState<StatisticsSummary>> getSummaryState() {
        return summaryState;
    }

    public LiveData<UiState<List<DailyTaskCount>>> getDailyCountsState() {
        return dailyCountsState;
    }

    public LiveData<UiState<List<CategoryTaskCount>>> getCategoryCountsState() {
        return categoryCountsState;
    }

    public StatisticsPeriod getSelectedPeriod() {
        return selectedPeriod;
    }

    // =========================================================
    // LOAD
    // =========================================================

    public void loadStatistics() {

        summaryState.setValue(UiState.loading());
        dailyCountsState.setValue(UiState.loading());
        categoryCountsState.setValue(UiState.loading());

        tasksLoaded = false;
        categoriesLoaded = false;

        tasksError = null;
        categoriesError = null;

        loadTasks();
        loadCategories();
    }

    public void setPeriod(
            StatisticsPeriod period
    ) {

        if (period == null) {
            return;
        }

        selectedPeriod = period;

        /*
         * Nema potrebe ponovno zvati Supabase.
         * Već učitane podatke samo ponovno obrađujemo
         * za odabrani period.
         */
        if (tasksLoaded && categoriesLoaded) {
            calculateStatistics();
        }
    }

    private void loadTasks() {

        taskRepository.getTasks(
                new TaskRepository.TaskCallback<List<Task>>() {

                    @Override
                    public void onSuccess(
                            List<Task> result
                    ) {

                        tasks =
                                result != null
                                        ? result
                                        : new ArrayList<>();

                        tasksLoaded = true;

                        tryCalculate();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        tasksError = message;
                        tasksLoaded = true;

                        tryCalculate();
                    }
                }
        );
    }

    private void loadCategories() {

        categoryRepository.getCategories(
                new CategoryRepository.CategoryCallback<List<Category>>() {

                    @Override
                    public void onSuccess(
                            List<Category> result
                    ) {

                        categories =
                                result != null
                                        ? result
                                        : new ArrayList<>();

                        categoriesLoaded = true;

                        tryCalculate();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        categoriesError = message;
                        categoriesLoaded = true;

                        tryCalculate();
                    }
                }
        );
    }

    private void tryCalculate() {

        if (!tasksLoaded || !categoriesLoaded) {
            return;
        }

        if (tasksError != null) {

            summaryState.postValue(
                    UiState.error(tasksError)
            );

            dailyCountsState.postValue(
                    UiState.error(tasksError)
            );

            categoryCountsState.postValue(
                    UiState.error(tasksError)
            );

            return;
        }

        /*
         * Bez kategorija i dalje možemo izračunati
         * summary i daily chart, ali Tasks by Category
         * ne možemo pouzdano prikazati.
         */
        if (categoriesError != null) {

            calculateSummaryAndDaily();

            categoryCountsState.postValue(
                    UiState.error(categoriesError)
            );

            return;
        }

        calculateStatistics();
    }

    // =========================================================
    // CALCULATIONS
    // =========================================================

    private void calculateStatistics() {

        calculateSummaryAndDaily();
        calculateCategoryCounts();
    }

    private void calculateSummaryAndDaily() {

        LocalDate today = LocalDate.now();

        LocalDate periodStart;
        LocalDate periodEnd;

        if (selectedPeriod == StatisticsPeriod.WEEK) {

            periodStart =
                    today.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );

            periodEnd =
                    periodStart.plusDays(6);

        } else {

            periodStart =
                    today.withDayOfMonth(1);

            periodEnd =
                    today.with(
                            TemporalAdjusters.lastDayOfMonth()
                    );
        }

        int totalTasks = tasks.size();
        int completedTasks = 0;
        int activeTasks = 0;
        int overdueTasks = 0;

        int tasksWithDeadlineInPeriod = 0;
        int completedTasksWithDeadlineInPeriod = 0;

        Map<LocalDate, Integer> completedByDay =
                new HashMap<>();

        Instant now = Instant.now();

        for (Task task : tasks) {

            if (task == null) {
                continue;
            }

            // -------------------------------------------------
            // ACTIVE
            // -------------------------------------------------

            if (task.getStatus() != TaskStatus.COMPLETED) {
                activeTasks++;
            }

            // -------------------------------------------------
            // OVERDUE
            // -------------------------------------------------

            Instant deadlineInstant =
                    parseInstant(task.getDeadline());

            if (deadlineInstant != null
                    && deadlineInstant.isBefore(now)
                    && task.getStatus() != TaskStatus.COMPLETED) {

                overdueTasks++;
            }

            // -------------------------------------------------
            // COMPLETED IN SELECTED PERIOD
            // -------------------------------------------------

            if (task.getStatus() == TaskStatus.COMPLETED) {

                LocalDate completedDate =
                        toLocalDate(
                                task.getCompletedAt()
                        );

                if (completedDate != null
                        && isDateInRange(
                        completedDate,
                        periodStart,
                        periodEnd
                )) {

                    completedTasks++;

                    Integer currentCount =
                            completedByDay.get(
                                    completedDate
                            );

                    completedByDay.put(
                            completedDate,
                            currentCount == null
                                    ? 1
                                    : currentCount + 1
                    );
                }
            }

            // -------------------------------------------------
            // COMPLETION RATE
            // Based on tasks whose deadline belongs
            // to selected period.
            // -------------------------------------------------

            LocalDate deadlineDate =
                    toLocalDate(
                            task.getDeadline()
                    );

            if (deadlineDate != null
                    && isDateInRange(
                    deadlineDate,
                    periodStart,
                    periodEnd
            )) {

                tasksWithDeadlineInPeriod++;

                if (task.getStatus() == TaskStatus.COMPLETED) {
                    completedTasksWithDeadlineInPeriod++;
                }
            }
        }

        Double completionRate = null;

        if (tasksWithDeadlineInPeriod > 0) {

            completionRate =
                    (completedTasksWithDeadlineInPeriod * 100.0)
                            / tasksWithDeadlineInPeriod;
        }

        String mostProductiveDay =
                calculateMostProductiveDay(
                        completedByDay
                );

        double averageCompletedPerDay =
                calculateAverageCompletedPerDay(
                        completedTasks,
                        periodStart,
                        today
                );

        StatisticsSummary summary =
                new StatisticsSummary(
                        totalTasks,
                        completedTasks,
                        activeTasks,
                        overdueTasks,
                        completionRate,
                        mostProductiveDay,
                        averageCompletedPerDay
                );

        summaryState.postValue(
                UiState.success(summary)
        );

        dailyCountsState.postValue(
                UiState.success(
                        buildDailyCounts(
                                completedByDay,
                                periodStart,
                                periodEnd
                        )
                )
        );
    }

    private void calculateCategoryCounts() {

        Map<String, Category> categoryMap =
                new HashMap<>();

        for (Category category : categories) {

            if (category == null
                    || category.getId() == null) {
                continue;
            }

            categoryMap.put(
                    category.getId(),
                    category
            );
        }

        Map<String, Integer> counts =
                new LinkedHashMap<>();

        int uncategorizedCount = 0;

        for (Task task : tasks) {

            if (task == null) {
                continue;
            }

            String categoryId =
                    task.getCategoryId();

            if (categoryId == null
                    || categoryId.trim().isEmpty()
                    || !categoryMap.containsKey(categoryId)) {

                uncategorizedCount++;
                continue;
            }

            Integer currentCount =
                    counts.get(categoryId);

            counts.put(
                    categoryId,
                    currentCount == null
                            ? 1
                            : currentCount + 1
            );
        }

        List<CategoryTaskCount> result =
                new ArrayList<>();

        for (Category category : categories) {

            if (category == null
                    || category.getId() == null) {
                continue;
            }

            int count =
                    counts.containsKey(category.getId())
                            ? counts.get(category.getId())
                            : 0;

            result.add(
                    new CategoryTaskCount(
                            category.getId(),
                            category.getName(),
                            category.getColor(),
                            count
                    )
            );
        }

        if (uncategorizedCount > 0) {

            result.add(
                    new CategoryTaskCount(
                            null,
                            "Uncategorized",
                            null,
                            uncategorizedCount
                    )
            );
        }

        categoryCountsState.postValue(
                UiState.success(result)
        );
    }

    // =========================================================
    // DAILY COUNTS
    // =========================================================

    private List<DailyTaskCount> buildDailyCounts(
            Map<LocalDate, Integer> completedByDay,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        List<DailyTaskCount> result =
                new ArrayList<>();

        LocalDate current = periodStart;

        while (!current.isAfter(periodEnd)) {

            int count =
                    completedByDay.containsKey(current)
                            ? completedByDay.get(current)
                            : 0;

            String label;

            if (selectedPeriod == StatisticsPeriod.WEEK) {

                label =
                        current.getDayOfWeek()
                                .getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.ENGLISH
                                );

            } else {

                label =
                        String.valueOf(
                                current.getDayOfMonth()
                        );
            }

            result.add(
                    new DailyTaskCount(
                            label,
                            count
                    )
            );

            current =
                    current.plusDays(1);
        }

        return result;
    }

    // =========================================================
    // PRODUCTIVITY
    // =========================================================

    private String calculateMostProductiveDay(
            Map<LocalDate, Integer> completedByDay
    ) {

        if (completedByDay.isEmpty()) {
            return "N/A";
        }

        LocalDate bestDate = null;
        int bestCount = 0;

        for (Map.Entry<LocalDate, Integer> entry
                : completedByDay.entrySet()) {

            if (entry.getValue() > bestCount) {

                bestCount =
                        entry.getValue();

                bestDate =
                        entry.getKey();
            }
        }

        if (bestDate == null) {
            return "N/A";
        }

        return bestDate
                .getDayOfWeek()
                .getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                );
    }

    private double calculateAverageCompletedPerDay(
            int completedTasks,
            LocalDate periodStart,
            LocalDate today
    ) {

        long days;

        if (selectedPeriod == StatisticsPeriod.WEEK) {

            /*
             * Za trenutni tjedan računamo samo dane
             * koji su do sada prošli, uključujući danas.
             */
            days =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            periodStart,
                            today
                    ) + 1;

        } else {

            /*
             * Za trenutni mjesec koristimo broj dana
             * od prvog dana mjeseca do danas.
             */
            days =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            periodStart,
                            today
                    ) + 1;
        }

        if (days <= 0) {
            return 0.0;
        }

        return completedTasks / (double) days;
    }

    // =========================================================
    // DATE HELPERS
    // =========================================================

    private Instant parseInstant(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        try {

            return Instant.parse(
                    value.trim()
            );

        } catch (Exception exception) {

            return null;
        }
    }

    private LocalDate toLocalDate(
            String value
    ) {

        Instant instant =
                parseInstant(value);

        if (instant == null) {
            return null;
        }

        return instant
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }

    private boolean isDateInRange(
            LocalDate date,
            LocalDate start,
            LocalDate end
    ) {

        return !date.isBefore(start)
                && !date.isAfter(end);
    }
}