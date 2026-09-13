package com.smartplanner.app.repositories;

import android.content.Context;

import com.smartplanner.app.models.SmartPlan;
import com.smartplanner.app.models.SmartPlanItem;
import com.smartplanner.app.models.enums.SmartPlanItemStatus;
import com.smartplanner.app.models.enums.SmartPlanStatus;
import com.smartplanner.app.models.enums.TaskStatus;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SmartPlanInvalidationRepository {

    // =========================================================
    // REPOSITORY
    // =========================================================

    private final SmartPlanRepository smartPlanRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SmartPlanInvalidationRepository(
            Context context
    ) {

        Context appContext =
                context.getApplicationContext();

        smartPlanRepository =
                new SmartPlanRepository(
                        appContext
                );
    }


    // =========================================================
    // CALLBACK
    //
    // Invalidation is a secondary operation.
    //
    // The original Task/Event CRUD operation may already
    // have succeeded, therefore callers can decide whether
    // they want to surface an invalidation failure.
    // =========================================================

    public interface InvalidationCallback {

        void onComplete();

        void onError(
                String message
        );
    }


    // =========================================================
    // MARK CURRENT PLAN AS NEEDS_UPDATE
    //
    // Safe behavior:
    //
    // no current plan
    //      -> nothing to invalidate
    //
    // NEEDS_UPDATE
    //      -> already invalidated
    //
    // ACTIVE
    //      -> NEEDS_UPDATE
    // =========================================================

    public void markCurrentPlanNeedsUpdate(
            InvalidationCallback callback
    ) {

        smartPlanRepository.getCurrentSmartPlan(
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlan>() {

                    @Override
                    public void onSuccess(
                            SmartPlan currentPlan
                    ) {

                        if (currentPlan == null) {

                            complete(
                                    callback
                            );

                            return;
                        }


                        if (currentPlan.getId() == null
                                || currentPlan
                                .getId()
                                .trim()
                                .isEmpty()) {

                            fail(
                                    callback,
                                    "Invalid current Smart Plan."
                            );

                            return;
                        }


                        /*
                         * getCurrentSmartPlan() returns either
                         * ACTIVE or NEEDS_UPDATE.
                         *
                         * If it is already NEEDS_UPDATE,
                         * invalidation is idempotent.
                         */
                        if (currentPlan.getStatus()
                                == SmartPlanStatus.NEEDS_UPDATE) {

                            complete(
                                    callback
                            );

                            return;
                        }


                        smartPlanRepository.markSmartPlanNeedsUpdate(
                                currentPlan.getId(),
                                new SmartPlanRepository
                                        .SmartPlanCallback<SmartPlan>() {

                                    @Override
                                    public void onSuccess(
                                            SmartPlan result
                                    ) {

                                        complete(
                                                callback
                                        );
                                    }


                                    @Override
                                    public void onError(
                                            String message
                                    ) {

                                        fail(
                                                callback,
                                                message
                                        );
                                    }
                                }
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        fail(
                                callback,
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // TASK STATUS CHANGE
    //
    // Any Task status change affects planning.
    //
    // Special case:
    //
    // Task -> COMPLETED
    //
    // Future PLANNED SmartPlanItems for that Task are removed
    // because work that is already completely finished must
    // not remain scheduled in the future.
    // =========================================================

    public void handleTaskStatusChanged(
            String taskId,
            TaskStatus newStatus,
            InvalidationCallback callback
    ) {

        if (taskId == null
                || taskId.trim().isEmpty()) {

            fail(
                    callback,
                    "Invalid task."
            );

            return;
        }


        if (newStatus == null) {

            fail(
                    callback,
                    "Invalid task status."
            );

            return;
        }


        if (newStatus != TaskStatus.COMPLETED) {

            markCurrentPlanNeedsUpdate(
                    callback
            );

            return;
        }


        removeFuturePlannedItemsForCompletedTask(
                taskId,
                callback
        );
    }


    // =========================================================
    // REMOVE FUTURE PLANNED ITEMS FOR COMPLETED TASK
    // =========================================================

    private void removeFuturePlannedItemsForCompletedTask(
            String taskId,
            InvalidationCallback callback
    ) {

        smartPlanRepository.getCurrentSmartPlan(
                new SmartPlanRepository
                        .SmartPlanCallback<SmartPlan>() {

                    @Override
                    public void onSuccess(
                            SmartPlan currentPlan
                    ) {

                        /*
                         * No current plan means there are no
                         * current future sessions to clean.
                         */
                        if (currentPlan == null) {

                            complete(
                                    callback
                            );

                            return;
                        }


                        if (currentPlan.getId() == null
                                || currentPlan
                                .getId()
                                .trim()
                                .isEmpty()) {

                            fail(
                                    callback,
                                    "Invalid current Smart Plan."
                            );

                            return;
                        }


                        smartPlanRepository.getSmartPlanItems(
                                currentPlan.getId(),
                                new SmartPlanRepository
                                        .SmartPlanCallback<
                                        List<SmartPlanItem>>() {

                                    @Override
                                    public void onSuccess(
                                            List<SmartPlanItem> items
                                    ) {

                                        List<SmartPlanItem>
                                                itemsToDelete =
                                                findFuturePlannedItems(
                                                        items,
                                                        taskId
                                                );


                                        deleteItemsSequentially(
                                                itemsToDelete,
                                                0,
                                                new InvalidationCallback() {

                                                    @Override
                                                    public void onComplete() {

                                                        markCurrentPlanNeedsUpdate(
                                                                callback
                                                        );
                                                    }


                                                    @Override
                                                    public void onError(
                                                            String message
                                                    ) {

                                                        fail(
                                                                callback,
                                                                message
                                                        );
                                                    }
                                                }
                                        );
                                    }


                                    @Override
                                    public void onError(
                                            String message
                                    ) {

                                        fail(
                                                callback,
                                                message
                                        );
                                    }
                                }
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        fail(
                                callback,
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // FIND FUTURE PLANNED ITEMS
    //
    // Historical COMPLETED and SKIPPED items are preserved.
    //
    // Only:
    //
    // same Task
    // PLANNED
    // future session
    //
    // is removed.
    // =========================================================

    private List<SmartPlanItem> findFuturePlannedItems(
            List<SmartPlanItem> items,
            String taskId
    ) {

        List<SmartPlanItem> result =
                new ArrayList<>();


        if (items == null
                || items.isEmpty()) {

            return result;
        }


        ZonedDateTime now =
                ZonedDateTime.now();


        for (SmartPlanItem item : items) {

            if (item == null) {
                continue;
            }


            if (item.getTaskId() == null
                    || !taskId.equals(
                    item.getTaskId()
            )) {

                continue;
            }


            if (item.getStatus()
                    != SmartPlanItemStatus.PLANNED) {

                continue;
            }


            ZonedDateTime plannedStart =
                    parseZonedDateTime(
                            item.getPlannedStart()
                    );


            if (plannedStart == null) {
                continue;
            }


            if (!plannedStart.isAfter(
                    now
            )) {

                continue;
            }


            result.add(
                    item
            );
        }


        return result;
    }


    // =========================================================
    // DELETE ITEMS SEQUENTIALLY
    // =========================================================

    private void deleteItemsSequentially(
            List<SmartPlanItem> items,
            int index,
            InvalidationCallback callback
    ) {

        if (items == null
                || index >= items.size()) {

            complete(
                    callback
            );

            return;
        }


        SmartPlanItem item =
                items.get(
                        index
                );


        if (item == null
                || item.getId() == null
                || item.getId()
                .trim()
                .isEmpty()) {

            deleteItemsSequentially(
                    items,
                    index + 1,
                    callback
            );

            return;
        }


        smartPlanRepository.deleteSmartPlanItem(
                item.getId(),
                new SmartPlanRepository
                        .SmartPlanCallback<Void>() {

                    @Override
                    public void onSuccess(
                            Void result
                    ) {

                        deleteItemsSequentially(
                                items,
                                index + 1,
                                callback
                        );
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        fail(
                                callback,
                                message
                        );
                    }
                }
        );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private ZonedDateTime parseZonedDateTime(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }


        try {

            return ZonedDateTime.parse(
                    value
            );

        } catch (Exception exception) {

            return null;
        }
    }


    private void complete(
            InvalidationCallback callback
    ) {

        if (callback != null) {

            callback.onComplete();
        }
    }


    private void fail(
            InvalidationCallback callback,
            String message
    ) {

        if (callback != null) {

            callback.onError(
                    message != null
                            && !message.trim().isEmpty()
                            ? message
                            : "Unable to update Smart Plan state."
            );
        }
    }
}