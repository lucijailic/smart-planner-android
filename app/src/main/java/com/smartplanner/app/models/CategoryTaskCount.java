package com.smartplanner.app.models;

public class CategoryTaskCount {

    private final String categoryId;
    private final String categoryName;
    private final String categoryColor;
    private final int taskCount;

    public CategoryTaskCount(
            String categoryId,
            String categoryName,
            String categoryColor,
            int taskCount
    ) {

        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.taskCount = taskCount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public int getTaskCount() {
        return taskCount;
    }
}