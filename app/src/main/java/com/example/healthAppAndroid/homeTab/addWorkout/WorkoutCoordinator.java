package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.FragmentActivity;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;
import com.example.healthAppAndroid.common.shareddata.WeeklyDataDao;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;
import com.example.healthAppAndroid.homeTab.addWorkout.views.AddWorkoutUpdateMaxesDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WorkoutCoordinator {
    private static class UpdateCurrentWeekThread extends Thread {
        private final Workout workout;
        private UpdateCurrentWeekThread(Workout workout) { this.workout = workout; }

        @Override
        public void run() {
            WeeklyDataDao dao = PersistenceService.shared.dao();
            if (workout.duration >= Workout.MinWorkoutDuration) {
                WeeklyData curr = PersistenceService.shared.getCurrentWeek(dao);
                switch (workout.type) {
                    case Workout.Type.SE:
                        curr.timeSE += workout.duration;
                        break;
                    case Workout.Type.HIC:
                        curr.timeHIC += workout.duration;
                        break;
                    case Workout.Type.Strength:
                        curr.timeStrength += workout.duration;
                        break;
                    default:
                        curr.timeEndurance += workout.duration;
                }

                boolean hasNewLifts = workout.newLifts != null;
                if (hasNewLifts) {
                    curr.bestSquat = workout.newLifts.squat;
                    curr.bestPullup = workout.newLifts.pullUp;
                    curr.bestBench = workout.newLifts.bench;
                    curr.bestDeadlift = workout.newLifts.deadlift;
                }

                curr.totalWorkouts += 1;
                PersistenceService.shared.saveChanges(dao, new WeeklyData[]{curr});
                if (hasNewLifts) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        AppUserData.shared.updateWeightMaxes(workout.newLifts);
                        AppCoordinator.shared.updateMaxWeights();
                    });
                }
            }
        }
    }

    public final Workout workout;
    private final HomeTabCoordinator parent;

    public WorkoutCoordinator(Workout w, HomeTabCoordinator parent) {
        this.workout = w;
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
