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

public class WorkoutCoordinator {
    private final static class UpdateHandler implements PersistenceService.Block {
        private final short[] lifts;

        private UpdateHandler(short[] lifts) { this.lifts = lifts; }

        private static UpdateHandler init(short[] lifts) {
            if (lifts == null) return null;
            return new UpdateHandler(lifts);
        }

        public void completion() {
            AppUserData.shared.updateWeightMaxes(lifts);
            AppCoordinator.shared.updateMaxWeights();
        }
    }

    public final Workout workout;
    private final HomeTabCoordinator parent;

    public WorkoutCoordinator(Workout w, HomeTabCoordinator parent) {
        workout = w;
        this.parent = parent;
    }

    private void finishedAddingWorkout(FragmentActivity activity, BottomSheetDialogFragment dialog,
                                       int totalCompletedWorkouts) {
        if (dialog != null)
            dialog.dismiss();
        parent.finishedAddingWorkout(activity, totalCompletedWorkouts);
    }

    public void completedWorkout(FragmentActivity activity,
                                 BottomSheetDialogFragment dialog, boolean showModalIfNeeded) {
        if (showModalIfNeeded &&
            workout.title.equalsIgnoreCase(activity.getString(R.string.workoutTitleTestDay))) {
            AddWorkoutUpdateMaxesDialog modal = new AddWorkoutUpdateMaxesDialog();
            modal.delegate = this;
            modal.workout = workout;
            modal.show(activity.getSupportFragmentManager(), "AddWorkoutUpdateMaxesDialog");
            return;
        }

        int totalCompleted = 0;
        if (workout.day >= 0 && workout.duration >= Workout.MinWorkoutDuration)
            totalCompleted = AppUserData.shared.addCompletedWorkout(workout.day);
        PersistenceService.updateCurrentWeek(workout, UpdateHandler.init(workout.newLifts));
        finishedAddingWorkout(activity, dialog, totalCompleted);
    }

    public void stoppedWorkout(FragmentActivity activity) {
        PersistenceService.updateCurrentWeek(workout, null);
        finishedAddingWorkout(activity, null, 0);
    }

    public void stopWorkoutFromBackButtonPress() {
        if (workout.startTime != 0) {
            workout.setDuration();
            PersistenceService.updateCurrentWeek(workout, null);
        }
    }
}
