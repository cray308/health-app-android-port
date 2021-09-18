package com.example.healthappandroid.hometab.data;

import android.content.Context;

import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.workouts.ExerciseManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class HomeViewModel {
    static final byte TimeMorning = 0;
    static final byte TimeAfternoon = 1;
    static final byte TimeEvening = 2;

    public String[] workoutNames = {null, null, null, null, null, null, null};
    public byte timeOfDay;

    public HomeViewModel() { updateTimeOfDay(); }

    public void fetchData(Context context) {
        for (int i = 0; i < 7; ++i)
            workoutNames[i] = null;
        byte plan = AppUserData.shared.currentPlan;
        if (plan >= 0 && AppUserData.shared.planStart <= Instant.now().getEpochSecond()) {
            ExerciseManager.setWeeklyWorkoutNames(
                    context, plan, AppUserData.shared.getWeekInPlan(), workoutNames);
        }
    }

    public boolean updateTimeOfDay() {
        long now = Instant.now().getEpochSecond();
        LocalDateTime localInfo = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(now), TimeZone.getDefault().toZoneId());
        int hour = localInfo.getHour();

        if (hour >= 5 && hour < 12 && timeOfDay != TimeMorning) {
            timeOfDay = TimeMorning;
            return true;
        } else if (hour >= 12 && hour < 17 && timeOfDay != TimeAfternoon) {
            timeOfDay = TimeAfternoon;
            return true;
        } else if ((hour < 5 || hour >= 17) && timeOfDay != TimeEvening) {
            timeOfDay = TimeEvening;
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
