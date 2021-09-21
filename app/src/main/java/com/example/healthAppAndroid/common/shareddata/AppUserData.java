package com.example.healthAppAndroid.common.shareddata;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.workouts.Workout;

public class AppUserData {
    private static abstract class Keys {
        static final String planStart = "planStart";
        static final String weekStart = "weekStart";
        static final String tzOffset = "tzOffset";
        static final String currentPlan = "currentPlan";
        static final String completedWorkouts = "completedWorkouts";
        static final String squatMax = "squatMax";
        static final String pullUpMax = "pullUpMax";
        static final String benchMax = "benchMax";
        static final String deadLiftMax = "deadLiftMax";
    }
    private final static String userDefaultsKey = "HealthAppPrefs";

    private final SharedPreferences prefs;

    public long planStart, weekStart;
    public int tzOffset;
    public byte currentPlan = -1, completedWorkouts;
    public final Workout.LiftData liftData = new Workout.LiftData();

    public static AppUserData shared;

    private static SharedPreferences getDict(Context context) {
        return context.getSharedPreferences(userDefaultsKey, Context.MODE_PRIVATE);
    }

    public static void setup(Context context, long now, long weekStart) {
        shared = new AppUserData(context, now, weekStart);
        shared.saveData();
    }

    public static void create(Context context) {
        shared = new AppUserData(context);
    }

    private AppUserData(Context context, long now, long weekStart) {
        prefs = getDict(context);
        this.weekStart = weekStart;
        tzOffset = DateHelper.getOffsetFromGMT(now);
    }

    private AppUserData(Context context) {
        prefs = getDict(context);
        planStart = prefs.getLong(Keys.planStart, -1);
        weekStart = prefs.getLong(Keys.weekStart, 0);
        tzOffset = prefs.getInt(Keys.tzOffset, 0);
        currentPlan = (byte) prefs.getInt(Keys.currentPlan, -1);
        completedWorkouts = (byte) prefs.getInt(Keys.completedWorkouts, 0);
        liftData.squat = (short) prefs.getInt(Keys.squatMax, 0);
        liftData.bench = (short) prefs.getInt(Keys.benchMax, 0);
        liftData.pullUp = (short) prefs.getInt(Keys.pullUpMax, 0);
        liftData.deadlift = (short) prefs.getInt(Keys.deadLiftMax, 0);
    }

    public void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        writePrefs(editor);
        editor.apply();
    }

    private void writePrefs(SharedPreferences.Editor editor) {
        editor.putLong(Keys.planStart, planStart);
        editor.putLong(Keys.weekStart, weekStart);
        editor.putInt(Keys.tzOffset, tzOffset);
        editor.putInt(Keys.currentPlan, currentPlan);
        editor.putInt(Keys.completedWorkouts, completedWorkouts);
        editor.putInt(Keys.squatMax, liftData.squat);
        editor.putInt(Keys.benchMax, liftData.bench);
        editor.putInt(Keys.pullUpMax, liftData.pullUp);
        editor.putInt(Keys.deadLiftMax, liftData.deadlift);
    }

    public void setWorkoutPlan(byte plan) {
        if (plan >= 0 && plan != currentPlan) {
            if (BuildConfig.DEBUG) {
                planStart = weekStart;
            } else {
                planStart = weekStart + DateHelper.weekSeconds;
            }
        }
        currentPlan = plan;
        saveData();
    }

    public int checkTimezone(long now) {
        int newOffset = DateHelper.getOffsetFromGMT(now);
        int diff = newOffset - tzOffset;
        if (diff != 0) {
            weekStart += diff;
            tzOffset = newOffset;
            saveData();
        }
        return diff;
    }

    public void deleteSavedData() {
        completedWorkouts = 0;
        saveData();
    }

    public void handleNewWeek(long weekStart) {
        completedWorkouts = 0;
        this.weekStart = weekStart;

        int plan = currentPlan;
        if (plan >= 0) {
            int difference = (int) (weekStart - planStart);
            int nWeeks = plan == 0 ? 8 : 13;
            if ((difference / DateHelper.weekSeconds) >= nWeeks) {
                if (plan == 0)
                    currentPlan = 1;
                planStart = weekStart;
            }
        }
        saveData();
    }

    public int addCompletedWorkout(byte day) {
        int total = 0;
        completedWorkouts |= (1 << day);
        saveData();
        for (short i = 0; i < 7; ++i) {
            if (((1 << i) & completedWorkouts) != 0)
                ++total;
        }
        return total;
    }

    public int getWeekInPlan() {
        return (int) ((weekStart - planStart) / DateHelper.weekSeconds);
    }

    public void updateWeightMaxes(Workout.LiftData newData) {
        if (newData.squat > liftData.squat)
            liftData.squat = newData.squat;
        if (newData.pullUp > liftData.pullUp)
            liftData.pullUp = newData.pullUp;
        if (newData.bench > liftData.bench)
            liftData.bench = newData.bench;
        if (newData.deadlift > liftData.deadlift)
            liftData.deadlift = newData.deadlift;
        saveData();
    }
}
