package com.smartplanner.app.models;

public class StatisticsSummary {

    private final int totalTasks;
    private final int completedTasks;
    private final int activeTasks;
    private final int overdueTasks;

    private final Double completionRate;

    private final String mostProductiveDay;
    private final double averageCompletedPerDay;

    public StatisticsSummary(
            int totalTasks,
            int completedTasks,
            int activeTasks,
            int overdueTasks,
            Double completionRate,
            String mostProductiveDay,
            double averageCompletedPerDay
    ) {

        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.activeTasks = activeTasks;
        this.overdueTasks = overdueTasks;
        this.completionRate = completionRate;
        this.mostProductiveDay = mostProductiveDay;
        this.averageCompletedPerDay = averageCompletedPerDay;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getActiveTasks() {
        return activeTasks;
    }

    public int getOverdueTasks() {
        return overdueTasks;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public String getMostProductiveDay() {
        return mostProductiveDay;
    }

    public double getAverageCompletedPerDay() {
        return averageCompletedPerDay;
    }
}