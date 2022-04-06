package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;

public final class AppUserData {
    private static abstract class Keys {
        private static final String weekStart = "weekStart";
        private static final String planStart = "planStart";
        private static final String tzOffset = "tzOffset";
        private static final String currentPlan = "currentPlan";
        private static final String completedWorkouts = "completedWorkouts";
        private static final String darkMode = "darkMode";
        private static final String bodyWeight = "weight";
        private static final String[] liftKeys = {"squatMax", "pullUpMax", "benchMax", "deadLiftMax"};
    }

    private final SharedPreferences prefs;

    public long planStart;
    private final long weekStart;
    public final short[] liftArray = {0, 0, 0, 0};
    public short weight = -1;
    public byte currentPlan = -1;
    public byte completedWorkouts;
    byte darkMode;

    final static long weekSeconds = 604800;
    public static AppUserData shared;

    private static SharedPreferences getDict(Context c) {
        return c.getSharedPreferences("HealthAppPrefs", Context.MODE_PRIVATE);
    }

    AppUserData(Context c, long start, int offset, boolean modern) {
        prefs = getDict(c);
        weekStart = start;
        planStart = start;
        darkMode = (byte)(modern ? -1 : 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Keys.planStart, start);
        editor.putLong(Keys.weekStart, start);
        editor.putInt(Keys.tzOffset, offset);
        editor.putInt(Keys.currentPlan, -1);
        editor.putInt(Keys.completedWorkouts, 0);
        editor.putInt(Keys.darkMode, darkMode);
        editor.putInt(Keys.bodyWeight, -1);
        editor.putInt(Keys.liftKeys[0], 0);
        editor.putInt(Keys.liftKeys[1], 0);
        editor.putInt(Keys.liftKeys[2], 0);
        editor.putInt(Keys.liftKeys[3], 0);
        editor.apply();
    }

    AppUserData(Context c, int[] output, long start, int offset, boolean modern) {
        byte changes = 0;
        int[] planLengths = {8, 13};
        prefs = getDict(c);
        planStart = prefs.getLong(Keys.planStart, 0);
        weekStart = start;
        long savedWeekStart = prefs.getLong(Keys.weekStart, 0);
        int savedTzOffset = prefs.getInt(Keys.tzOffset, 0);
        currentPlan = (byte)prefs.getInt(Keys.currentPlan, -1);
        completedWorkouts = (byte)prefs.getInt(Keys.completedWorkouts, 0);
        darkMode = (byte)prefs.getInt(Keys.darkMode, -1);

        int tzDiff = savedTzOffset - offset;
        if (tzDiff != 0) {
            planStart += tzDiff;
            if (start != savedWeekStart) {
                changes = 7;
                savedWeekStart += tzDiff;
            } else {
                changes = 6;
                tzDiff = 0;
            }
        }

        int week = (int)((start - planStart) / weekSeconds);
        if (start != savedWeekStart) {
            changes |= 17;
            completedWorkouts = 0;

            if (currentPlan >= 0 && week >= planLengths[currentPlan]) {
                if (currentPlan == 0) {
                    currentPlan = 1;
                    changes |= 8;
                }
                planStart = start;
                changes |= 2;
                week = 0;
            }
        }

        if (darkMode >= 0 && modern) {
            darkMode = -1;
            changes |= 32;
        }

        weight = (short)prefs.getInt(Keys.bodyWeight, -1);
        for (int i = 0; i < 4; ++i) {
            liftArray[i] = (short)prefs.getInt(Keys.liftKeys[i], 0);
        }

        if (changes != 0) {
            SharedPreferences.Editor editor = prefs.edit();
            if ((changes & 1) != 0)
                editor.putLong(Keys.weekStart, start);
            if ((changes & 2) != 0)
                editor.putLong(Keys.planStart, planStart);
            if ((changes & 4) != 0)
                editor.putInt(Keys.tzOffset, offset);
            if ((changes & 8) != 0)
                editor.putInt(Keys.completedWorkouts, currentPlan);
            if ((changes & 16) != 0)
                editor.putInt(Keys.completedWorkouts, completedWorkouts);
            if ((changes & 32) != 0)
                editor.putInt(Keys.darkMode, darkMode);
            editor.apply();
        }

        output[0] = tzDiff;
        output[1] = week;
    }

    boolean deleteSavedData() {
        if (completedWorkouts != 0) {
            completedWorkouts = 0;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Keys.completedWorkouts, 0);
            editor.apply();
            return true;
        }
        return false;
    }

    private boolean updateWeights(short[] newLifts,
                                  short[] output, SharedPreferences.Editor editor) {
        boolean madeChange = false;
        for (int i = 0; i < 4; ++i) {
            short old = liftArray[i];
            short newVal = newLifts[i];
            if (newVal > old) {
                madeChange = true;
                liftArray[i] = newVal;
                editor.putInt(Keys.liftKeys[i], newVal);
                output[i] = newVal;
            } else {
                output[i] = old;
            }
        }
        return madeChange;
    }

    byte addWorkoutData(byte day, short[] weights, short[] output, boolean[] updated) {
        SharedPreferences.Editor editor = prefs.edit();
        byte completed = 0;
        boolean madeChange = false;
        if (weights != null)
            madeChange = updateWeights(weights, output, editor);
        if (day >= 0) {
            madeChange = true;
            completedWorkouts |= (1 << day);
            completed = completedWorkouts;
            editor.putInt(Keys.completedWorkouts, completedWorkouts);
        }
        if (madeChange)
            editor.apply();
        updated[0] = madeChange;
        return completed;
    }

    int updateSettings(byte plan, byte dm, short[] newArr) {
        SharedPreferences.Editor editor = prefs.edit();
        int changes = plan == currentPlan ? 0 : 1;
        if (changes != 0) {
            currentPlan = plan;
            editor.putInt(Keys.currentPlan, plan);
            if (plan >= 0) {
                if (AppCoordinator.shared.onEmulator) {
                    planStart = weekStart;
                    ExerciseManager.setWeekStart(0);
                } else {
                    planStart = weekStart + weekSeconds;
                }
                editor.putLong(Keys.planStart, planStart);
            }
        }

        if (dm != darkMode) {
            changes |= 2;
            darkMode = dm;
            editor.putInt(Keys.darkMode, dm);
        }

        short newWeight = newArr[4];
        if (newWeight != weight) {
            changes |= 4;
            weight = newWeight;
            editor.putInt(Keys.bodyWeight, newWeight);
        }

        if (!updateWeights(newArr, new short[]{0, 0, 0, 0}, editor) && changes != 0)
            editor.apply();
        return changes;
    }
}
