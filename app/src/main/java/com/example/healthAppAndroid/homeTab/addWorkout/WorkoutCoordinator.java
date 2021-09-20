package com.example.healthAppAndroid.homeTab.addWorkout;

import androidx.fragment.app.FragmentActivity;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;
import com.example.healthAppAndroid.homeTab.addWorkout.data.UpdateCurrentWeekThread;
import com.example.healthAppAndroid.homeTab.addWorkout.views.AddWorkoutUpdateMaxesDialog;
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
        new UpdateCurrentWeekThread(workout).start();
        finishedAddingWorkout(activity, dialog, totalCompleted);
    }

    public void stoppedWorkout(FragmentActivity activity) {
        new UpdateCurrentWeekThread(workout).start();
        finishedAddingWorkout(activity, null, 0);
    }

    public void stopWorkoutFromBackButtonPress() {
        if (workout.startTime != 0) {
            workout.setDuration();
            new UpdateCurrentWeekThread(workout).start();
        }
    }
}
