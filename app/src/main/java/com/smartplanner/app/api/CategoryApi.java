package com.smartplanner.app.api;

import com.smartplanner.app.models.Category;
import com.smartplanner.app.models.CategoryRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CategoryApi {

    @GET("rest/v1/categories")
    Call<List<Category>> getCategories(
            @Query("user_id") String userFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/categories")
    Call<List<Category>> createCategory(
            @Body CategoryRequest request,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/categories")
    Call<List<Category>> updateCategory(
            @Query("id") String idFilter,
            @Query("select") String select,
            @Body CategoryRequest request
    );

    @DELETE("rest/v1/categories")
    Call<Void> deleteCategory(
            @Query("id") String idFilter
    );
}