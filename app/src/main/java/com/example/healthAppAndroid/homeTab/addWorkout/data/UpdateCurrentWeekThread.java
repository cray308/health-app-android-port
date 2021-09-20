package com.example.healthAppAndroid.homeTab.addWorkout.data;

import android.os.Handler;
import android.os.Looper;

import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;
import com.example.healthAppAndroid.common.shareddata.WeeklyDataDao;
import com.example.healthAppAndroid.common.workouts.Workout;

public class UpdateCurrentWeekThread extends Thread {
    private final Workout workout;

    public UpdateCurrentWeekThread(Workout workout) {
        this.workout = workout;
    }

    @Override
    public void run() {
        WeeklyDataDao dao = PersistenceService.shared.dao();
        if (workout.duration >= Workout.MinWorkoutDuration) {
            WeeklyData curr = PersistenceService.shared.getCurrentWeek(dao);
            switch (workout.type) {
                case Workout.TypeSE:
                    curr.timeSE += workout.duration;
                    break;
                case Workout.TypeHIC:
                    curr.timeHIC += workout.duration;
                    break;
                case Workout.TypeStrength:
                    curr.timeStrength += workout.duration;
                    break;
                case Workout.TypeEndurance:
                    curr.timeEndurance += workout.duration;
                    break;
            }

            boolean hasNewLifts = workout.newLifts != null;
            if (hasNewLifts) {
                curr.bestSquat = workout.newLifts[0];
                curr.bestPullup = workout.newLifts[1];
                curr.bestBench = workout.newLifts[2];
                curr.bestDeadlift = workout.newLifts[3];
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
