package com.example.healthappandroid.common.shareddata;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.healthappandroid.common.helpers.DateHelper;

public class AppUserData {
    private final static String userDefaultsKey = "HealthAppPrefs";

    private static final String[] keys = {
            "planStart", "weekStart", "tzOffset", "currentPlan", "completedWorkouts",
            "squatMax", "pullUpMax", "benchMax", "deadliftMax"
    };

    private final SharedPreferences prefs;

    public long planStart;
    public long weekStart;
    public int tzOffset;
    public byte currentPlan = -1;
    public byte completedWorkouts;
    public final short[] liftMaxes = {0, 0, 0, 0};

    public static AppUserData shared;

    private static SharedPreferences getDict(Context context) {
        return context.getSharedPreferences(userDefaultsKey, Context.MODE_PRIVATE);
    }

    public static void setup(Context context, long now, long weekStart) {
        AppUserData temp = new AppUserData(context, now, weekStart);
        SharedPreferences.Editor editor = temp.prefs.edit();
        temp.writePrefs(editor);
        editor.commit();
    }

    public static void create(Context context) { shared = new AppUserData(context); }

    private AppUserData(Context context, long now, long weekStart) {
        prefs = getDict(context);
        this.weekStart = weekStart;
        tzOffset = DateHelper.getOffsetFromGMT(now);
    }

    private AppUserData(Context context) {
        prefs = getDict(context);
        planStart = prefs.getLong(keys[0], -1);
        weekStart = prefs.getLong(keys[1], 0);
        tzOffset = prefs.getInt(keys[2], 0);
        currentPlan = (byte) prefs.getInt(keys[3], -1);
        completedWorkouts = (byte) prefs.getInt(keys[4], 0);
        for (int i = 0; i < 4; ++i)
            liftMaxes[i] = (short) prefs.getInt(keys[5 + i], 0);
    }

    public void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        writePrefs(editor);
        editor.apply();
    }

    private void writePrefs(SharedPreferences.Editor editor) {
        editor.putLong(keys[0], planStart);
        editor.putLong(keys[1], weekStart);
        editor.putInt(keys[2], tzOffset);
        editor.putInt(keys[3], currentPlan);
        editor.putInt(keys[4], completedWorkouts);
        for (int i = 0; i < 4; ++i)
            editor.putInt(keys[5 + i], liftMaxes[i]);
    }

    public void setWorkoutPlan(byte plan) {
        if (plan >= 0 && plan != currentPlan)
            planStart = weekStart + DateHelper.weekSeconds;
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

    public int getWeekInPlan() { return (int) ((weekStart - planStart) / DateHelper.weekSeconds); }

    public void updateWeightMaxes(short[] weights) {
        for (int i = 0; i < 4; ++i) {
            if (weights[i] > liftMaxes[i])
                liftMaxes[i] = weights[i];
        }
        saveData();
    }
}
