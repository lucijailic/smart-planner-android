package com.smartplanner.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnImportantClickListener {

        void onImportantClick(
                Task task,
                boolean newImportantState
        );
    }

    public interface OnTaskClickListener {

        void onTaskClick(Task task);
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

            tasks.addAll(
                    newTasks
            );
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

        return new TaskViewHolder(
                view
        );
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

        bindStatus(
                holder,
                task
        );

        bindPriority(
                holder,
                task
        );

        bindDeadline(
                holder,
                task
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

        Category category =
                null;

        String categoryId =
                task.getCategoryId();

        if (categoryId != null
                && !categoryId.trim().isEmpty()) {

            category =
                    categoryMap.get(
                            categoryId
                    );
        }

        int categoryColor;

        if (category != null) {

            categoryColor =
                    parseCategoryColor(
                            category.getColor()
                    );

            holder.tvTaskCategory.setVisibility(
                    View.VISIBLE
            );

            holder.tvTaskCategory.setText(
                    category.getName()
            );

            holder.tvTaskCategory.setBackground(
                    createRoundedBackground(
                            categoryColor,
                            50
                    )
            );

            holder.ivTaskCategoryIcon.setImageResource(
                    getCategoryIcon(
                            category.getName()
                    )
            );

        } else {

            categoryColor =
                    Color.parseColor(
                            "#46C8BE"
                    );

            holder.tvTaskCategory.setVisibility(
                    View.GONE
            );

            holder.ivTaskCategoryIcon.setImageResource(
                    R.drawable.ic_task_category_default
            );
        }

        holder.ivTaskCategoryIcon.setBackground(
                createRoundedBackground(
                        makePastelColor(
                                categoryColor
                        ),
                        50
                )
        );

        holder.ivTaskCategoryIcon.setColorFilter(
                categoryColor
        );
    }

    private int getCategoryIcon(
            String categoryName
    ) {

        if (categoryName == null) {

            return R.drawable.ic_task_category_default;
        }

        String name =
                categoryName
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        if (name.contains("work")
                || name.contains("job")
                || name.contains("business")) {

            return R.drawable.ic_task_category_work;
        }

        if (name.contains("university")
                || name.contains("school")
                || name.contains("study")
                || name.contains("college")) {

            return R.drawable.ic_task_category_study;
        }

        if (name.contains("personal")
                || name.contains("home")) {

            return R.drawable.ic_task_category_personal;
        }

        if (name.contains("health")
                || name.contains("fitness")
                || name.contains("gym")
                || name.contains("sport")) {

            return R.drawable.ic_task_category_health;
        }

        if (name.contains("shopping")
                || name.contains("shop")
                || name.contains("groceries")) {

            return R.drawable.ic_task_category_shopping;
        }

        return R.drawable.ic_task_category_default;
    }

    private void bindStatus(
            TaskViewHolder holder,
            Task task
    ) {

        holder.tvTaskStatus.setText(
                getStatusText(
                        task
                )
        );

        int backgroundColor;
        int textColor;

        if (isOverdue(task)) {

            backgroundColor =
                    Color.parseColor(
                            "#FFE5E7"
                    );

            textColor =
                    Color.parseColor(
                            "#CF3F49"
                    );

        } else if (task.getStatus()
                == TaskStatus.COMPLETED) {

            backgroundColor =
                    Color.parseColor(
                            "#DDF7E9"
                    );

            textColor =
                    Color.parseColor(
                            "#258A5B"
                    );

        } else if (task.getStatus()
                == TaskStatus.IN_PROGRESS) {

            backgroundColor =
                    Color.parseColor(
                            "#E4F2FF"
                    );

            textColor =
                    Color.parseColor(
                            "#287AB8"
                    );

        } else {

            backgroundColor =
                    Color.parseColor(
                            "#EAF4FF"
                    );

            textColor =
                    Color.parseColor(
                            "#3977A8"
                    );
        }

        holder.tvTaskStatus.setBackground(
                createRoundedBackground(
                        backgroundColor,
                        50
                )
        );

        holder.tvTaskStatus.setTextColor(
                textColor
        );
    }

    private void bindPriority(
            TaskViewHolder holder,
            Task task
    ) {

        holder.tvTaskPriority.setText(
                getPriorityText(
                        task
                )
        );

        int backgroundColor;
        int textColor;

        if (task.getPriority() == null) {

            backgroundColor =
                    Color.parseColor(
                            "#F1F4F6"
                    );

            textColor =
                    Color.parseColor(
                            "#718294"
                    );

        } else {

            switch (task.getPriority()) {

                case HIGH:

                    backgroundColor =
                            Color.parseColor(
                                    "#FFE4E5"
                            );

                    textColor =
                            Color.parseColor(
                                    "#D63B45"
                            );

                    break;

                case MEDIUM:

                    backgroundColor =
                            Color.parseColor(
                                    "#FFF1D5"
                            );

                    textColor =
                            Color.parseColor(
                                    "#B87514"
                            );

                    break;

                case LOW:
                default:

                    backgroundColor =
                            Color.parseColor(
                                    "#DDF7E7"
                            );

                    textColor =
                            Color.parseColor(
                                    "#258557"
                            );

                    break;
            }
        }

        holder.tvTaskPriority.setBackground(
                createRoundedBackground(
                        backgroundColor,
                        50
                )
        );

        holder.tvTaskPriority.setTextColor(
                textColor
        );
    }

    private void bindDeadline(
            TaskViewHolder holder,
            Task task
    ) {

        holder.tvTaskDeadline.setText(
                getDeadlineText(
                        task
                )
        );

        if (isOverdue(task)) {

            holder.tvTaskDeadline.setTextColor(
                    Color.parseColor(
                            "#CF3F49"
                    )
            );

        } else {

            holder.tvTaskDeadline.setTextColor(
                    holder.itemView
                            .getContext()
                            .getColor(
                                    R.color.sp_text_secondary
                            )
            );
        }
    }

    private void bindDuration(
            TaskViewHolder holder,
            Task task
    ) {

        Integer duration =
                task.getEstimatedDuration();

        if (duration == null) {

            holder.layoutTaskDuration.setVisibility(
                    View.GONE
            );

            return;
        }

        holder.layoutTaskDuration.setVisibility(
                View.VISIBLE
        );

        holder.tvTaskDuration.setText(
                formatDuration(
                        duration
                )
        );
    }

    private void bindImportant(
            TaskViewHolder holder,
            Task task
    ) {

        if (task.isImportant()) {

            holder.tvTaskImportant.setText(
                    "★"
            );

            holder.tvTaskImportant.setAlpha(
                    1f
            );

        } else {

            holder.tvTaskImportant.setText(
                    "☆"
            );

            holder.tvTaskImportant.setAlpha(
                    0.75f
            );
        }
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
                return "Low";

            case MEDIUM:
                return "Medium";

            case HIGH:
                return "High";

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
                        "dd MMM yyyy, HH:mm",
                        Locale.ENGLISH
                );

        return outputFormat.format(
                parsedDate
        );
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

        if (task == null) {
            return false;
        }

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

    private int makePastelColor(
            int color
    ) {

        int red =
                Color.red(
                        color
                );

        int green =
                Color.green(
                        color
                );

        int blue =
                Color.blue(
                        color
                );

        red =
                (int) (
                        red * 0.18f
                                + 255 * 0.82f
                );

        green =
                (int) (
                        green * 0.18f
                                + 255 * 0.82f
                );

        blue =
                (int) (
                        blue * 0.18f
                                + 255 * 0.82f
                );

        return Color.rgb(
                red,
                green,
                blue
        );
    }

    private GradientDrawable createRoundedBackground(
            int color,
            int radiusDp
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.RECTANGLE
        );

        drawable.setColor(
                color
        );

        drawable.setCornerRadius(
                dpToPx(
                        radiusDp
                )
        );

        return drawable;
    }

    private float dpToPx(
            int dp
    ) {

        return dp
                * android.content.res.Resources
                .getSystem()
                .getDisplayMetrics()
                .density;
    }

    static class TaskViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivTaskCategoryIcon;

        TextView tvTaskTitle;
        TextView tvTaskCategory;
        TextView tvTaskStatus;
        TextView tvTaskPriority;

        TextView tvTaskDeadline;
        TextView tvTaskDuration;

        TextView tvTaskImportant;

        LinearLayout layoutTaskDuration;

        TaskViewHolder(
                @NonNull View itemView
        ) {

            super(
                    itemView
            );

            ivTaskCategoryIcon =
                    itemView.findViewById(
                            R.id.ivTaskCategoryIcon
                    );

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

            layoutTaskDuration =
                    itemView.findViewById(
                            R.id.layoutTaskDuration
                    );
        }
    }
}