package com.smartplanner.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.UiState;
import com.smartplanner.app.models.enums.ReminderType;
import com.smartplanner.app.repositories.EventRepository;

import java.util.List;

public class EventsViewModel extends AndroidViewModel {

    private final EventRepository eventRepository;

    private final MutableLiveData<UiState<List<Event>>> eventsState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Event>> eventActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Event>> importantActionState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Event>> eventDetailsState =
            new MutableLiveData<>();

    private final MutableLiveData<UiState<Boolean>> deleteActionState =
            new MutableLiveData<>();

    public EventsViewModel(
            @NonNull Application application
    ) {

        super(application);

        eventRepository =
                new EventRepository(
                        application
                );
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public LiveData<UiState<List<Event>>> getEventsState() {
        return eventsState;
    }

    public LiveData<UiState<Event>> getEventActionState() {
        return eventActionState;
    }

    public LiveData<UiState<Event>> getImportantActionState() {
        return importantActionState;
    }

    public LiveData<UiState<Event>> getEventDetailsState() {
        return eventDetailsState;
    }

    public LiveData<UiState<Boolean>> getDeleteActionState() {
        return deleteActionState;
    }

    // =========================================================
    // LOAD EVENTS
    // =========================================================

    public void loadEvents() {

        eventsState.setValue(
                UiState.loading()
        );

        eventRepository.getEvents(
                new EventRepository.EventCallback<List<Event>>() {

                    @Override
                    public void onSuccess(
                            List<Event> result
                    ) {

                        eventsState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        eventsState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    // =========================================================
    // LOAD SINGLE EVENT
    // =========================================================

    public void loadEvent(
            String eventId
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            eventDetailsState.setValue(
                    UiState.error(
                            "Invalid event."
                    )
            );

            return;
        }

        eventDetailsState.setValue(
                UiState.loading()
        );

        eventRepository.getEvent(
                eventId,
                new EventRepository.EventCallback<Event>() {

                    @Override
                    public void onSuccess(
                            Event result
                    ) {

                        eventDetailsState.postValue(
                                UiState.success(result)
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        eventDetailsState.postValue(
                                UiState.error(message)
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
            ReminderType reminderType
    ) {

        eventActionState.setValue(
                UiState.loading()
        );

        eventRepository.createEvent(
                categoryId,
                title,
                description,
                startAt,
                endAt,
                location,
                important,
                reminderType,
                new EventRepository.EventCallback<Event>() {

                    @Override
                    public void onSuccess(
                            Event result
                    ) {

                        eventActionState.postValue(
                                UiState.success(result)
                        );

                        loadEvents();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        eventActionState.postValue(
                                UiState.error(message)
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
            ReminderType reminderType
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            eventActionState.setValue(
                    UiState.error(
                            "Invalid event."
                    )
            );

            return;
        }

        eventActionState.setValue(
                UiState.loading()
        );

        eventRepository.updateEvent(
                eventId,
                categoryId,
                title,
                description,
                startAt,
                endAt,
                location,
                important,
                reminderType,
                new EventRepository.EventCallback<Event>() {

                    @Override
                    public void onSuccess(
                            Event result
                    ) {

                        eventActionState.postValue(
                                UiState.success(result)
                        );

                        if (result != null
                                && result.getId() != null
                                && !result.getId().trim().isEmpty()) {

                            eventDetailsState.postValue(
                                    UiState.success(result)
                            );
                        }

                        loadEvents();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        eventActionState.postValue(
                                UiState.error(message)
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
            boolean important
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            importantActionState.setValue(
                    UiState.error(
                            "Invalid event."
                    )
            );

            return;
        }

        importantActionState.setValue(
                UiState.loading()
        );

        eventRepository.updateImportant(
                eventId,
                important,
                new EventRepository.EventCallback<Event>() {

                    @Override
                    public void onSuccess(
                            Event result
                    ) {

                        importantActionState.postValue(
                                UiState.success(result)
                        );

                        if (result != null
                                && result.getId() != null
                                && !result.getId().trim().isEmpty()) {

                            eventDetailsState.postValue(
                                    UiState.success(result)
                            );
                        }

                        loadEvents();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        importantActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }

    // =========================================================
    // DELETE EVENT
    // =========================================================

    public void deleteEvent(
            String eventId
    ) {

        if (eventId == null
                || eventId.trim().isEmpty()) {

            deleteActionState.setValue(
                    UiState.error(
                            "Invalid event."
                    )
            );

            return;
        }

        deleteActionState.setValue(
                UiState.loading()
        );

        eventRepository.deleteEvent(
                eventId,
                new EventRepository.EventCallback<Void>() {

                    @Override
                    public void onSuccess(
                            Void result
                    ) {

                        deleteActionState.postValue(
                                UiState.success(true)
                        );

                        loadEvents();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        deleteActionState.postValue(
                                UiState.error(message)
                        );
                    }
                }
        );
    }
}