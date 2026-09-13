package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.models.Event;
import com.smartplanner.app.models.PlanningPreferences;
import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.Task;
import com.smartplanner.app.models.UserAvailability;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;
import com.smartplanner.app.smartplan.SmartPlanGenerator;
import com.smartplanner.app.smartplan.SmartPlanInput;
import com.smartplanner.app.smartplan.SmartPlanResult;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SmartPlanGenerationRepository {

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserAvailabilityRepository userAvailabilityRepository;
    private final PlanningPreferencesRepository planningPreferencesRepository;
    private final SmartPlanRepository smartPlanRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SmartPlanGenerationRepository(
            Context context
    ) {

        Context appContext =
                context.getApplicationContext();

        taskRepository =
                new TaskRepository(
                        appContext
                );

        eventRepository =
                new EventRepository(
                        appContext
                );

        userAvailabilityRepository =
                new UserAvailabilityRepository(
                        appContext
                );

        planningPreferencesRepository =
                new PlanningPreferencesRepository(
                        appContext
                );

        smartPlanRepository =
                new SmartPlanRepository(
                        appContext
                );
    }

    // =========================================================
    // CALLBACK
    // =========================================================

    public interface SmartPlanGenerationCallback {

        void onSuccess(
                GenerationResult result
        );

        void onError(
                String message
        );
    }

    // =========================================================
    // GENERATION MODE
    // =========================================================

    private enum GenerationMode {

        GENERATE,

        REGENERATE
    }

    // =========================================================
    // GENERATION RESULT
    // =========================================================

    public static class GenerationResult {

        private final SmartPlan smartPlan;

        private final List<SmartPlanItem> smartPlanItems;

        private final SmartPlanResult algorithmResult;

        /*
         * true:
         *
         * A new SmartPlan was successfully persisted.
         *
         * false:
         *
         * Generator returned no planned sessions,
         * therefore no new SmartPlan was persisted.
         */
        private final boolean planCreated;

        public GenerationResult(
                SmartPlan smartPlan,
                List<SmartPlanItem> smartPlanItems,
                SmartPlanResult algorithmResult,
                boolean planCreated
        ) {

            this.smartPlan =
                    smartPlan;

            this.smartPlanItems =
                    smartPlanItems != null
                            ? smartPlanItems
                            : new ArrayList<>();

            this.algorithmResult =
                    algorithmResult;

            this.planCreated =
                    planCreated;
        }

        public SmartPlan getSmartPlan() {
            return smartPlan;
        }

        public List<SmartPlanItem> getSmartPlanItems() {
            return smartPlanItems;
        }

        public SmartPlanResult getAlgorithmResult() {
            return algorithmResult;
        }

        public boolean isPlanCreated() {
            return planCreated;
        }
    }

    // =========================================================
    // GENERATE SMART PLAN
    //
    // Used only when there is NO current SmartPlan.
    // =========================================================

    public void generateSmartPlan(
            SmartPlanGenerationCallback callback
    ) {

        if (callback == null) {
            return;
        }

        smartPlanRepository
                .getCurrentSmartPlan(
                        new SmartPlanRepository
                                .SmartPlanCallback<SmartPlan>() {

                            @Override
                            public void onSuccess(
                                    SmartPlan currentPlan
                            ) {

                                if (currentPlan != null) {

                                    callback.onError(
                                            "A Smart Plan already exists. Use Regenerate instead."
                                    );

                                    return;
                                }

                                startGenerationPipeline(
                                        GenerationMode.GENERATE,
                                        null,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // REGENERATE SMART PLAN
    //
    // Used only when an ACTIVE or NEEDS_UPDATE
    // SmartPlan already exists.
    //
    // Persistence is atomic through:
    //
    // public.regenerate_smart_plan(...)
    // =========================================================

    public void regenerateSmartPlan(
            SmartPlanGenerationCallback callback
    ) {

        if (callback == null) {
            return;
        }

        smartPlanRepository
                .getCurrentSmartPlan(
                        new SmartPlanRepository
                                .SmartPlanCallback<SmartPlan>() {

                            @Override
                            public void onSuccess(
                                    SmartPlan currentPlan
                            ) {

                                if (currentPlan == null) {

                                    callback.onError(
                                            "No current Smart Plan exists. Use Generate instead."
                                    );

                                    return;
                                }

                                startGenerationPipeline(
                                        GenerationMode.REGENERATE,
                                        currentPlan,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // START SHARED PIPELINE
    // =========================================================

    private void startGenerationPipeline(
            GenerationMode mode,
            SmartPlan currentPlan,
            SmartPlanGenerationCallback callback
    ) {

        loadTasks(
                mode,
                currentPlan,
                callback
        );
    }

    // =========================================================
    // STEP 1
    // LOAD TASKS
    // =========================================================

    private void loadTasks(
            GenerationMode mode,
            SmartPlan currentPlan,
            SmartPlanGenerationCallback callback
    ) {

        taskRepository
                .getTasks(
                        new TaskRepository
                                .TaskCallback<List<Task>>() {

                            @Override
                            public void onSuccess(
                                    List<Task> tasks
                            ) {

                                List<Task> safeTasks =
                                        tasks != null
                                                ? tasks
                                                : new ArrayList<>();

                                loadEvents(
                                        mode,
                                        currentPlan,
                                        safeTasks,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // STEP 2
    // LOAD EVENTS
    // =========================================================

    private void loadEvents(
            GenerationMode mode,
            SmartPlan currentPlan,
            List<Task> tasks,
            SmartPlanGenerationCallback callback
    ) {

        eventRepository
                .getEvents(
                        new EventRepository
                                .EventCallback<List<Event>>() {

                            @Override
                            public void onSuccess(
                                    List<Event> events
                            ) {

                                List<Event> safeEvents =
                                        events != null
                                                ? events
                                                : new ArrayList<>();

                                loadAvailability(
                                        mode,
                                        currentPlan,
                                        tasks,
                                        safeEvents,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // STEP 3
    // LOAD AVAILABILITY
    // =========================================================

    private void loadAvailability(
            GenerationMode mode,
            SmartPlan currentPlan,
            List<Task> tasks,
            List<Event> events,
            SmartPlanGenerationCallback callback
    ) {

        userAvailabilityRepository
                .getAvailability(
                        new UserAvailabilityRepository
                                .UserAvailabilityCallback<
                                List<UserAvailability>>() {

                            @Override
                            public void onSuccess(
                                    List<UserAvailability> availability
                            ) {

                                List<UserAvailability> safeAvailability =
                                        availability != null
                                                ? availability
                                                : new ArrayList<>();

                                if (safeAvailability.isEmpty()) {

                                    callback.onError(
                                            "Please complete Smart Plan availability setup first."
                                    );

                                    return;
                                }

                                loadPreferences(
                                        mode,
                                        currentPlan,
                                        tasks,
                                        events,
                                        safeAvailability,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // STEP 4
    // LOAD PLANNING PREFERENCES
    // =========================================================

    private void loadPreferences(
            GenerationMode mode,
            SmartPlan currentPlan,
            List<Task> tasks,
            List<Event> events,
            List<UserAvailability> availability,
            SmartPlanGenerationCallback callback
    ) {

        planningPreferencesRepository
                .getCurrentPreferences(
                        new PlanningPreferencesRepository
                                .PlanningPreferencesCallback<
                                PlanningPreferences>() {

                            @Override
                            public void onSuccess(
                                    PlanningPreferences preferences
                            ) {

                                if (preferences == null) {

                                    callback.onError(
                                            "Please complete Smart Plan planning preferences first."
                                    );

                                    return;
                                }

                                loadSmartPlanHistory(
                                        mode,
                                        currentPlan,
                                        tasks,
                                        events,
                                        availability,
                                        preferences,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // STEP 5
    // LOAD SMART PLAN HISTORY
    // =========================================================

    private void loadSmartPlanHistory(
            GenerationMode mode,
            SmartPlan currentPlan,
            List<Task> tasks,
            List<Event> events,
            List<UserAvailability> availability,
            PlanningPreferences preferences,
            SmartPlanGenerationCallback callback
    ) {

        smartPlanRepository
                .getUserSmartPlanItems(
                        new SmartPlanRepository
                                .SmartPlanCallback<
                                List<SmartPlanItem>>() {

                            @Override
                            public void onSuccess(
                                    List<SmartPlanItem> allItems
                            ) {

                                /*
                                 * Full Generate / Regenerate uses only
                                 * COMPLETED historical sessions.
                                 *
                                 * COMPLETED:
                                 * reduces remaining Task duration.
                                 *
                                 * SKIPPED:
                                 * does NOT reduce duration.
                                 *
                                 * PLANNED:
                                 * must NOT block a newly generated plan.
                                 */
                                List<SmartPlanItem> completedHistory =
                                        filterCompletedItems(
                                                allItems
                                        );

                                runGenerator(
                                        mode,
                                        currentPlan,
                                        tasks,
                                        events,
                                        availability,
                                        preferences,
                                        completedHistory,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // STEP 6
    // RUN PURE GENERATOR
    // =========================================================

    private void runGenerator(
            GenerationMode mode,
            SmartPlan currentPlan,
            List<Task> tasks,
            List<Event> events,
            List<UserAvailability> availability,
            PlanningPreferences preferences,
            List<SmartPlanItem> completedHistory,
            SmartPlanGenerationCallback callback
    ) {

        ZonedDateTime now =
                ZonedDateTime.now();

        LocalDate periodStart =
                now.toLocalDate();

        LocalDate periodEnd =
                periodStart.plusDays(6);

        SmartPlanInput input =
                new SmartPlanInput(
                        tasks,
                        events,
                        availability,
                        preferences,
                        completedHistory,
                        periodStart,
                        periodEnd,
                        now
                );

        SmartPlanResult smartPlanResult;

        try {

            smartPlanResult =
                    SmartPlanGenerator.generate(
                            input
                    );

        } catch (Exception exception) {

            callback.onError(
                    "Unable to generate Smart Plan."
            );

            return;
        }

        if (smartPlanResult == null) {

            callback.onError(
                    "Unable to generate Smart Plan."
            );

            return;
        }

        // =====================================================
        // NO PLANNED SESSIONS
        // =====================================================

        if (!smartPlanResult.hasPlannedItems()) {

            /*
             * GENERATE:
             *
             * No empty parent SmartPlan is created.
             *
             *
             * REGENERATE:
             *
             * Existing current plan remains untouched.
             *
             * UI can still show algorithm warnings and
             * unscheduled Tasks.
             */

            callback.onSuccess(
                    new GenerationResult(
                            currentPlan,
                            new ArrayList<>(),
                            smartPlanResult,
                            false
                    )
            );

            return;
        }

        // =====================================================
        // PERSIST RESULT
        // =====================================================

        if (mode == GenerationMode.REGENERATE) {

            persistRegeneratedSmartPlan(
                    periodStart,
                    periodEnd,
                    smartPlanResult,
                    callback
            );

        } else {

            persistFirstSmartPlan(
                    periodStart,
                    periodEnd,
                    smartPlanResult,
                    callback
            );
        }
    }

    // =========================================================
    // FIRST GENERATE
    //
    // Atomic persistence through PostgreSQL RPC:
    //
    // public.create_smart_plan(...)
    //
    // Parent SmartPlan + all generated items are created
    // inside one database transaction.
    // =========================================================

    private void persistFirstSmartPlan(
            LocalDate periodStart,
            LocalDate periodEnd,
            SmartPlanResult algorithmResult,
            SmartPlanGenerationCallback callback
    ) {

        smartPlanRepository
                .createSmartPlanAtomically(
                        periodStart,
                        periodEnd,
                        algorithmResult.getPlannedItems(),
                        new SmartPlanRepository
                                .SmartPlanCallback<SmartPlan>() {

                            @Override
                            public void onSuccess(
                                    SmartPlan createdPlan
                            ) {

                                if (!hasValidPlan(
                                        createdPlan
                                )) {

                                    callback.onError(
                                            "Smart Plan creation failed."
                                    );

                                    return;
                                }

                                /*
                                 * Parent and items are already stored
                                 * atomically by PostgreSQL.
                                 *
                                 * We only fetch the created items so
                                 * GenerationResult contains the actual
                                 * persisted database models.
                                 */
                                loadCreatedSmartPlanItems(
                                        createdPlan,
                                        algorithmResult,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // LOAD ITEMS OF FIRST CREATED PLAN
    // =========================================================

    private void loadCreatedSmartPlanItems(
            SmartPlan createdPlan,
            SmartPlanResult algorithmResult,
            SmartPlanGenerationCallback callback
    ) {

        smartPlanRepository
                .getSmartPlanItems(
                        createdPlan.getId(),
                        new SmartPlanRepository
                                .SmartPlanCallback<
                                List<SmartPlanItem>>() {

                            @Override
                            public void onSuccess(
                                    List<SmartPlanItem> items
                            ) {

                                List<SmartPlanItem> safeItems =
                                        items != null
                                                ? items
                                                : new ArrayList<>();

                                callback.onSuccess(
                                        new GenerationResult(
                                                createdPlan,
                                                safeItems,
                                                algorithmResult,
                                                true
                                        )
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                /*
                                 * Important:
                                 *
                                 * The RPC itself already succeeded.
                                 *
                                 * Parent and all sessions exist in the
                                 * database. Only this follow-up read
                                 * failed.
                                 *
                                 * We do not attempt a client-side
                                 * rollback.
                                 */
                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // REGENERATE
    //
    // Atomic persistence through PostgreSQL RPC.
    // =========================================================

    private void persistRegeneratedSmartPlan(
            LocalDate periodStart,
            LocalDate periodEnd,
            SmartPlanResult algorithmResult,
            SmartPlanGenerationCallback callback
    ) {

        smartPlanRepository
                .regenerateSmartPlan(
                        periodStart,
                        periodEnd,
                        algorithmResult.getPlannedItems(),
                        new SmartPlanRepository
                                .SmartPlanCallback<SmartPlan>() {

                            @Override
                            public void onSuccess(
                                    SmartPlan newPlan
                            ) {

                                if (!hasValidPlan(
                                        newPlan
                                )) {

                                    callback.onError(
                                            "Smart Plan regeneration failed."
                                    );

                                    return;
                                }

                                loadRegeneratedSmartPlanItems(
                                        newPlan,
                                        algorithmResult,
                                        callback
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // LOAD ITEMS OF NEW REGENERATED PLAN
    // =========================================================

    private void loadRegeneratedSmartPlanItems(
            SmartPlan newPlan,
            SmartPlanResult algorithmResult,
            SmartPlanGenerationCallback callback
    ) {

        smartPlanRepository
                .getSmartPlanItems(
                        newPlan.getId(),
                        new SmartPlanRepository
                                .SmartPlanCallback<
                                List<SmartPlanItem>>() {

                            @Override
                            public void onSuccess(
                                    List<SmartPlanItem> items
                            ) {

                                List<SmartPlanItem> safeItems =
                                        items != null
                                                ? items
                                                : new ArrayList<>();

                                callback.onSuccess(
                                        new GenerationResult(
                                                newPlan,
                                                safeItems,
                                                algorithmResult,
                                                true
                                        )
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                callback.onError(
                                        message
                                );
                            }
                        }
                );
    }

    // =========================================================
    // FILTER COMPLETED HISTORY
    // =========================================================

    private List<SmartPlanItem> filterCompletedItems(
            List<SmartPlanItem> items
    ) {

        List<SmartPlanItem> completedItems =
                new ArrayList<>();

        if (items == null
                || items.isEmpty()) {

            return completedItems;
        }

        for (SmartPlanItem item : items) {

            if (item == null) {
                continue;
            }

            if (item.getStatus()
                    != SmartPlanItemStatus.COMPLETED) {

                continue;
            }

            completedItems.add(
                    item
            );
        }

        return completedItems;
    }

    // =========================================================
    // VALIDATE RETURNED PLAN
    // =========================================================

    private boolean hasValidPlan(
            SmartPlan smartPlan
    ) {

        return smartPlan != null
                && smartPlan.getId() != null
                && !smartPlan
                .getId()
                .trim()
                .isEmpty();
    }
}