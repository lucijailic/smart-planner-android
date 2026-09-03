package com.smartplanner.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartplanner.app.R;
import com.smartplanner.app.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface CategoryActionListener {

        void onEditCategory(Category category);

        void onDeleteCategory(Category category);
    }

    private final List<Category> categories =
            new ArrayList<>();

    private final CategoryActionListener actionListener;

    public CategoryAdapter(
            CategoryActionListener actionListener
    ) {
        this.actionListener = actionListener;
    }

    public void submitList(
            List<Category> newCategories
    ) {

        categories.clear();

        if (newCategories != null) {
            categories.addAll(newCategories);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_category,
                        parent,
                        false
                );

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position
    ) {

        Category category =
                categories.get(position);

        holder.bind(
                category,
                actionListener
        );
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder
            extends RecyclerView.ViewHolder {

        private final View viewCategoryColor;

        private final TextView tvCategoryName;

        private final MaterialButton btnEditCategory;
        private final MaterialButton btnDeleteCategory;

        public CategoryViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            viewCategoryColor =
                    itemView.findViewById(
                            R.id.viewCategoryColor
                    );

            tvCategoryName =
                    itemView.findViewById(
                            R.id.tvCategoryName
                    );

            btnEditCategory =
                    itemView.findViewById(
                            R.id.btnEditCategory
                    );

            btnDeleteCategory =
                    itemView.findViewById(
                            R.id.btnDeleteCategory
                    );
        }

        public void bind(
                Category category,
                CategoryActionListener listener
        ) {

            tvCategoryName.setText(
                    category.getName()
            );

            applyCategoryColor(
                    category.getColor()
            );

            btnEditCategory.setOnClickListener(
                    view -> listener.onEditCategory(
                            category
                    )
            );

            btnDeleteCategory.setOnClickListener(
                    view -> listener.onDeleteCategory(
                            category
                    )
            );
        }

        private void applyCategoryColor(
                String colorHex
        ) {

            String safeColor =
                    "#46C8BE";

            if (colorHex != null
                    && !colorHex.trim().isEmpty()) {

                safeColor =
                        colorHex.trim();
            }

            try {

                GradientDrawable drawable =
                        new GradientDrawable();

                drawable.setShape(
                        GradientDrawable.RECTANGLE
                );

                drawable.setCornerRadius(
                        dpToPx(8)
                );

                drawable.setColor(
                        Color.parseColor(
                                safeColor
                        )
                );

                viewCategoryColor.setBackground(
                        drawable
                );

            } catch (
                    IllegalArgumentException exception
            ) {

                GradientDrawable fallbackDrawable =
                        new GradientDrawable();

                fallbackDrawable.setShape(
                        GradientDrawable.RECTANGLE
                );

                fallbackDrawable.setCornerRadius(
                        dpToPx(8)
                );

                fallbackDrawable.setColor(
                        Color.parseColor(
                                "#46C8BE"
                        )
                );

                viewCategoryColor.setBackground(
                        fallbackDrawable
                );
            }
        }

        private float dpToPx(
                int dp
        ) {

            return dp
                    * itemView
                    .getResources()
                    .getDisplayMetrics()
                    .density;
        }
    }
}