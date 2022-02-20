package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;

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
        private static final String weekStart = "weekStart";
        private static final String planStart = "planStart";
        private static final String tzOffset = "tzOffset";
        private static final String currentPlan = "currentPlan";
        private static final String completedWorkouts = "completedWorkouts";
        private static final String bodyWeight = "weight";
        private static final String[] liftKeys = {"squatMax","pullUpMax","benchMax","deadLiftMax"};
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
    private final long weekStart;
    public final short[] liftArray = {0, 0, 0, 0};
    public short weight = -1;
    public byte currentPlan = -1;
    public byte completedWorkouts;

    final static long weekSeconds = 604800;
    public static AppUserData shared;

    private static SharedPreferences getDict(Context context) {
        return context.getSharedPreferences("HealthAppPrefs", Context.MODE_PRIVATE);
    }

    AppUserData(Context context, long[] weekStartArr) {
        long now = Instant.now().getEpochSecond();
        prefs = getDict(context);
        ZoneId zoneId = ZoneId.systemDefault();
        weekStart = calcStartOfWeek(now, zoneId);
        planStart = weekStart;
        int tzOffset = getOffsetFromGMT(now, zoneId);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Keys.planStart, weekStart);
        editor.putLong(Keys.weekStart, weekStart);
        editor.putInt(Keys.tzOffset, tzOffset);
        editor.putInt(Keys.currentPlan, -1);
        editor.putInt(Keys.completedWorkouts, 0);
        editor.putInt(Keys.bodyWeight, -1);
        editor.putInt(Keys.liftKeys[0], 0);
        editor.putInt(Keys.liftKeys[1], 0);
        editor.putInt(Keys.liftKeys[2], 0);
        editor.putInt(Keys.liftKeys[3], 0);
        editor.apply();
        weekStartArr[0] = weekStart;
    }

    AppUserData(Context context, long[] weekStartArr, int[] tzArr, int[] weekArr) {
        byte changes = 0;
        int[] planLengths = {8, 13};
        ZoneId zoneId = ZoneId.systemDefault();
        long now = Instant.now().getEpochSecond();
        long actualWeekStart = calcStartOfWeek(now, zoneId);
        prefs = getDict(context);
        planStart = prefs.getLong(Keys.planStart, 0);
        long _weekStart = prefs.getLong(Keys.weekStart, 0);
        int tzOffset = prefs.getInt(Keys.tzOffset, 0);
        currentPlan = (byte) prefs.getInt(Keys.currentPlan, -1);
        completedWorkouts = (byte) prefs.getInt(Keys.completedWorkouts, 0);

        int newOffset = getOffsetFromGMT(now, zoneId);
        int tzDiff = tzOffset - newOffset;
        if (tzDiff != 0) {
            changes = 7;
            _weekStart += tzDiff;
            planStart += tzDiff;
            tzOffset = newOffset;
        }

        int week = (int) ((actualWeekStart - planStart) / weekSeconds);
        if (actualWeekStart != _weekStart) {
            changes |= 17;
            completedWorkouts = 0;
            _weekStart = actualWeekStart;

            if (currentPlan >= 0 && week >= planLengths[currentPlan]) {
                if (currentPlan == Plans.baseBuilding) {
                    currentPlan = Plans.continuation;
                    changes |= 8;
                }
                planStart = _weekStart;
                changes |= 2;
                week = 0;
            }
        }

        weight = (short) prefs.getInt(Keys.bodyWeight, -1);
        for (int i = 0; i < 4; ++i) {
            liftArray[i] = (short) prefs.getInt(Keys.liftKeys[i], 0);
        }

        if (changes != 0) {
            SharedPreferences.Editor editor = prefs.edit();
            if ((changes & 1) != 0) {
                editor.putLong(Keys.weekStart, _weekStart);
            }
            if ((changes & 2) != 0) {
                editor.putLong(Keys.planStart, planStart);
            }
            if ((changes & 4) != 0) {
                editor.putInt(Keys.tzOffset, tzOffset);
            }
            if ((changes & 8) != 0) {
                editor.putInt(Keys.completedWorkouts, currentPlan);
            }
            if ((changes & 16) != 0) {
                editor.putInt(Keys.completedWorkouts, completedWorkouts);
            }
            editor.apply();
        }

        weekStart = _weekStart;
        tzArr[0] = tzDiff;
        weekStartArr[0] = weekStart;
        weekArr[0] = week;
    }

    void deleteSavedData() {
        if (completedWorkouts != 0) {
            completedWorkouts = 0;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Keys.completedWorkouts, 0);
            editor.apply();
        }
    }

    public byte addCompletedWorkout(byte day) {
        completedWorkouts |= (1 << day);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Keys.completedWorkouts, completedWorkouts);
        editor.apply();
        return completedWorkouts;
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

    boolean updateWeightMaxes(short[] newLifts, short[] output) {
        SharedPreferences.Editor editor = prefs.edit();
        boolean madeChange = updateWeights(newLifts, output, editor);
        if (madeChange)
            editor.apply();
        return madeChange;
    }

    boolean updateSettings(byte plan, short[] newLifts, short newWeight) {
        SharedPreferences.Editor editor = prefs.edit();
        byte changes = (byte) (plan == currentPlan ? 0 : 1);
        if (changes != 0) {
            currentPlan = plan;
            editor.putInt(Keys.currentPlan, plan);
            if (plan >= 0) {
                if (BuildConfig.DEBUG) {
                    planStart = weekStart;
                    ExerciseManager.setWeekStart(0);
                } else {
                    planStart = weekStart + weekSeconds;
                }
                editor.putLong(Keys.planStart, planStart);
            }
        }

        if (newWeight != weight) {
            changes |= 2;
            weight = newWeight;
            editor.putInt(Keys.bodyWeight, newWeight);
        }

        if (!updateWeights(newLifts, new short[]{0, 0, 0, 0}, editor) && changes != 0)
            editor.apply();
        return (changes & 1) != 0;
    }
}
