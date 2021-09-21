package com.example.healthAppAndroid.homeTab.data;

import android.content.Context;

import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.workouts.ExerciseManager;

import java.time.LocalDateTime;

public class HomeViewModel {
    private static abstract class Time {
        private static final byte morning = 0, afternoon = 1, evening = 2;
    }

    public final String[] workoutNames = {null, null, null, null, null, null, null};
    public byte timeOfDay;

    public HomeViewModel() { updateTimeOfDay(); }

    public void fetchData(Context context) {
        for (int i = 0; i < 7; ++i)
            workoutNames[i] = null;
        byte plan = AppUserData.shared.currentPlan;
        if (plan >= 0 && AppUserData.shared.planStart <= DateHelper.getCurrentTime()) {
            ExerciseManager.setWeeklyWorkoutNames(
                context, plan, AppUserData.shared.getWeekInPlan(), workoutNames);
        }
    }

    public boolean updateTimeOfDay() {
        LocalDateTime localInfo = DateHelper.localTime(DateHelper.getCurrentTime());
        int hour = localInfo.getHour();

        if (hour >= 5 && hour < 12 && timeOfDay != Time.morning) {
            timeOfDay = Time.morning;
            return true;
        } else if (hour >= 12 && hour < 17 && timeOfDay != Time.afternoon) {
            timeOfDay = Time.afternoon;
            return true;
        } else if ((hour < 5 || hour >= 17) && timeOfDay != Time.evening) {
            timeOfDay = Time.evening;
            return true;
        }
        return false;
    }

    public boolean hasWorkoutsForThisWeek() {
        for (int i = 0; i < 7; ++i) {
            if (workoutNames[i] != null) return true;
        }
        return false;
    }

    public boolean shouldShowConfetti(int totalCompletedWorkouts) {
        if (totalCompletedWorkouts == 0) return false;

        int nWorkouts = 0;
        for (int i = 0; i < 7; ++i) {
            if (workoutNames[i] != null)
                ++nWorkouts;
        }
        return nWorkouts == totalCompletedWorkouts;
    }
}
