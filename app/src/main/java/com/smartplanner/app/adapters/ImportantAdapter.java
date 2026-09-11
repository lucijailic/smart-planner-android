package com.smartplanner.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartplanner.app.R;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.ImportantItem;
import com.smartplanner.app.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImportantAdapter
        extends RecyclerView.Adapter<ImportantAdapter.ImportantViewHolder> {

    public interface ImportantItemListener {

        void onImportantItemClick(
                ImportantItem item
        );
    }

    private final List<ImportantItem> items =
            new ArrayList<>();

    private final ImportantItemListener listener;

    public ImportantAdapter(
            ImportantItemListener listener
    ) {

        this.listener = listener;
    }

    // =========================================================
    // DATA
    // =========================================================

    public void setItems(
            List<ImportantItem> newItems
    ) {

        items.clear();

        if (newItems != null) {

            items.addAll(
                    newItems
            );
        }

        notifyDataSetChanged();
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    @NonNull
    @Override
    public ImportantViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(
                                parent.getContext()
                        )
                        .inflate(
                                R.layout.item_important,
                                parent,
                                false
                        );

        return new ImportantViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ImportantViewHolder holder,
            int position
    ) {

        ImportantItem item =
                items.get(
                        position
                );

        holder.bind(
                item
        );
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    class ImportantViewHolder
            extends RecyclerView.ViewHolder {

        private final MaterialCardView cardImportantItem;

        private final ImageView ivImportantType;

        private final TextView tvImportantItemType;
        private final TextView tvImportantItemTitle;
        private final TextView tvImportantItemInfo;

        ImportantViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            cardImportantItem =
                    itemView.findViewById(
                            R.id.cardImportantItem
                    );

            ivImportantType =
                    itemView.findViewById(
                            R.id.ivImportantType
                    );

            tvImportantItemType =
                    itemView.findViewById(
                            R.id.tvImportantItemType
                    );

            tvImportantItemTitle =
                    itemView.findViewById(
                            R.id.tvImportantItemTitle
                    );

            tvImportantItemInfo =
                    itemView.findViewById(
                            R.id.tvImportantItemInfo
                    );
        }

        void bind(
                ImportantItem item
        ) {

            if (item.getType()
                    == ImportantItem.Type.TASK) {

                bindTask(
                        item.getTask()
                );

            } else {

                bindEvent(
                        item.getEvent()
                );
            }

            cardImportantItem.setOnClickListener(
                    v -> {

                        if (listener != null) {

                            listener.onImportantItemClick(
                                    item
                            );
                        }
                    }
            );
        }

        private void bindTask(
                Task task
        ) {

            tvImportantItemType.setText(
                    "TASK"
            );

            ivImportantType.setImageResource(
                    R.drawable.ic_nav_tasks
            );

            if (task == null) {

                tvImportantItemTitle.setText(
                        "Untitled task"
                );

                tvImportantItemInfo.setText(
                        "Important task"
                );

                return;
            }

            String title =
                    task.getTitle();

            if (title == null
                    || title.trim().isEmpty()) {

                title =
                        "Untitled task";
            }

            tvImportantItemTitle.setText(
                    title
            );

            Date deadline =
                    parseDate(
                            task.getDeadline()
                    );

            if (deadline == null) {

                tvImportantItemInfo.setText(
                        "Important task • No deadline"
                );

                return;
            }

            tvImportantItemInfo.setText(
                    buildCountdown(
                            deadline,
                            true
                    )
            );
        }

        private void bindEvent(
                Event event
        ) {

            tvImportantItemType.setText(
                    "EVENT"
            );

            ivImportantType.setImageResource(
                    R.drawable.ic_nav_events
            );

            if (event == null) {

                tvImportantItemTitle.setText(
                        "Untitled event"
                );

                tvImportantItemInfo.setText(
                        "Important event"
                );

                return;
            }

            String title =
                    event.getTitle();

            if (title == null
                    || title.trim().isEmpty()) {

                title =
                        "Untitled event";
            }

            tvImportantItemTitle.setText(
                    title
            );

            Date start =
                    parseDate(
                            event.getStartAt()
                    );

            Date end =
                    parseDate(
                            event.getEndAt()
                    );

            Date now =
                    new Date();

            if (start != null
                    && end != null
                    && !start.after(now)
                    && !end.before(now)) {

                tvImportantItemInfo.setText(
                        "Event is ongoing"
                );

                return;
            }

            if (start == null) {

                tvImportantItemInfo.setText(
                        "Important event"
                );

                return;
            }

            tvImportantItemInfo.setText(
                    buildCountdown(
                            start,
                            false
                    )
            );
        }

        // =====================================================
        // COUNTDOWN
        // =====================================================

        private String buildCountdown(
                Date targetDate,
                boolean task
        ) {

            Date now =
                    new Date();

            long difference =
                    targetDate.getTime()
                            - now.getTime();

            if (difference < 0) {

                long overdueMinutes =
                        Math.abs(
                                difference
                        ) / (60 * 1000);

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
                        overdueMinutes
                                / 60;

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
                    totalMinutes
                            % 60;

            String prefix =
                    task
                            ? "Due in "
                            : "Starts in ";

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

        // =====================================================
        // DATE
        // =====================================================

        private Date parseDate(
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

                } catch (Exception ignored) {
                }
            }

            return null;
        }
    }
}