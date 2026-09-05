package com.smartplanner.app.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.smartplanner.app.R;
import com.smartplanner.app.models.Subtask;

import java.util.ArrayList;
import java.util.List;

public class SubtaskAdapter
        extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

    public interface OnSubtaskActionListener {

        void onSubtaskCheckedChanged(
                Subtask subtask,
                boolean completed
        );

        void onDeleteSubtaskClick(
                Subtask subtask
        );
    }

    private final List<Subtask> subtasks =
            new ArrayList<>();

    private final OnSubtaskActionListener actionListener;

    public SubtaskAdapter(
            OnSubtaskActionListener actionListener
    ) {

        this.actionListener =
                actionListener;
    }

    public void setSubtasks(
            List<Subtask> newSubtasks
    ) {

        subtasks.clear();

        if (newSubtasks != null) {

            subtasks.addAll(
                    newSubtasks
            );
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_subtask,
                                parent,
                                false
                        );

        return new SubtaskViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull SubtaskViewHolder holder,
            int position
    ) {

        Subtask subtask =
                subtasks.get(position);

        holder.bind(
                subtask,
                actionListener
        );
    }

    @Override
    public int getItemCount() {

        return subtasks.size();
    }

    static class SubtaskViewHolder
            extends RecyclerView.ViewHolder {

        private final MaterialCheckBox checkSubtaskCompleted;
        private final TextView tvSubtaskTitle;
        private final MaterialButton btnDeleteSubtask;

        public SubtaskViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            checkSubtaskCompleted =
                    itemView.findViewById(
                            R.id.checkSubtaskCompleted
                    );

            tvSubtaskTitle =
                    itemView.findViewById(
                            R.id.tvSubtaskTitle
                    );

            btnDeleteSubtask =
                    itemView.findViewById(
                            R.id.btnDeleteSubtask
                    );
        }

        public void bind(
                Subtask subtask,
                OnSubtaskActionListener actionListener
        ) {

            if (subtask == null) {
                return;
            }

            checkSubtaskCompleted.setOnCheckedChangeListener(
                    null
            );

            checkSubtaskCompleted.setChecked(
                    subtask.isCompleted()
            );

            tvSubtaskTitle.setText(
                    subtask.getTitle()
            );

            updateCompletedAppearance(
                    subtask.isCompleted()
            );

            checkSubtaskCompleted.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {

                        updateCompletedAppearance(
                                isChecked
                        );

                        if (actionListener != null) {

                            actionListener.onSubtaskCheckedChanged(
                                    subtask,
                                    isChecked
                            );
                        }
                    }
            );

            btnDeleteSubtask.setOnClickListener(
                    view -> {

                        if (actionListener != null) {

                            actionListener.onDeleteSubtaskClick(
                                    subtask
                            );
                        }
                    }
            );
        }

        private void updateCompletedAppearance(
                boolean completed
        ) {

            if (completed) {

                tvSubtaskTitle.setPaintFlags(
                        tvSubtaskTitle.getPaintFlags()
                                | Paint.STRIKE_THRU_TEXT_FLAG
                );

                tvSubtaskTitle.setAlpha(
                        0.6f
                );

            } else {

                tvSubtaskTitle.setPaintFlags(
                        tvSubtaskTitle.getPaintFlags()
                                & ~Paint.STRIKE_THRU_TEXT_FLAG
                );

                tvSubtaskTitle.setAlpha(
                        1.0f
                );
            }
        }
    }
}