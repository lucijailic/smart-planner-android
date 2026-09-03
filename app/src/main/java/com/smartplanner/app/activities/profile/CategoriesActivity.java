package com.smartplanner.app.activities.profile;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartplanner.app.R;
import com.smartplanner.app.adapters.CategoryAdapter;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.viewmodels.CategoriesViewModel;

import java.util.List;

public class CategoriesActivity
        extends AppCompatActivity
        implements CategoryAdapter.CategoryActionListener {

    private RecyclerView recyclerCategories;

    private ProgressBar progressCategories;

    private LinearLayout layoutCategoriesEmpty;
    private LinearLayout layoutCategoriesError;

    private TextView tvCategoriesError;

    private MaterialButton btnAddCategory;
    private MaterialButton btnRetryCategories;

    private CategoryAdapter categoryAdapter;

    private CategoriesViewModel categoriesViewModel;

    private BottomSheetDialog categoryDialog;

    private String selectedColor = "#46C8BE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_categories);

        initViews();
        setupRecyclerView();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {

        recyclerCategories =
                findViewById(R.id.recyclerCategories);

        progressCategories =
                findViewById(R.id.progressCategories);

        layoutCategoriesEmpty =
                findViewById(R.id.layoutCategoriesEmpty);

        layoutCategoriesError =
                findViewById(R.id.layoutCategoriesError);

        tvCategoriesError =
                findViewById(R.id.tvCategoriesError);

        btnAddCategory =
                findViewById(R.id.btnAddCategory);

        btnRetryCategories =
                findViewById(R.id.btnRetryCategories);
    }

    private void setupRecyclerView() {

        categoryAdapter =
                new CategoryAdapter(this);

        recyclerCategories.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerCategories.setAdapter(
                categoryAdapter
        );
    }

    private void setupViewModel() {

        categoriesViewModel =
                new ViewModelProvider(this)
                        .get(CategoriesViewModel.class);

        categoriesViewModel
                .getCategoriesState()
                .observe(
                        this,
                        this::renderCategoriesState
                );

        categoriesViewModel
                .getCategoryActionState()
                .observe(
                        this,
                        this::renderCategoryActionState
                );

        categoriesViewModel
                .getDeleteCategoryState()
                .observe(
                        this,
                        this::renderDeleteCategoryState
                );

        categoriesViewModel.loadCategories();
    }

    private void setupListeners() {

        btnRetryCategories.setOnClickListener(
                view -> categoriesViewModel
                        .loadCategories()
        );

        btnAddCategory.setOnClickListener(
                view -> showCategoryDialog(null)
        );
    }

    private void renderCategoriesState(
            UiState<List<Category>> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                showLoading();
                break;

            case SUCCESS:
                showCategories(
                        state.getData()
                );
                break;

            case ERROR:
                showError(
                        state.getMessage()
                );
                break;
        }
    }

    private void renderCategoryActionState(
            UiState<Category> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                break;

            case SUCCESS:

                if (categoryDialog != null
                        && categoryDialog.isShowing()) {

                    categoryDialog.dismiss();
                }

                Toast.makeText(
                        this,
                        R.string.category_saved,
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case ERROR:

                Toast.makeText(
                        this,
                        state.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void renderDeleteCategoryState(
            UiState<Void> state
    ) {

        if (state == null) {
            return;
        }

        switch (state.getStatus()) {

            case LOADING:
                break;

            case SUCCESS:

                Toast.makeText(
                        this,
                        R.string.category_deleted,
                        Toast.LENGTH_SHORT
                ).show();

                break;

            case ERROR:

                String message =
                        state.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            getString(
                                    R.string.category_delete_error
                            );
                }

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                break;
        }
    }

    private void showCategoryDialog(
            Category category
    ) {

        categoryDialog =
                new BottomSheetDialog(this);

        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_category,
                                null
                        );

        categoryDialog.setContentView(
                dialogView
        );

        TextView tvTitle =
                dialogView.findViewById(
                        R.id.tvCategoryDialogTitle
                );

        TextInputLayout tilCategoryName =
                dialogView.findViewById(
                        R.id.tilCategoryName
                );

        TextInputEditText etCategoryName =
                dialogView.findViewById(
                        R.id.etCategoryName
                );

        View colorTeal =
                dialogView.findViewById(
                        R.id.colorTeal
                );

        View colorBlue =
                dialogView.findViewById(
                        R.id.colorBlue
                );

        View colorPurple =
                dialogView.findViewById(
                        R.id.colorPurple
                );

        View colorOrange =
                dialogView.findViewById(
                        R.id.colorOrange
                );

        View colorPink =
                dialogView.findViewById(
                        R.id.colorPink
                );

        MaterialButton btnCancel =
                dialogView.findViewById(
                        R.id.btnCancelCategory
                );

        MaterialButton btnSave =
                dialogView.findViewById(
                        R.id.btnSaveCategory
                );

        boolean isEdit =
                category != null;

        if (isEdit) {

            tvTitle.setText(
                    R.string.edit_category
            );

            btnSave.setText(
                    R.string.save
            );

            etCategoryName.setText(
                    category.getName()
            );

            if (category.getColor() != null
                    && !category.getColor()
                    .trim()
                    .isEmpty()) {

                selectedColor =
                        category.getColor()
                                .trim();

            } else {

                selectedColor =
                        "#46C8BE";
            }

        } else {

            tvTitle.setText(
                    R.string.add_category
            );

            btnSave.setText(
                    R.string.add_category
            );

            selectedColor =
                    "#46C8BE";
        }

        updateColorSelection(
                colorTeal,
                colorBlue,
                colorPurple,
                colorOrange,
                colorPink
        );

        colorTeal.setOnClickListener(
                view -> {

                    selectedColor =
                            "#46C8BE";

                    updateColorSelection(
                            colorTeal,
                            colorBlue,
                            colorPurple,
                            colorOrange,
                            colorPink
                    );
                }
        );

        colorBlue.setOnClickListener(
                view -> {

                    selectedColor =
                            "#5C8DF6";

                    updateColorSelection(
                            colorTeal,
                            colorBlue,
                            colorPurple,
                            colorOrange,
                            colorPink
                    );
                }
        );

        colorPurple.setOnClickListener(
                view -> {

                    selectedColor =
                            "#8B6FE8";

                    updateColorSelection(
                            colorTeal,
                            colorBlue,
                            colorPurple,
                            colorOrange,
                            colorPink
                    );
                }
        );

        colorOrange.setOnClickListener(
                view -> {

                    selectedColor =
                            "#F4A261";

                    updateColorSelection(
                            colorTeal,
                            colorBlue,
                            colorPurple,
                            colorOrange,
                            colorPink
                    );
                }
        );

        colorPink.setOnClickListener(
                view -> {

                    selectedColor =
                            "#E98AAE";

                    updateColorSelection(
                            colorTeal,
                            colorBlue,
                            colorPurple,
                            colorOrange,
                            colorPink
                    );
                }
        );

        btnCancel.setOnClickListener(
                view -> categoryDialog.dismiss()
        );

        btnSave.setOnClickListener(
                view -> {

                    tilCategoryName.setError(null);

                    String name = "";

                    if (etCategoryName.getText()
                            != null) {

                        name =
                                etCategoryName
                                        .getText()
                                        .toString()
                                        .trim();
                    }

                    if (name.isEmpty()) {

                        tilCategoryName.setError(
                                getString(
                                        R.string.category_name_required
                                )
                        );

                        return;
                    }

                    if (isEdit) {

                        categoriesViewModel
                                .updateCategory(
                                        category.getId(),
                                        name,
                                        category.getIcon(),
                                        selectedColor
                                );

                    } else {

                        categoriesViewModel
                                .createCategory(
                                        name,
                                        null,
                                        selectedColor
                                );
                    }
                }
        );

        categoryDialog.show();
    }

    private void updateColorSelection(
            View colorTeal,
            View colorBlue,
            View colorPurple,
            View colorOrange,
            View colorPink
    ) {

        applyColorCircle(
                colorTeal,
                "#46C8BE"
        );

        applyColorCircle(
                colorBlue,
                "#5C8DF6"
        );

        applyColorCircle(
                colorPurple,
                "#8B6FE8"
        );

        applyColorCircle(
                colorOrange,
                "#F4A261"
        );

        applyColorCircle(
                colorPink,
                "#E98AAE"
        );
    }

    private void applyColorCircle(
            View view,
            String colorHex
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.OVAL
        );

        drawable.setColor(
                Color.parseColor(
                        colorHex
                )
        );

        if (colorHex.equalsIgnoreCase(
                selectedColor
        )) {

            drawable.setStroke(
                    dpToPx(3),
                    Color.parseColor(
                            "#183B5B"
                    )
            );

        } else {

            drawable.setStroke(
                    dpToPx(1),
                    Color.TRANSPARENT
            );
        }

        view.setBackground(
                drawable
        );
    }

    private int dpToPx(
            int dp
    ) {

        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void showLoading() {

        progressCategories.setVisibility(
                View.VISIBLE
        );

        recyclerCategories.setVisibility(
                View.GONE
        );

        layoutCategoriesEmpty.setVisibility(
                View.GONE
        );

        layoutCategoriesError.setVisibility(
                View.GONE
        );
    }

    private void showCategories(
            List<Category> categories
    ) {

        progressCategories.setVisibility(
                View.GONE
        );

        layoutCategoriesError.setVisibility(
                View.GONE
        );

        if (categories == null
                || categories.isEmpty()) {

            recyclerCategories.setVisibility(
                    View.GONE
            );

            layoutCategoriesEmpty.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        layoutCategoriesEmpty.setVisibility(
                View.GONE
        );

        recyclerCategories.setVisibility(
                View.VISIBLE
        );

        categoryAdapter.submitList(
                categories
        );
    }

    private void showError(
            String message
    ) {

        progressCategories.setVisibility(
                View.GONE
        );

        recyclerCategories.setVisibility(
                View.GONE
        );

        layoutCategoriesEmpty.setVisibility(
                View.GONE
        );

        layoutCategoriesError.setVisibility(
                View.VISIBLE
        );

        if (message == null
                || message.trim()
                .isEmpty()) {

            tvCategoriesError.setText(
                    R.string.categories_error
            );

        } else {

            tvCategoriesError.setText(
                    message
            );
        }
    }

    @Override
    public void onEditCategory(
            Category category
    ) {

        showCategoryDialog(
                category
        );
    }

    @Override
    public void onDeleteCategory(
            Category category
    ) {

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string.delete_category_title
                )
                .setMessage(
                        getString(
                                R.string.delete_category_message,
                                category.getName()
                        )
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .setPositiveButton(
                        R.string.delete_category_confirm,
                        (dialog, which) ->
                                categoriesViewModel
                                        .deleteCategory(
                                                category.getId()
                                        )
                )
                .show();
    }
}