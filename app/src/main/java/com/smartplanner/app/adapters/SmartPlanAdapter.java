package com.smartplanner.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartplanner.app.R;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SmartPlanAdapter
        extends RecyclerView.Adapter<
        SmartPlanAdapter.SmartPlanViewHolder> {

    // =========================================================
    // LISTENER
    // =========================================================

    public interface SmartPlanItemListener {

        void onSmartPlanItemClick(
                SmartPlanItem item
        );
    }


    // =========================================================
    // DATA
    // =========================================================

    private final List<SmartPlanItem> items =
            new ArrayList<>();

    private final Map<String, Task> taskMap =
            new HashMap<>();

    private final SmartPlanItemListener listener;


    // =========================================================
    // FORMATTERS
    // =========================================================

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern(
                    "HH:mm",
                    Locale.ENGLISH
            );

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(
                    "EEEE, MMM d",
                    Locale.ENGLISH
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SmartPlanAdapter(
            SmartPlanItemListener listener
    ) {

        this.listener =
                listener;
    }


    // =========================================================
    // SET ITEMS
    // =========================================================

    public void setItems(
            List<SmartPlanItem> newItems
    ) {

        items.clear();


        if (newItems != null) {

            for (SmartPlanItem item :
                    newItems) {

                if (item != null) {

                    items.add(
                            item
                    );
                }
            }
        }


        notifyDataSetChanged();
    }


    // =========================================================
    // SET TASKS
    // =========================================================

    public void setTasks(
            List<Task> tasks
    ) {

        taskMap.clear();


        if (tasks != null) {

            for (Task task :
                    tasks) {

                if (task == null
                        || task.getId() == null
                        || task.getId()
                        .trim()
                        .isEmpty()) {

                    continue;
                }


                taskMap.put(
                        task.getId(),
                        task
                );
            }
        }


        notifyDataSetChanged();
    }


    // =========================================================
    // CREATE HOLDER
    // =========================================================

    @NonNull
    @Override
    public SmartPlanViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(
                                parent.getContext()
                        )
                        .inflate(
                                R.layout.item_smart_plan_session,
                                parent,
                                false
                        );


        return new SmartPlanViewHolder(
                view
        );
    }


    // =========================================================
    // BIND
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull SmartPlanViewHolder holder,
            int position
    ) {

        SmartPlanItem item =
                items.get(
                        position
                );


        holder.bind(
                item
        );
    }


    // =========================================================
    // COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return items.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    class SmartPlanViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvStartTime;
        private final TextView tvEndTime;

        private final TextView tvTitle;
        private final TextView tvStatus;

        private final TextView tvDate;
        private final TextView tvDuration;


        SmartPlanViewHolder(
                @NonNull View itemView
        ) {

            super(
                    itemView
            );


            tvStartTime =
                    itemView.findViewById(
                            R.id.tvSmartPlanItemStartTime
                    );

            tvEndTime =
                    itemView.findViewById(
                            R.id.tvSmartPlanItemEndTime
                    );

            tvTitle =
                    itemView.findViewById(
                            R.id.tvSmartPlanItemTitle
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvSmartPlanItemStatus
                    );

            tvDate =
                    itemView.findViewById(
                            R.id.tvSmartPlanItemDate
                    );

            tvDuration =
                    itemView.findViewById(
                            R.id.tvSmartPlanItemDuration
                    );


            itemView.setOnClickListener(
                    view -> {

                        int position =
                                getBindingAdapterPosition();


                        if (position
                                == RecyclerView.NO_POSITION) {

                            return;
                        }


                        if (listener != null) {

                            listener.onSmartPlanItemClick(
                                    items.get(
                                            position
                                    )
                            );
                        }
                    }
            );
        }


        // =====================================================
        // BIND ITEM
        // =====================================================

        private void bind(
                SmartPlanItem item
        ) {

            ZonedDateTime start =
                    parseDateTime(
                            item.getPlannedStart()
                    );

            ZonedDateTime end =
                    parseDateTime(
                            item.getPlannedEnd()
                    );


            // =============================================
            // TASK TITLE
            // =============================================

            Task task =
                    item.getTaskId() != null
                            ? taskMap.get(
                            item.getTaskId()
                    )
                            : null;


            if (task != null
                    && task.getTitle() != null
                    && !task.getTitle()
                    .trim()
                    .isEmpty()) {

                tvTitle.setText(
                        task.getTitle()
                );

            } else {

                tvTitle.setText(
                        "Task session"
                );
            }


            // =============================================
            // START
            // =============================================

            if (start != null) {

                tvStartTime.setText(
                        start.format(
                                timeFormatter
                        )
                );

                tvDate.setText(
                        start.format(
                                dateFormatter
                        )
                );

            } else {

                tvStartTime.setText(
                        "--:--"
                );

                tvDate.setText(
                        "Scheduled session"
                );
            }


            // =============================================
            // END
            // =============================================

            if (end != null) {

                tvEndTime.setText(
                        end.format(
                                timeFormatter
                        )
                );

            } else {

                tvEndTime.setText(
                        "--:--"
                );
            }


            // =============================================
            // DURATION
            // =============================================

            tvDuration.setText(
                    formatDuration(
                            item.getPlannedDuration()
                    )
            );


            // =============================================
            // STATUS
            // =============================================

            updateStatus(
                    item.getStatus()
            );
        }


        // =====================================================
        // STATUS
        // =====================================================

        private void updateStatus(
                SmartPlanItemStatus status
        ) {

            if (status == null) {

                tvStatus.setText(
                        "PLANNED"
                );

                return;
            }


            switch (status) {

                case COMPLETED:

                    tvStatus.setText(
                            "COMPLETED"
                    );

                    break;


                case SKIPPED:

                    tvStatus.setText(
                            "SKIPPED"
                    );

                    break;


                case PLANNED:

                default:

                    tvStatus.setText(
                            "PLANNED"
                    );

                    break;
            }
        }
    }


    // =========================================================
    // DATE TIME PARSING
    // =========================================================

    private ZonedDateTime parseDateTime(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }


        try {

            OffsetDateTime offsetDateTime =
                    OffsetDateTime.parse(
                            value.trim()
                    );


            return offsetDateTime
                    .atZoneSameInstant(
                            ZoneId.systemDefault()
                    );

        } catch (Exception ignored) {
        }


        try {

            return ZonedDateTime
                    .parse(
                            value.trim()
                    )
                    .withZoneSameInstant(
                            ZoneId.systemDefault()
                    );

        } catch (Exception exception) {

            return null;
        }
    }


    // =========================================================
    // DURATION
    // =========================================================

    private String formatDuration(
            int minutes
    ) {

        if (minutes <= 0) {

            return "—";
        }


        if (minutes < 60) {

            return minutes
                    + " min";
        }


        int hours =
                minutes / 60;

        int remainingMinutes =
                minutes % 60;


        if (remainingMinutes == 0) {

            return hours
                    + (hours == 1
                    ? " hr"
                    : " hrs");
        }


        return hours
                + " h "
                + remainingMinutes
                + " min";
    }
}