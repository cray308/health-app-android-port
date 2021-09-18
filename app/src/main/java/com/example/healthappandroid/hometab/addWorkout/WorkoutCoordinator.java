package com.example.healthappandroid.hometab.addWorkout;

import androidx.fragment.app.FragmentManager;

import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.workouts.Workout;
import com.example.healthappandroid.hometab.addWorkout.data.UpdateCurrentWeekTask;
import com.example.healthappandroid.hometab.addWorkout.views.AddWorkoutUpdateMaxesDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WorkoutCoordinator {
    public final Workout workout;

    public WorkoutCoordinator(Workout w) { this.workout = w; }

    public void handleFinishedWorkout(FragmentManager fm) {
        if (workout.stopType == Workout.StopTypeCompleted) {
            completedWorkout(fm, null, null, true);
        } else {
            new UpdateCurrentWeekTask(workout, null, 0).execute();
        }
    }

    public void completedWorkout(FragmentManager fm, BottomSheetDialogFragment dialog,
                                 short[] weights, boolean showModalIfRequired) {
        if (showModalIfRequired && workout.title.equalsIgnoreCase("test day")) {
            AddWorkoutUpdateMaxesDialog modal = new AddWorkoutUpdateMaxesDialog();
            modal.delegate = this;
            modal.show(fm, "AddWorkoutUpdateMaxesDialog");
            return;
        }

        int totalCompleted = 0;
        if (workout.day >= 0)
            totalCompleted = AppUserData.shared.addCompletedWorkout(workout.day);
        if (dialog != null)
            dialog.dismiss();
        new UpdateCurrentWeekTask(workout, weights, totalCompleted).execute();
    }
}
