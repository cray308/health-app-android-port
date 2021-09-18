package com.example.healthappandroid.hometab.addWorkout.data;

import android.os.AsyncTask;

import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.shareddata.PersistenceService;
import com.example.healthappandroid.common.shareddata.WeeklyData;
import com.example.healthappandroid.common.workouts.Workout;

public class UpdateCurrentWeekTask extends AsyncTask<Void, Void, Void> {
    private final int totalCompletedWorkouts;
    private final long duration;
    private final byte type;
    short[] lifts;

    public UpdateCurrentWeekTask(Workout w, short[] lifts, int totalCompletedWorkouts) {
        this.totalCompletedWorkouts = totalCompletedWorkouts;
        type = w.type;
        duration = (long) ((w.stopTime - w.startTime) / 60.0);
        this.lifts = lifts;
    }

    @Override
    protected Void doInBackground(Void... unused) {
        if (duration < 15) return null;
        WeeklyData curr = PersistenceService.shared.getCurrentWeek();
        switch (type) {
            case Workout.TypeSE:
                curr.timeSE += duration;
                break;
            case Workout.TypeHIC:
                curr.timeHIC += duration;
                break;
            case Workout.TypeStrength:
                curr.timeStrength += duration;
                break;
            case Workout.TypeEndurance:
                curr.timeEndurance += duration;
                break;
        }

        if (lifts != null) {
            curr.bestSquat = lifts[0];
            curr.bestPullup = lifts[1];
            curr.bestBench = lifts[2];
            curr.bestDeadlift = lifts[3];
        }

        curr.totalWorkouts += 1;
        PersistenceService.shared.saveChanges(new WeeklyData[]{curr});
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        if (lifts != null) {
            AppUserData.shared.updateWeightMaxes(lifts);
            AppCoordinator.shared.updateMaxWeights();
        }
        AppCoordinator.shared.homeCoordinator.finishedAddingWorkout(totalCompletedWorkouts);
    }
}
