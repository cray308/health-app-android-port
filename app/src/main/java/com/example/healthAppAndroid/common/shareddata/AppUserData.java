package com.example.healthAppAndroid.common.shareddata;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.workouts.LiftType;

public class AppUserData {
    public static abstract class Plans {
        public static final byte noPlan = -1;
        public static final byte baseBuilding = 0;
        public static final byte continuation = 1;
    }
    private static abstract class Keys {
        private static final String planStart = "planStart";
        private static final String weekStart = "weekStart";
        private static final String tzOffset = "tzOffset";
        private static final String currentPlan = "currentPlan";
        private static final String completedWorkouts = "completedWorkouts";
        private static final String squatMax = "squatMax";
        private static final String pullUpMax = "pullUpMax";
        private static final String benchMax = "benchMax";
        private static final String deadLiftMax = "deadLiftMax";
    }

    private final SharedPreferences prefs;

    public long planStart;
    public long weekStart;
    public int tzOffset;
    public byte currentPlan = Plans.noPlan;
    public byte completedWorkouts;
    public final short[] liftArray = {0, 0, 0, 0};

    public static AppUserData shared;

    private static SharedPreferences getDict(Context context) {
        return context.getSharedPreferences("HealthAppPrefs", Context.MODE_PRIVATE);
    }

    public static void create(Context context) {
        long now = DateHelper.getCurrentTime();
        shared = new AppUserData(context, now, DateHelper.calcStartOfWeek(now));
        shared.saveData();
    }

    public static int setupFromStorage(Context context) {
        shared = new AppUserData(context);
        long now = DateHelper.getCurrentTime();
        long weekStart = DateHelper.calcStartOfWeek(now);

        int newOffset = DateHelper.getOffsetFromGMT(now);
        int tzDiff = shared.tzOffset - newOffset;
        if (tzDiff != 0) {
            shared.weekStart += tzDiff;
            shared.tzOffset = newOffset;
            shared.saveData();
        }

        if (weekStart != shared.weekStart) {
            shared.completedWorkouts = 0;
            shared.weekStart = weekStart;

            if (shared.currentPlan >= 0) {
                int nWeeks = shared.currentPlan == Plans.baseBuilding ? 8 : 13;
                if (shared.getWeekInPlan() >= nWeeks) {
                    if (shared.currentPlan == Plans.baseBuilding)
                        shared.currentPlan = Plans.continuation;
                    shared.planStart = weekStart;
                }
            }
            shared.saveData();
        }
        return tzDiff;
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
        currentPlan = (byte) prefs.getInt(Keys.currentPlan, Plans.noPlan);
        completedWorkouts = (byte) prefs.getInt(Keys.completedWorkouts, 0);
        liftArray[LiftType.squat] = (short) prefs.getInt(Keys.squatMax, 0);
        liftArray[LiftType.pullUp] = (short) prefs.getInt(Keys.pullUpMax, 0);
        liftArray[LiftType.bench] = (short) prefs.getInt(Keys.benchMax, 0);
        liftArray[LiftType.deadlift] = (short) prefs.getInt(Keys.deadLiftMax, 0);
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
        editor.putInt(Keys.squatMax, liftArray[LiftType.squat]);
        editor.putInt(Keys.pullUpMax, liftArray[LiftType.pullUp]);
        editor.putInt(Keys.benchMax, liftArray[LiftType.bench]);
        editor.putInt(Keys.deadLiftMax, liftArray[LiftType.deadlift]);
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

    public void deleteSavedData() {
        completedWorkouts = 0;
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

    public int getWeekInPlan() { return (int) ((weekStart - planStart) / DateHelper.weekSeconds); }

    public void updateWeightMaxes(short[] newLifts) {
        for (int i = 0; i < 4; ++i) {
            if (newLifts[i] > liftArray[i])
                liftArray[i] = newLifts[i];
        }
        saveData();
    }
}
