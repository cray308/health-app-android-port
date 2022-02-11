package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.homeTab.addWorkout.LiftType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class AppUserData {
    private static abstract class Plans {
        private static final byte baseBuilding = 0;
        private static final byte continuation = 1;
    }
    private static abstract class Keys {
        private static final String planStart = "planStart";
        private static final String weekStart = "weekStart";
        private static final String tzOffset = "tzOffset";
        private static final String currentPlan = "currentPlan";
        private static final String completedWorkouts = "completedWorkouts";
        private static final String bodyWeight = "weight";
        private static final String squatMax = "squatMax";
        private static final String pullUpMax = "pullUpMax";
        private static final String benchMax = "benchMax";
        private static final String deadLiftMax = "deadLiftMax";
    }

    private static long getStartOfDay(long date, LocalDateTime info) {
        int seconds = (info.getHour() * 3600) + (info.getMinute() * 60) + info.getSecond();
        return date - seconds;
    }

    private static long calcStartOfWeek(long date, ZoneId zoneId) {
        LocalDateTime localInfo = LocalDateTime.ofInstant(Instant.ofEpochSecond(date), zoneId);
        int weekday = localInfo.getDayOfWeek().getValue();

        if (weekday == 1) return getStartOfDay(date, localInfo);

        date -= weekSeconds;
        while (weekday != 1) {
            date += 86400;
            weekday = weekday == 7 ? 1 : weekday + 1;
        }
        localInfo = LocalDateTime.ofInstant(Instant.ofEpochSecond(date), zoneId);
        return getStartOfDay(date, localInfo);
    }

    private static int getOffsetFromGMT(long date, ZoneId zoneId) {
        OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(date), zoneId);
        return time.getOffset().getTotalSeconds();
    }

    private final SharedPreferences prefs;

    public long planStart;
    long weekStart;
    private int tzOffset;
    public final short[] liftArray = {0, 0, 0, 0};
    public short weight = -1;
    public byte currentPlan = -1;
    public byte week;
    public byte completedWorkouts;

    final static long weekSeconds = 604800;
    public static AppUserData shared;

    private static SharedPreferences getDict(Context context) {
        return context.getSharedPreferences("HealthAppPrefs", Context.MODE_PRIVATE);
    }

    static void create(Context context) {
        shared = new AppUserData(context, Instant.now().getEpochSecond());
        shared.saveData();
    }

    static int setupFromStorage(Context context) {
        byte[] planLengths = {8, 13};
        boolean madeChange = false;
        shared = new AppUserData(context);
        ZoneId zoneId = ZoneId.systemDefault();
        long now = Instant.now().getEpochSecond();
        long weekStart = calcStartOfWeek(now, zoneId);

        int newOffset = getOffsetFromGMT(now, zoneId);
        int tzDiff = shared.tzOffset - newOffset;
        if (tzDiff != 0) {
            madeChange = true;
            shared.weekStart += tzDiff;
            shared.planStart += tzDiff;
            shared.tzOffset = newOffset;
        }

        shared.week = (byte) ((weekStart - shared.planStart) / weekSeconds);
        if (weekStart != shared.weekStart) {
            madeChange = true;
            shared.completedWorkouts = 0;
            shared.weekStart = weekStart;

            if (shared.currentPlan >= 0 && shared.week >= planLengths[shared.currentPlan]) {
                if (shared.currentPlan == Plans.baseBuilding)
                    shared.currentPlan = Plans.continuation;
                shared.planStart = weekStart;
                shared.week = 0;
            }
        }

        if (madeChange)
            shared.saveData();
        return tzDiff;
    }

    private AppUserData(Context context, long now) {
        prefs = getDict(context);
        ZoneId zoneId = ZoneId.systemDefault();
        weekStart = calcStartOfWeek(now, zoneId);
        planStart = weekStart;
        tzOffset = getOffsetFromGMT(now, zoneId);
    }

    private AppUserData(Context context) {
        prefs = getDict(context);
        planStart = prefs.getLong(Keys.planStart, 0);
        weekStart = prefs.getLong(Keys.weekStart, 0);
        tzOffset = prefs.getInt(Keys.tzOffset, 0);
        currentPlan = (byte) prefs.getInt(Keys.currentPlan, -1);
        completedWorkouts = (byte) prefs.getInt(Keys.completedWorkouts, 0);
        weight = (short) prefs.getInt(Keys.bodyWeight, -1);
        liftArray[LiftType.squat] = (short) prefs.getInt(Keys.squatMax, 0);
        liftArray[LiftType.pullUp] = (short) prefs.getInt(Keys.pullUpMax, 0);
        liftArray[LiftType.bench] = (short) prefs.getInt(Keys.benchMax, 0);
        liftArray[LiftType.deadlift] = (short) prefs.getInt(Keys.deadLiftMax, 0);
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Keys.planStart, planStart);
        editor.putLong(Keys.weekStart, weekStart);
        editor.putInt(Keys.tzOffset, tzOffset);
        editor.putInt(Keys.currentPlan, currentPlan);
        editor.putInt(Keys.completedWorkouts, completedWorkouts);
        editor.putInt(Keys.bodyWeight, weight);
        editor.putInt(Keys.squatMax, liftArray[LiftType.squat]);
        editor.putInt(Keys.pullUpMax, liftArray[LiftType.pullUp]);
        editor.putInt(Keys.benchMax, liftArray[LiftType.bench]);
        editor.putInt(Keys.deadLiftMax, liftArray[LiftType.deadlift]);
        editor.apply();
    }

    void deleteSavedData() {
        if (completedWorkouts != 0) {
            completedWorkouts = 0;
            saveData();
        }
    }

    public byte addCompletedWorkout(byte day) {
        byte total = 0;
        completedWorkouts |= (1 << day);
        saveData();
        for (short i = 0; i < 7; ++i) {
            if (((1 << i) & completedWorkouts) != 0)
                ++total;
        }
        return total;
    }

    boolean updateWeightMaxes(short[] newLifts) {
        boolean madeChange = false;
        for (int i = 0; i < 4; ++i) {
            if (newLifts[i] > liftArray[i]) {
                madeChange = true;
                liftArray[i] = newLifts[i];
            }
        }
        if (madeChange)
            saveData();
        return madeChange;
    }

    void updateSettings(byte plan, short[] newLifts, short newWeight) {
        boolean madeChange = plan != currentPlan;
        if (plan != -1 && madeChange) {
            if (BuildConfig.DEBUG) {
                planStart = weekStart;
                week = 0;
            } else {
                planStart = weekStart + weekSeconds;
            }
        }
        currentPlan = plan;

        if (newWeight != weight) {
            madeChange = true;
            weight = newWeight;
        }

        if (!updateWeightMaxes(newLifts) && madeChange)
            saveData();
    }
}
