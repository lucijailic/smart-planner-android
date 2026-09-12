package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.UserAvailabilityApi;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.UserAvailabilityRequest;
import com.smartplanner.app.storage.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAvailabilityRepository {

    private static final String AVAILABILITY_SELECT =
            "id,user_id,day_of_week,start_time,end_time,is_enabled,created_at,updated_at";

    private static final String CONFLICT_COLUMNS =
            "user_id,day_of_week";


    private final UserAvailabilityApi userAvailabilityApi;

    private final SessionManager sessionManager;


    public UserAvailabilityRepository(
            Context context
    ) {

        Context appContext =
                context.getApplicationContext();


        userAvailabilityApi =
                ApiClient
                        .getClient(appContext)
                        .create(
                                UserAvailabilityApi.class
                        );


        sessionManager =
                SessionManager.getInstance(
                        appContext
                );
    }


    public interface UserAvailabilityCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }


    // =========================================================
    // GET AVAILABILITY
    //
    // Empty list is valid.
    // It means Smart Plan availability has not been configured.
    // =========================================================

    public void getAvailability(
            UserAvailabilityCallback<List<UserAvailability>> callback
    ) {

        String userId =
                sessionManager.getUserId();


        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }


        userAvailabilityApi
                .getAvailability(
                        "eq." + userId,
                        AVAILABILITY_SELECT
                )
                .enqueue(
                        new Callback<List<UserAvailability>>() {

                            @Override
                            public void onResponse(
                                    Call<List<UserAvailability>> call,
                                    Response<List<UserAvailability>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load availability."
                                    );

                                    return;
                                }


                                List<UserAvailability> availability =
                                        response.body();


                                if (availability == null) {

                                    availability =
                                            new ArrayList<>();
                                }


                                callback.onSuccess(
                                        availability
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<UserAvailability>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // SAVE AVAILABILITY
    //
    // All seven days are sent together.
    //
    // Missing rows -> INSERT
    // Existing rows -> UPDATE
    // =========================================================

    public void saveAvailability(
            List<UserAvailabilityRequest> inputRequests,
            UserAvailabilityCallback<List<UserAvailability>> callback
    ) {

        String userId =
                sessionManager.getUserId();


        if (userId == null
                || userId.trim().isEmpty()) {

            callback.onError(
                    "No active user session."
            );

            return;
        }


        if (inputRequests == null
                || inputRequests.size() != 7) {

            callback.onError(
                    "Availability must contain all seven days."
            );

            return;
        }


        List<UserAvailabilityRequest> requests =
                new ArrayList<>();


        for (UserAvailabilityRequest input : inputRequests) {

            if (input == null) {

                callback.onError(
                        "Invalid availability data."
                );

                return;
            }


            String day =
                    normalizeDay(
                            input.getDayOfWeek()
                    );


            if (!isValidDay(day)) {

                callback.onError(
                        "Invalid day of week."
                );

                return;
            }


            String startTime =
                    normalizeTime(
                            input.getStartTime()
                    );

            String endTime =
                    normalizeTime(
                            input.getEndTime()
                    );


            if (!isValidTime(startTime)
                    || !isValidTime(endTime)) {

                callback.onError(
                        "Availability time must use 15-minute intervals."
                );

                return;
            }


            if (!isEndAfterStart(
                    startTime,
                    endTime
            )) {

                callback.onError(
                        "End time must be after start time."
                );

                return;
            }


            requests.add(
                    new UserAvailabilityRequest(
                            userId,
                            day,
                            startTime,
                            endTime,
                            input.isEnabled()
                    )
            );
        }


        if (!containsAllDays(requests)) {

            callback.onError(
                    "Availability must contain each day exactly once."
            );

            return;
        }


        userAvailabilityApi
                .saveAvailability(
                        CONFLICT_COLUMNS,
                        AVAILABILITY_SELECT,
                        requests
                )
                .enqueue(
                        new Callback<List<UserAvailability>>() {

                            @Override
                            public void onResponse(
                                    Call<List<UserAvailability>> call,
                                    Response<List<UserAvailability>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to save availability."
                                    );

                                    return;
                                }


                                List<UserAvailability> availability =
                                        response.body();


                                if (availability == null
                                        || availability.isEmpty()) {

                                    callback.onError(
                                            "Availability was not returned."
                                    );

                                    return;
                                }


                                callback.onSuccess(
                                        availability
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<UserAvailability>> call,
                                    Throwable throwable
                            ) {

                                callback.onError(
                                        "Unable to connect. Please try again."
                                );
                            }
                        }
                );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private String normalizeDay(
            String day
    ) {

        if (day == null) {

            return "";
        }


        return day
                .trim()
                .toUpperCase(
                        Locale.US
                );
    }


    private boolean isValidDay(
            String day
    ) {

        switch (day) {

            case "MONDAY":
            case "TUESDAY":
            case "WEDNESDAY":
            case "THURSDAY":
            case "FRIDAY":
            case "SATURDAY":
            case "SUNDAY":

                return true;

            default:

                return false;
        }
    }


    private String normalizeTime(
            String time
    ) {

        if (time == null) {

            return "";
        }


        String value =
                time.trim();



        if (value.length() >= 5) {

            return value.substring(
                    0,
                    5
            );
        }


        return value;
    }


    private boolean isValidTime(
            String time
    ) {

        if (time == null
                || !time.matches(
                "^([01]\\d|2[0-3]):[0-5]\\d$"
        )) {

            return false;
        }


        int minute;


        try {

            minute =
                    Integer.parseInt(
                            time.substring(
                                    3,
                                    5
                            )
                    );

        } catch (Exception exception) {

            return false;
        }


        return minute % 15 == 0;
    }


    private boolean isEndAfterStart(
            String startTime,
            String endTime
    ) {

        int startMinutes =
                timeToMinutes(
                        startTime
                );

        int endMinutes =
                timeToMinutes(
                        endTime
                );


        return startMinutes >= 0
                && endMinutes > startMinutes;
    }


    private int timeToMinutes(
            String time
    ) {

        if (!isValidTime(time)) {

            return -1;
        }


        try {

            int hour =
                    Integer.parseInt(
                            time.substring(
                                    0,
                                    2
                            )
                    );

            int minute =
                    Integer.parseInt(
                            time.substring(
                                    3,
                                    5
                            )
                    );


            return hour * 60
                    + minute;

        } catch (Exception exception) {

            return -1;
        }
    }


    private boolean containsAllDays(
            List<UserAvailabilityRequest> requests
    ) {

        String[] requiredDays = {
                "MONDAY",
                "TUESDAY",
                "WEDNESDAY",
                "THURSDAY",
                "FRIDAY",
                "SATURDAY",
                "SUNDAY"
        };


        for (String requiredDay : requiredDays) {

            int count =
                    0;


            for (UserAvailabilityRequest request : requests) {

                if (requiredDay.equals(
                        request.getDayOfWeek()
                )) {

                    count++;
                }
            }


            if (count != 1) {

                return false;
            }
        }


        return true;
    }
}