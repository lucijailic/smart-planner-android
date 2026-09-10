package com.smartplanner.app.repositories;

import android.content.Context;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.smartplanner.app.api.ApiClient;
import com.smartplanner.app.api.EventApi;
import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.EventRequest;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.storage.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private static final String EVENT_SELECT =
            "id,user_id,category_id,title,description,start_at,end_at," +
                    "location,is_important,reminder_type,created_at,updated_at";

    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse(
                    "application/json; charset=utf-8"
            );

    private final EventApi eventApi;
    private final SessionManager sessionManager;

    public EventRepository(
            Context context
    ) {

        eventApi =
                ApiClient
                        .getClient(context)
                        .create(EventApi.class);

        sessionManager =
                SessionManager.getInstance(context);
    }

    public interface EventCallback<T> {

        void onSuccess(T result);

        void onError(String message);
    }

    // =========================================================
    // GET EVENTS
    // =========================================================

    public void getEvents(
            EventCallback<List<Event>> callback
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

        eventApi.getEvents(
                        "eq." + userId,
                        EVENT_SELECT,
                        "start_at.asc"
                )
                .enqueue(
                        new Callback<List<Event>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Event>> call,
                                    Response<List<Event>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load events."
                                    );

                                    return;
                                }

                                List<Event> events =
                                        response.body();

                                if (events == null) {

                                    callback.onError(
                                            "Unable to load events."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        events
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Event>> call,
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
    // GET SINGLE EVENT
    // =========================================================

    public void getEvent(
            String eventId,
            EventCallback<Event> callback
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            callback.onError(
                    "Invalid event."
            );

            return;
        }

        eventApi.getEvent(
                        "eq." + eventId,
                        EVENT_SELECT
                )
                .enqueue(
                        new Callback<List<Event>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Event>> call,
                                    Response<List<Event>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to load event."
                                    );

                                    return;
                                }

                                List<Event> events =
                                        response.body();

                                if (events == null
                                        || events.isEmpty()) {

                                    callback.onError(
                                            "Event not found."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        events.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Event>> call,
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
    // CREATE EVENT
    // =========================================================

    public void createEvent(
            String categoryId,
            String title,
            String description,
            String startAt,
            String endAt,
            String location,
            boolean important,
            ReminderType reminderType,
            EventCallback<Event> callback
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

        String validationError =
                validateEvent(
                        title,
                        startAt,
                        endAt
                );

        if (validationError != null) {

            callback.onError(
                    validationError
            );

            return;
        }

        ReminderType safeReminderType =
                reminderType != null
                        ? reminderType
                        : ReminderType.NONE;

        EventRequest request =
                new EventRequest(
                        userId,
                        normalizeNullableString(categoryId),
                        title.trim(),
                        normalizeNullableString(description),
                        startAt.trim(),
                        endAt.trim(),
                        normalizeNullableString(location),
                        important,
                        safeReminderType
                );

        eventApi.createEvent(
                        request,
                        EVENT_SELECT
                )
                .enqueue(
                        new Callback<List<Event>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Event>> call,
                                    Response<List<Event>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to create event."
                                    );

                                    return;
                                }

                                List<Event> events =
                                        response.body();

                                if (events == null
                                        || events.isEmpty()) {

                                    callback.onError(
                                            "Event creation was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        events.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Event>> call,
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
    // UPDATE EVENT
    // =========================================================

    public void updateEvent(
            String eventId,
            String categoryId,
            String title,
            String description,
            String startAt,
            String endAt,
            String location,
            boolean important,
            ReminderType reminderType,
            EventCallback<Event> callback
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            callback.onError(
                    "Invalid event."
            );

            return;
        }

        String validationError =
                validateEvent(
                        title,
                        startAt,
                        endAt
                );

        if (validationError != null) {

            callback.onError(
                    validationError
            );

            return;
        }

        ReminderType safeReminderType =
                reminderType != null
                        ? reminderType
                        : ReminderType.NONE;

        JsonObject request =
                new JsonObject();

        // -----------------------------------------------------
        // CATEGORY
        // -----------------------------------------------------

        if (categoryId == null
                || categoryId.trim().isEmpty()) {

            request.add(
                    "category_id",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "category_id",
                    categoryId.trim()
            );
        }

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        request.addProperty(
                "title",
                title.trim()
        );

        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        if (description == null
                || description.trim().isEmpty()) {

            request.add(
                    "description",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "description",
                    description.trim()
            );
        }

        // -----------------------------------------------------
        // START
        // -----------------------------------------------------

        request.addProperty(
                "start_at",
                startAt.trim()
        );

        // -----------------------------------------------------
        // END
        // -----------------------------------------------------

        request.addProperty(
                "end_at",
                endAt.trim()
        );

        // -----------------------------------------------------
        // LOCATION
        // -----------------------------------------------------

        if (location == null
                || location.trim().isEmpty()) {

            request.add(
                    "location",
                    JsonNull.INSTANCE
            );

        } else {

            request.addProperty(
                    "location",
                    location.trim()
            );
        }

        // -----------------------------------------------------
        // IMPORTANT
        // -----------------------------------------------------

        request.addProperty(
                "is_important",
                important
        );

        // -----------------------------------------------------
        // REMINDER
        // -----------------------------------------------------

        request.addProperty(
                "reminder_type",
                safeReminderType.name()
        );

        RequestBody requestBody =
                createJsonRequestBody(
                        request
                );

        eventApi.updateEvent(
                        "eq." + eventId,
                        EVENT_SELECT,
                        requestBody
                )
                .enqueue(
                        new Callback<List<Event>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Event>> call,
                                    Response<List<Event>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update event."
                                    );

                                    return;
                                }

                                List<Event> events =
                                        response.body();

                                if (events == null
                                        || events.isEmpty()) {

                                    callback.onError(
                                            "Event update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        events.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Event>> call,
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
    // UPDATE IMPORTANT
    // =========================================================

    public void updateImportant(
            String eventId,
            boolean important,
            EventCallback<Event> callback
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            callback.onError(
                    "Invalid event."
            );

            return;
        }

        JsonObject request =
                new JsonObject();

        request.addProperty(
                "is_important",
                important
        );

        RequestBody requestBody =
                createJsonRequestBody(
                        request
                );

        eventApi.updateImportant(
                        "eq." + eventId,
                        EVENT_SELECT,
                        requestBody
                )
                .enqueue(
                        new Callback<List<Event>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Event>> call,
                                    Response<List<Event>> response
                            ) {

                                if (!response.isSuccessful()) {

                                    callback.onError(
                                            "Unable to update important status."
                                    );

                                    return;
                                }

                                List<Event> events =
                                        response.body();

                                if (events == null
                                        || events.isEmpty()) {

                                    callback.onError(
                                            "Event update was not returned."
                                    );

                                    return;
                                }

                                callback.onSuccess(
                                        events.get(0)
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Event>> call,
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
    // DELETE EVENT
    // =========================================================

    public void deleteEvent(
            String eventId,
            EventCallback<Void> callback
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            callback.onError(
                    "Invalid event."
            );

            return;
        }

        eventApi.deleteEvent(
                        "eq." + eventId
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    callback.onSuccess(
                                            null
                                    );

                                } else {

                                    callback.onError(
                                            "Unable to delete event."
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

    // =========================================================
    // VALIDATION
    // =========================================================

    private String validateEvent(
            String title,
            String startAt,
            String endAt
    ) {

        if (title == null
                || title.trim().isEmpty()) {

            return "Event title is required.";
        }

        if (title.trim().length() > 150) {

            return "Event title is too long.";
        }

        if (startAt == null
                || startAt.trim().isEmpty()) {

            return "Start date and time are required.";
        }

        if (endAt == null
                || endAt.trim().isEmpty()) {

            return "End date and time are required.";
        }

        Date startDate =
                parseSupabaseDate(
                        startAt
                );

        Date endDate =
                parseSupabaseDate(
                        endAt
                );

        if (startDate == null
                || endDate == null) {

            return "Invalid event date or time.";
        }

        if (!endDate.after(startDate)) {

            return "End time must be after start time.";
        }

        return null;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String normalizeNullableString(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private RequestBody createJsonRequestBody(
            JsonObject jsonObject
    ) {

        return RequestBody.create(
                jsonObject.toString(),
                JSON_MEDIA_TYPE
        );
    }

    private Date parseSupabaseDate(
            String value
    ) {

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

            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}