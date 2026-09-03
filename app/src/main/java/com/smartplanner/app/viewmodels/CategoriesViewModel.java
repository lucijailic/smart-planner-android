package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.repositories.CategoryRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;

    private final MutableLiveData<UiState<List<Category>>> categoriesState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Category>> categoryActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Void>> deleteCategoryState =
            new MutableLiveData<>();

    public CategoriesViewModel(
            @NonNull Application application
    ) {
        super(application);

        categoryRepository =
                new CategoryRepository(application);
    }

    public LiveData<UiState<List<Category>>> getCategoriesState() {
        return categoriesState;
    }

    public LiveData<UiState<Category>> getCategoryActionState() {
        return categoryActionState;
    }

    public LiveData<UiState<Void>> getDeleteCategoryState() {
        return deleteCategoryState;
    }

    public void loadCategories() {

        categoriesState.setValue(
                UiState.loading()
        );

        categoryRepository.getCategories(
                new CategoryRepository.CategoryCallback<List<Category>>() {

                    @Override
                    public void onSuccess(
                            List<Category> result
                    ) {

                        categoriesState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        categoriesState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void createCategory(
            String name,
            String icon,
            String color
    ) {

        categoryActionState.setValue(
                UiState.loading()
        );

        categoryRepository.createCategory(
                name,
                icon,
                color,
                new CategoryRepository.CategoryCallback<Category>() {

                    @Override
                    public void onSuccess(
                            Category result
                    ) {

                        categoryActionState.postValue(
                                UiState.success(result)
                        );

                        loadCategories();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        categoryActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void updateCategory(
            String categoryId,
            String name,
            String icon,
            String color
    ) {

        categoryActionState.setValue(
                UiState.loading()
        );

        categoryRepository.updateCategory(
                categoryId,
                name,
                icon,
                color,
                new CategoryRepository.CategoryCallback<Category>() {

                    @Override
                    public void onSuccess(
                            Category result
                    ) {

                        categoryActionState.postValue(
                                UiState.success(result)
                        );

                        loadCategories();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        categoryActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    public void deleteCategory(
            String categoryId
    ) {

        deleteCategoryState.setValue(
                UiState.loading()
        );

        categoryRepository.deleteCategory(
                categoryId,
                new CategoryRepository.CategoryCallback<Void>() {

                    @Override
                    public void onSuccess(
                            Void result
                    ) {

                        deleteCategoryState.postValue(
                                UiState.success(null)
                        );

                        loadCategories();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        deleteCategoryState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}