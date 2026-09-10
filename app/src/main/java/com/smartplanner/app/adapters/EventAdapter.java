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
import com.smartplanner.app.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventAdapter
        extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnImportantClickListener {

        void onImportantClick(
                Event event,
                boolean newImportantState
        );
    }

    public interface OnEventClickListener {

        void onEventClick(
                Event event
        );
    }

    private final List<Event> events =
            new ArrayList<>();

    private final Map<String, Category> categoryMap =
            new HashMap<>();

    private OnImportantClickListener importantClickListener;
    private OnEventClickListener eventClickListener;

    // =========================================================
    // DATA
    // =========================================================

    public void setEvents(
            List<Event> newEvents
    ) {

        events.clear();

        if (newEvents != null) {

            events.addAll(
                    newEvents
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

    // =========================================================
    // LISTENERS
    // =========================================================

    public void setOnImportantClickListener(
            OnImportantClickListener listener
    ) {

        importantClickListener =
                listener;
    }

    public void setOnEventClickListener(
            OnEventClickListener listener
    ) {

        eventClickListener =
                listener;
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_event,
                                parent,
                                false
                        );

        return new EventViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull EventViewHolder holder,
            int position
    ) {

        Event event =
                events.get(position);

        holder.tvEventTitle.setText(
                event.getTitle()
        );

        bindCategory(
                holder,
                event
        );

        bindStatus(
                holder,
                event
        );

        bindDateTime(
                holder,
                event
        );

        bindLocation(
                holder,
                event
        );

        bindImportant(
                holder,
                event
        );

        holder.tvEventImportant.setOnClickListener(
                view -> {

                    if (importantClickListener == null) {
                        return;
                    }

                    boolean newImportantState =
                            !event.isImportant();

                    importantClickListener.onImportantClick(
                            event,
                            newImportantState
                    );
                }
        );

        holder.itemView.setOnClickListener(
                view -> {

                    if (eventClickListener != null) {

                        eventClickListener.onEventClick(
                                event
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {

        return events.size();
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    private void bindCategory(
            EventViewHolder holder,
            Event event
    ) {

        Category category =
                null;

        String categoryId =
                event.getCategoryId();

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

            holder.tvEventCategory.setVisibility(
                    View.VISIBLE
            );

            holder.tvEventCategory.setText(
                    category.getName()
            );

            holder.tvEventCategory.setBackground(
                    createRoundedBackground(
                            categoryColor,
                            50
                    )
            );

            holder.ivEventCategoryIcon.setImageResource(
                    getCategoryIcon(
                            category
                    )
            );

        } else {

            categoryColor =
                    Color.parseColor(
                            "#46C8BE"
                    );

            holder.tvEventCategory.setVisibility(
                    View.GONE
            );

            holder.ivEventCategoryIcon.setImageResource(
                    R.drawable.ic_task_category_default
            );
        }

        holder.ivEventCategoryIcon.setBackground(
                createRoundedBackground(
                        makePastelColor(
                                categoryColor
                        ),
                        50
                )
        );

        holder.ivEventCategoryIcon.setColorFilter(
                categoryColor
        );
    }

    private int getCategoryIcon(
            Category category
    ) {

        if (category == null) {

            return R.drawable.ic_task_category_default;
        }

        String icon =
                category.getIcon();

        if (icon != null
                && !icon.trim().isEmpty()) {

            switch (
                    icon.trim()
                            .toLowerCase(
                                    Locale.US
                            )
            ) {

                case "work":
                    return R.drawable.ic_task_category_work;

                case "personal":
                    return R.drawable.ic_task_category_personal;

                case "health":
                    return R.drawable.ic_task_category_health;

                case "study":
                    return R.drawable.ic_task_category_study;

                case "shopping":
                    return R.drawable.ic_task_category_shopping;
            }
        }

        String name =
                category.getName();

        if (name == null) {

            return R.drawable.ic_task_category_default;
        }

        String normalizedName =
                name.trim()
                        .toLowerCase(
                                Locale.US
                        );

        if (normalizedName.contains("work")
                || normalizedName.contains("job")
                || normalizedName.contains("business")) {

            return R.drawable.ic_task_category_work;
        }

        if (normalizedName.contains("personal")
                || normalizedName.contains("home")) {

            return R.drawable.ic_task_category_personal;
        }

        if (normalizedName.contains("health")
                || normalizedName.contains("fitness")
                || normalizedName.contains("gym")
                || normalizedName.contains("sport")) {

            return R.drawable.ic_task_category_health;
        }

        if (normalizedName.contains("study")
                || normalizedName.contains("university")
                || normalizedName.contains("school")
                || normalizedName.contains("college")) {

            return R.drawable.ic_task_category_study;
        }

        if (normalizedName.contains("shopping")
                || normalizedName.contains("shop")
                || normalizedName.contains("groceries")) {

            return R.drawable.ic_task_category_shopping;
        }

        return R.drawable.ic_task_category_default;
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void bindStatus(
            EventViewHolder holder,
            Event event
    ) {

        String status =
                getStatusText(
                        event
                );

        holder.tvEventStatus.setText(
                status
        );

        int backgroundColor;
        int textColor;

        switch (status) {

            case "Ongoing":

                backgroundColor =
                        Color.parseColor(
                                "#DDF7E9"
                        );

                textColor =
                        Color.parseColor(
                                "#258A5B"
                        );

                break;

            case "Past":

                backgroundColor =
                        Color.parseColor(
                                "#F1F4F6"
                        );

                textColor =
                        Color.parseColor(
                                "#718294"
                        );

                break;

            case "Upcoming":
            default:

                backgroundColor =
                        Color.parseColor(
                                "#E4F2FF"
                        );

                textColor =
                        Color.parseColor(
                                "#287AB8"
                        );

                break;
        }

        holder.tvEventStatus.setBackground(
                createRoundedBackground(
                        backgroundColor,
                        50
                )
        );

        holder.tvEventStatus.setTextColor(
                textColor
        );
    }

    private String getStatusText(
            Event event
    ) {

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

            return "Upcoming";
        }

        Date now =
                new Date();

        if (now.before(start)) {

            return "Upcoming";
        }

        if (now.after(end)) {

            return "Past";
        }

        return "Ongoing";
    }

    // =========================================================
    // DATE AND TIME
    // =========================================================

    private void bindDateTime(
            EventViewHolder holder,
            Event event
    ) {

        holder.tvEventDateTime.setText(
                getDateTimeText(
                        event
                )
        );
    }

    private String getDateTimeText(
            Event event
    ) {

        Date start =
                parseSupabaseDate(
                        event.getStartAt()
                );

        Date end =
                parseSupabaseDate(
                        event.getEndAt()
                );

        if (start == null) {

            return "Date not available";
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.ENGLISH
                );

        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.ENGLISH
                );

        if (end == null) {

            return dateFormat.format(start)
                    + ", "
                    + timeFormat.format(start);
        }

        String startDate =
                dateFormat.format(
                        start
                );

        String endDate =
                dateFormat.format(
                        end
                );

        String startTime =
                timeFormat.format(
                        start
                );

        String endTime =
                timeFormat.format(
                        end
                );

        if (startDate.equals(endDate)) {

            return startDate
                    + ", "
                    + startTime
                    + " – "
                    + endTime;
        }

        return startDate
                + ", "
                + startTime
                + " – "
                + endDate
                + ", "
                + endTime;
    }

    // =========================================================
    // LOCATION
    // =========================================================

    private void bindLocation(
            EventViewHolder holder,
            Event event
    ) {

        String location =
                event.getLocation();

        if (location == null
                || location.trim().isEmpty()) {

            holder.layoutEventLocation.setVisibility(
                    View.GONE
            );

            return;
        }

        holder.layoutEventLocation.setVisibility(
                View.VISIBLE
        );

        holder.tvEventLocation.setText(
                location.trim()
        );
    }

    // =========================================================
    // IMPORTANT
    // =========================================================

    private void bindImportant(
            EventViewHolder holder,
            Event event
    ) {

        if (event.isImportant()) {

            holder.tvEventImportant.setText(
                    "★"
            );

            holder.tvEventImportant.setAlpha(
                    1f
            );

        } else {

            holder.tvEventImportant.setText(
                    "☆"
            );

            holder.tvEventImportant.setAlpha(
                    0.75f
            );
        }
    }

    // =========================================================
    // DATE PARSING
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

    // =========================================================
    // COLORS
    // =========================================================

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

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class EventViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivEventCategoryIcon;

        TextView tvEventTitle;
        TextView tvEventCategory;
        TextView tvEventStatus;
        TextView tvEventDateTime;
        TextView tvEventLocation;
        TextView tvEventImportant;

        LinearLayout layoutEventLocation;

        EventViewHolder(
                @NonNull View itemView
        ) {

            super(
                    itemView
            );

            ivEventCategoryIcon =
                    itemView.findViewById(
                            R.id.ivEventCategoryIcon
                    );

            tvEventTitle =
                    itemView.findViewById(
                            R.id.tvEventTitle
                    );

            tvEventCategory =
                    itemView.findViewById(
                            R.id.tvEventCategory
                    );

            tvEventStatus =
                    itemView.findViewById(
                            R.id.tvEventStatus
                    );

            tvEventDateTime =
                    itemView.findViewById(
                            R.id.tvEventDateTime
                    );

            tvEventLocation =
                    itemView.findViewById(
                            R.id.tvEventLocation
                    );

            tvEventImportant =
                    itemView.findViewById(
                            R.id.tvEventImportant
                    );

            layoutEventLocation =
                    itemView.findViewById(
                            R.id.layoutEventLocation
                    );
        }
    }
}