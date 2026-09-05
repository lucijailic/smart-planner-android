package com.smartplanner.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartplanner.app.R;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.enums.TaskStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnImportantClickListener {

        void onImportantClick(
                Task task,
                boolean newImportantState
        );
    }

    private final List<Task> tasks =
            new ArrayList<>();

    private final Map<String, Category> categoryMap =
            new HashMap<>();

    private OnImportantClickListener importantClickListener;

    private OnTaskClickListener taskClickListener;

    public void setTasks(
            List<Task> newTasks
    ) {

        tasks.clear();

        if (newTasks != null) {
            tasks.addAll(newTasks);
        }

        notifyDataSetChanged();
    }

    public void setCategories(
            List<Category> categories
    ) {

        categoryMap.clear();

        if (categories != null) {

            for (Category category : categories) {

                if (category != null
                        && category.getId() != null) {

                    categoryMap.put(
                            category.getId(),
                            category
                    );
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setOnTaskClickListener(
            OnTaskClickListener listener
    ) {

        taskClickListener =
                listener;
    }

    public void setOnImportantClickListener(
            OnImportantClickListener listener
    ) {

        importantClickListener =
                listener;
    }

    public interface OnTaskClickListener {

        void onTaskClick(Task task);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_task,
                                parent,
                                false
                        );

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position
    ) {

        Task task =
                tasks.get(position);

        holder.tvTaskTitle.setText(
                task.getTitle()
        );

        bindCategory(
                holder,
                task
        );

        holder.tvTaskStatus.setText(
                getStatusText(task)
        );

        holder.tvTaskPriority.setText(
                getPriorityText(task)
        );

        holder.tvTaskDeadline.setText(
                getDeadlineText(task)
        );

        bindDuration(
                holder,
                task
        );

        bindImportant(
                holder,
                task
        );

        holder.tvTaskImportant.setOnClickListener(
                view -> {

                    if (importantClickListener == null) {
                        return;
                    }

                    boolean newImportantState =
                            !task.isImportant();

                    importantClickListener.onImportantClick(
                            task,
                            newImportantState
                    );
                }
        );

        holder.itemView.setOnClickListener(
                view -> {

                    if (taskClickListener != null) {

                        taskClickListener.onTaskClick(
                                task
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private void bindCategory(
            TaskViewHolder holder,
            Task task
    ) {

        String categoryId =
                task.getCategoryId();

        if (categoryId == null
                || categoryId.trim().isEmpty()) {

            holder.tvTaskCategory.setVisibility(
                    View.GONE
            );

            return;
        }

        Category category =
                categoryMap.get(categoryId);

        if (category == null) {

            holder.tvTaskCategory.setVisibility(
                    View.GONE
            );

            return;
        }

        holder.tvTaskCategory.setVisibility(
                View.VISIBLE
        );

        holder.tvTaskCategory.setText(
                category.getName()
        );

        int categoryColor =
                parseCategoryColor(
                        category.getColor()
                );

        GradientDrawable background =
                new GradientDrawable();

        background.setShape(
                GradientDrawable.RECTANGLE
        );

        background.setCornerRadius(
                dpToPx(
                        holder.itemView,
                        12
                )
        );

        background.setColor(
                categoryColor
        );

        holder.tvTaskCategory.setBackground(
                background
        );
    }

    private int parseCategoryColor(
            String color
    ) {

        if (color == null
                || color.trim().isEmpty()) {

            return Color.parseColor(
                    "#46C8BE"
            );
        }

        try {

            String formattedColor =
                    color.trim();

            if (!formattedColor.startsWith("#")) {

                formattedColor =
                        "#" + formattedColor;
            }

            return Color.parseColor(
                    formattedColor
            );

        } catch (IllegalArgumentException exception) {

            return Color.parseColor(
                    "#46C8BE"
            );
        }
    }

    private float dpToPx(
            View view,
            int dp
    ) {

        return dp
                * view.getResources()
                .getDisplayMetrics()
                .density;
    }

    private String getStatusText(
            Task task
    ) {

        if (isOverdue(task)) {
            return "Overdue";
        }

        if (task.getStatus() == null) {
            return "";
        }

        switch (task.getStatus()) {

            case TO_DO:
                return "To Do";

            case IN_PROGRESS:
                return "In Progress";

            case COMPLETED:
                return "Completed";

            default:
                return "";
        }
    }

    private String getPriorityText(
            Task task
    ) {

        if (task.getPriority() == null) {
            return "";
        }

        switch (task.getPriority()) {

            case LOW:
                return "Low priority";

            case MEDIUM:
                return "Medium priority";

            case HIGH:
                return "High priority";

            default:
                return "";
        }
    }

    private String getDeadlineText(
            Task task
    ) {

        String deadline =
                task.getDeadline();

        if (deadline == null
                || deadline.trim().isEmpty()) {

            return "No deadline";
        }

        Date parsedDate =
                parseSupabaseDate(
                        deadline
                );

        if (parsedDate == null) {
            return deadline;
        }

        SimpleDateFormat outputFormat =
                new SimpleDateFormat(
                        "dd.MM.yyyy. HH:mm",
                        Locale.getDefault()
                );

        return "Deadline: "
                + outputFormat.format(
                parsedDate
        );
    }

    private void bindDuration(
            TaskViewHolder holder,
            Task task
    ) {

        Integer duration =
                task.getEstimatedDuration();

        if (duration == null) {

            holder.tvTaskDuration.setVisibility(
                    View.GONE
            );

            return;
        }

        holder.tvTaskDuration.setVisibility(
                View.VISIBLE
        );

        holder.tvTaskDuration.setText(
                "Duration: "
                        + formatDuration(duration)
        );
    }

    private void bindImportant(
            TaskViewHolder holder,
            Task task
    ) {

        holder.tvTaskImportant.setVisibility(
                View.VISIBLE
        );

        if (task.isImportant()) {

            holder.tvTaskImportant.setText(
                    "★"
            );

            holder.tvTaskImportant.setAlpha(
                    1.0f
            );

        } else {

            holder.tvTaskImportant.setText(
                    "☆"
            );

            holder.tvTaskImportant.setAlpha(
                    0.55f
            );
        }
    }

    private String formatDuration(
            int minutes
    ) {

        if (minutes < 60) {
            return minutes + " min";
        }

        int hours =
                minutes / 60;

        int remainingMinutes =
                minutes % 60;

        if (remainingMinutes == 0) {
            return hours + " h";
        }

        return hours
                + " h "
                + remainingMinutes
                + " min";
    }

    private boolean isOverdue(
            Task task
    ) {

        if (task.getStatus()
                == TaskStatus.COMPLETED) {

            return false;
        }

        String deadline =
                task.getDeadline();

        if (deadline == null
                || deadline.trim().isEmpty()) {

            return false;
        }

        Date deadlineDate =
                parseSupabaseDate(
                        deadline
                );

        if (deadlineDate == null) {
            return false;
        }

        return deadlineDate.before(
                new Date()
        );
    }

    private Date parseSupabaseDate(
            String value
    ) {

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

    static class TaskViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTaskTitle;
        TextView tvTaskCategory;
        TextView tvTaskStatus;
        TextView tvTaskPriority;
        TextView tvTaskDeadline;
        TextView tvTaskDuration;
        TextView tvTaskImportant;

        public TaskViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvTaskTitle =
                    itemView.findViewById(
                            R.id.tvTaskTitle
                    );

            tvTaskCategory =
                    itemView.findViewById(
                            R.id.tvTaskCategory
                    );

            tvTaskStatus =
                    itemView.findViewById(
                            R.id.tvTaskStatus
                    );

            tvTaskPriority =
                    itemView.findViewById(
                            R.id.tvTaskPriority
                    );

            tvTaskDeadline =
                    itemView.findViewById(
                            R.id.tvTaskDeadline
                    );

            tvTaskDuration =
                    itemView.findViewById(
                            R.id.tvTaskDuration
                    );

            tvTaskImportant =
                    itemView.findViewById(
                            R.id.tvTaskImportant
                    );
        }
    }
}