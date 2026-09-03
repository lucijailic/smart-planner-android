package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.CategoryApi;
import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.CategoryRequest;
import com.smartplanner.app.storage.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {

    private static final String CATEGORY_SELECT =
            "id,user_id,name,icon,color,created_at,updated_at";

    private final CategoryApi categoryApi;
    private final SessionManager sessionManager;

    public CategoryRepository(Context context) {

        categoryApi = ApiClient
                .getClient(context)
                .create(CategoryApi.class);

        sessionManager =
                SessionManager.getInstance(context);
    }

    public interface CategoryCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }

    public void getCategories(
            CategoryCallback<List<Category>> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        categoryApi.getCategories(
                        "eq." + userId,
                        CATEGORY_SELECT,
                        "name.asc"
                )
                .enqueue(
                        new Callback<List<Category>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Category>> call,
                                    Response<List<Category>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load categories."
                                    );

                                    return;
                                }

                                List<Category> categories =
                                        response.body();

                                if (categories == null) {

                                    callback.onError(
                                            "Unable to load categories."
                                    );

                                    return;
                                }

                                callback.onSuccess(categories);
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Category>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void createCategory(
            String name,
            String icon,
            String color,
            CategoryCallback<Category> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        CategoryRequest request =
                new CategoryRequest(
                        userId,
                        name,
                        icon,
                        color
                );

        categoryApi.createCategory(
                        request,
                        CATEGORY_SELECT
                )
                .enqueue(
                        new Callback<List<Category>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Category>> call,
                                    Response<List<Category>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    if (response.code() == 409) {

                                        callback.onError(
                                                "A category with this name already exists."
                                        );

                                    } else {

                                        callback.onError(
                                                "Unable to create category."
                                        );
                                    }

                                    return;
                                }

                                List<Category> categories =
                                        response.body();

                                if (categories == null
                                        || categories.isEmpty()) {

                                    callback.onError(
                                            "Category creation was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        categories.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Category>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void updateCategory(
            String categoryId,
            String name,
            String icon,
            String color,
            CategoryCallback<Category> callback
    ) {

        String userId =
                sessionManager.getUserId();

        if (userId == null
                || userId.isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }

        CategoryRequest request =
                new CategoryRequest(
                        userId,
                        name,
                        icon,
                        color
                );

        categoryApi.updateCategory(
                        "eq." + categoryId,
                        CATEGORY_SELECT,
                        request
                )
                .enqueue(
                        new Callback<List<Category>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Category>> call,
                                    Response<List<Category>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    if (response.code() == 409) {

                                        callback.onError(
                                                "A category with this name already exists."
                                        );

                                    } else {

                                        callback.onError(
                                                "Unable to update category."
                                        );
                                    }

                                    return;
                                }

                                List<Category> categories =
                                        response.body();

                                if (categories == null
                                        || categories.isEmpty()) {

                                    callback.onError(
                                            "Category update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        categories.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Category>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }

    public void deleteCategory(
            String categoryId,
            CategoryCallback<Void> callback
    ) {

        categoryApi.deleteCategory(
                        "eq." + categoryId
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    callback.onSuccess(null);

                                } else {

                                    callback.onError(
                                            "Unable to delete category."
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }
}