package com.example.healthappandroid.hometab.addWorkout;

import androidx.fragment.app.FragmentActivity;

import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.workouts.Workout;
import com.example.healthappandroid.hometab.HomeTabCoordinator;
import com.example.healthappandroid.hometab.addWorkout.data.UpdateCurrentWeekTask;
import com.example.healthappandroid.hometab.addWorkout.views.AddWorkoutUpdateMaxesDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WorkoutCoordinator {
    public final Workout workout;
    private final HomeTabCoordinator parent;

    public WorkoutCoordinator(Workout w, HomeTabCoordinator parent) {
        this.workout = w;
        this.parent = parent;
    }

    private void finishedAddingWorkout(FragmentActivity activity,
                                       BottomSheetDialogFragment dialog,
                                       int totalCompletedWorkouts) {
        if (dialog != null)
            dialog.dismiss();
        parent.finishedAddingWorkout(activity, totalCompletedWorkouts);
    }

    public void completedWorkout(FragmentActivity activity,
                                 BottomSheetDialogFragment dialog, boolean showModalIfNeeded) {
        if (showModalIfNeeded && workout.title.equalsIgnoreCase("test day")) {
            AddWorkoutUpdateMaxesDialog modal = new AddWorkoutUpdateMaxesDialog();
            modal.delegate = this;
            modal.workout = workout;
            modal.show(activity.getSupportFragmentManager(), "AddWorkoutUpdateMaxesDialog");
            return;
        }

        int totalCompleted = 0;
        if (workout.day >= 0 && workout.duration >= Workout.MinWorkoutDuration)
            totalCompleted = AppUserData.shared.addCompletedWorkout(workout.day);
        new UpdateCurrentWeekTask().execute(workout);
        finishedAddingWorkout(activity, dialog, totalCompleted);
    }

    public void stoppedWorkout(FragmentActivity activity) {
        new UpdateCurrentWeekTask().execute(workout);
        finishedAddingWorkout(activity, null, 0);
    }

    public void stopWorkoutFromBackButtonPress() {
        if (workout.startTime != 0) {
            workout.setDuration();
            new UpdateCurrentWeekTask().execute(workout);
        }
    }
}
