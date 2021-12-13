package com.example.healthAppAndroid.homeTab.addWorkout;

import androidx.fragment.app.FragmentActivity;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;
import com.example.healthAppAndroid.homeTab.addWorkout.views.AddWorkoutUpdateMaxesDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class WorkoutCoordinator {
    private final static class UpdateHandler implements PersistenceService.Block {
        private final short[] lifts;

        private UpdateHandler(short[] lifts) { this.lifts = lifts; }

        private static UpdateHandler init(short[] lifts) {
            if (lifts == null) return null;
            return new UpdateHandler(lifts);
        }

        public void completion() {
            AppCoordinator.shared.updateMaxWeights(lifts);
        }
    }

    public final Workout workout;
    private final HomeTabCoordinator parent;

    public WorkoutCoordinator(Workout w, HomeTabCoordinator parent) {
        workout = w;
        this.parent = parent;
    }

    private void handleFinishedWorkout(FragmentActivity activity,
                                       BottomSheetDialogFragment dialog, short[] lifts) {
        int totalCompleted = 0;
        if (workout.duration >= Workout.MinWorkoutDuration && workout.day >= 0)
            totalCompleted = AppUserData.shared.addCompletedWorkout(workout.day);

        PersistenceService.updateCurrentWeek(workout, lifts, UpdateHandler.init(lifts));
        if (dialog != null)
            dialog.dismiss();
        parent.finishedAddingWorkout(activity, totalCompleted);
    }

    public void completedWorkout(FragmentActivity activity, BottomSheetDialogFragment dialog,
                                 boolean showModalIfNeeded, short[] lifts) {
        if (showModalIfNeeded)
            workout.setDuration();
        if (showModalIfNeeded &&
            workout.title.equalsIgnoreCase(activity.getString(R.string.workoutTitleTestDay))) {
            AddWorkoutUpdateMaxesDialog modal = new AddWorkoutUpdateMaxesDialog();
            modal.delegate = this;
            modal.show(activity.getSupportFragmentManager(), "AddWorkoutUpdateMaxesDialog");
        } else {
            handleFinishedWorkout(activity, dialog, lifts);
        }
    }

    public void stoppedWorkout(FragmentActivity activity) {
        workout.setDuration();
        if (workout.checkEnduranceDuration()) {
            handleFinishedWorkout(activity, null, null);
        } else {
            PersistenceService.updateCurrentWeek(workout, null, null);
            parent.finishedAddingWorkout(activity, 0);
        }
    }

    public void stopWorkoutFromBackButtonPress() {
        if (workout.startTime != 0) {
            workout.setDuration();
            if (workout.checkEnduranceDuration()) {
                handleFinishedWorkout(null, null, null);
                return;
            }
        }
        PersistenceService.updateCurrentWeek(workout, null, null);
    }
}
