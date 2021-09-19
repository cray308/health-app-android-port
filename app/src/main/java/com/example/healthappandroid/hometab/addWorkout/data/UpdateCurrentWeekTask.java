package com.example.healthappandroid.hometab.addWorkout.data;

import android.os.AsyncTask;

import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.shareddata.PersistenceService;
import com.example.healthappandroid.common.shareddata.WeeklyData;
import com.example.healthappandroid.common.shareddata.WeeklyDataDao;
import com.example.healthappandroid.common.workouts.Workout;

public class UpdateCurrentWeekTask extends AsyncTask<Workout, Void, Workout> {
    @Override
    protected Workout doInBackground(Workout... args) {
        Workout workout = args[0];
        WeeklyDataDao dao = PersistenceService.shared.dao();
        if (workout.duration < Workout.MinWorkoutDuration) return workout;
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

        if (workout.newLifts != null) {
            curr.bestSquat = workout.newLifts[0];
            curr.bestPullup = workout.newLifts[1];
            curr.bestBench = workout.newLifts[2];
            curr.bestDeadlift = workout.newLifts[3];
        }

        curr.totalWorkouts += 1;
        PersistenceService.shared.saveChanges(dao, new WeeklyData[]{curr});
        return workout;
    }

    @Override
    protected void onPostExecute(Workout workout) {
        if (workout.newLifts != null) {
            AppUserData.shared.updateWeightMaxes(workout.newLifts);
            AppCoordinator.shared.updateMaxWeights();
        }
    }
}
