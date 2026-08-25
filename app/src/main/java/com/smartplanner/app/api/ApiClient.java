package com.smartplanner.app.api;

import android.content.Context;

import com.smartplanner.app.BuildConfig;
import com.smartplanner.app.storage.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static Retrofit retrofit;

    private ApiClient() {
        // Prevent instantiation
    }

    public static synchronized Retrofit getClient(Context context) {

        if (retrofit == null) {

            SessionManager sessionManager =
                    SessionManager.getInstance(context);

            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();

            if (BuildConfig.DEBUG) {
                loggingInterceptor.setLevel(
                        HttpLoggingInterceptor.Level.BASIC
                );
            } else {
                loggingInterceptor.setLevel(
                        HttpLoggingInterceptor.Level.NONE
                );
            }

            OkHttpClient okHttpClient = new OkHttpClient.Builder()

                    // Supabase API key
                    .addInterceptor(chain -> chain.proceed(
                            chain.request()
                                    .newBuilder()
                                    .header(
                                            "apikey",
                                            BuildConfig.SUPABASE_PUBLISHABLE_KEY
                                    )
                                    .build()
                    ))

                    // Logged-in user's JWT
                    .addInterceptor(
                            new AuthInterceptor(sessionManager)
                    )

                    // HTTP logging
                    .addInterceptor(loggingInterceptor)

                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(
                            ensureTrailingSlash(
                                    BuildConfig.SUPABASE_URL
                            )
                    )
                    .client(okHttpClient)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit;
    }

    private static String ensureTrailingSlash(String url) {
        return url.endsWith("/")
                ? url
                : url + "/";
    }
}