package com.example.healthAppAndroid.core;

import android.content.SharedPreferences;

import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class UserData {
    static abstract class Keys {
        static final String weekStart = "weekStart";
        private static final String planStart = "planStart";
        private static final String tzOffset = "tzOffset";
        private static final String isDST = "isDST";
        private static final String plan = "currentPlan";
        private static final String completedWorkouts = "completedWorkouts";
        private static final String bodyWeight = "weight";
        private static final String[] lifts = {"squatMax", "pullUpMax", "benchMax", "deadLiftMax"};
    }

    static abstract class Mask {
        private static final byte weekStart = 1;
        private static final byte planStart = 2;
        private static final byte tzOffset = 4;
        private static final byte DST = 8;
        static final byte plan = 16;
        private static final byte completedWorkouts = 32;
    }

    private static abstract class Plan {
        private static final byte baseBuilding = 0;
        private static final byte continuation = 1;
    }

    static final class TimeData {
        final long weekStart;
        final int tzOffset;
        int tzDiff;
        int week;
        final boolean isDST;

        TimeData(long now) {
            ZoneId zoneId = ZoneId.systemDefault();
            Instant instant = Instant.ofEpochSecond(now);
            LocalDateTime time = LocalDateTime.ofInstant(instant, zoneId);
            int weekday = time.getDayOfWeek().getValue();
            if (weekday != 1) {
                now = now - Macros.weekSeconds + (((8 - weekday) % 7) * Macros.daySeconds);
                instant = Instant.ofEpochSecond(now);
                time = LocalDateTime.ofInstant(instant, zoneId);
            }
            weekStart = now - (time.getHour() * Macros.hourSeconds) - (time.getMinute() * 60)
                        - time.getSecond();
            tzOffset = OffsetDateTime.ofInstant(instant, zoneId).getOffset().getTotalSeconds();
            isDST = zoneId.getRules().isDaylightSavings(instant);
        }
    }

    private final SharedPreferences prefs;
    public final int[] lifts = {0, 0, 0, 0};
    public long planStart;
    final long weekStart;
    public int weight;
    public byte plan = -1;
    public byte completedWorkouts;

    UserData(SharedPreferences prefs) {
        this.prefs = prefs;
        TimeData timeData = new TimeData(Instant.now().getEpochSecond());
        weekStart = planStart = timeData.weekStart;

        prefs.edit().putLong(Keys.planStart, weekStart)
             .putLong(Keys.weekStart, weekStart)
             .putInt(Keys.tzOffset, timeData.tzOffset)
             .putBoolean(Keys.isDST, timeData.isDST)
             .putInt(Keys.plan, -1)
             .putInt(Keys.completedWorkouts, 0)
             .putInt(Keys.bodyWeight, -1)
             .putInt(Keys.lifts[0], 0)
             .putInt(Keys.lifts[1], 0)
             .putInt(Keys.lifts[2], 0)
             .putInt(Keys.lifts[3], 0).apply();
    }

    UserData(SharedPreferences prefs, TimeData[] timeDataRef) {
        this.prefs = prefs;
        TimeData timeData = new TimeData(Instant.now().getEpochSecond());
        timeDataRef[0] = timeData;
        weekStart = timeData.weekStart;
        planStart = prefs.getLong(Keys.planStart, 0);
        plan = (byte)prefs.getInt(Keys.plan, -1);
        completedWorkouts = (byte)prefs.getInt(Keys.completedWorkouts, 0);
        weight = prefs.getInt(Keys.bodyWeight, -1);
        for (int i = 0; i < 4; ++i) {
            lifts[i] = prefs.getInt(Keys.lifts[i], 0);
        }
        long savedWeekStart = prefs.getLong(Keys.weekStart, 0);
        int savedOffset = prefs.getInt(Keys.tzOffset, 0);
        boolean wasDST = prefs.getBoolean(Keys.isDST, false);

        byte changes = 0;
        timeData.tzDiff = savedOffset - timeData.tzOffset;
        int dstChange = (timeData.isDST ? 1 : 0) - (wasDST ? 1 : 0);
        if (dstChange != 0) {
            changes = Mask.DST;
            if (Math.abs(timeData.tzDiff) != Macros.hourSeconds) {
                timeData.tzDiff += dstChange * Macros.hourSeconds;
            } else {
                timeData.tzDiff = 0;
                changes |= Mask.tzOffset;
            }
        }

        if (timeData.tzDiff != 0) {
            planStart += timeData.tzDiff;
            changes |= (Mask.tzOffset | Mask.planStart);
            if (weekStart != savedWeekStart) {
                changes |= Mask.weekStart;
                savedWeekStart += timeData.tzDiff;
            }
        }

        timeData.week = (int)((weekStart - planStart + Macros.hourSeconds) / Macros.weekSeconds);
        if (weekStart != savedWeekStart) {
            changes |= (Mask.completedWorkouts | Mask.weekStart);
            completedWorkouts = 0;

            if (plan >= 0 && timeData.week >= new int[]{8, 13}[plan]) {
                if (plan == Plan.baseBuilding) {
                    plan = Plan.continuation;
                    changes |= Mask.plan;
                }
                planStart = weekStart;
                changes |= Mask.planStart;
                timeData.week = 0;
            }
        }

        if (changes != 0) {
            SharedPreferences.Editor editor = prefs.edit();
            if ((changes & Mask.weekStart) != 0) editor.putLong(Keys.weekStart, weekStart);
            if ((changes & Mask.planStart) != 0) editor.putLong(Keys.planStart, planStart);
            if ((changes & Mask.tzOffset) != 0) editor.putInt(Keys.tzOffset, timeData.tzOffset);
            if ((changes & Mask.DST) != 0) editor.putBoolean(Keys.isDST, timeData.isDST);
            if ((changes & Mask.plan) != 0) editor.putInt(Keys.plan, plan);
            if ((changes & Mask.completedWorkouts) != 0)
                editor.putInt(Keys.completedWorkouts, completedWorkouts);
            editor.apply();
        }
    }

    private boolean updateWeights(int[] newLifts,
                                  SharedPreferences.Editor editor, boolean canDecrease) {
        boolean madeChange = false;
        for (int i = 0; i < 4; ++i) {
            int newVal = newLifts[i];
            if (newVal > lifts[i] || (canDecrease && newVal < lifts[i])) {
                madeChange = true;
                lifts[i] = newVal;
                editor.putInt(Keys.lifts[i], newVal);
            }
        }
        return madeChange;
    }

    private static void setCurrentWeek() {
        if (Macros.onEmulator()) ExerciseManager.setCurrentWeek(0);
    }

    private void setNewPlanStart() {
        if (Macros.onEmulator()) {
            planStart = weekStart;
        } else {
            planStart = new TimeData(weekStart + Macros.weekSeconds + Macros.hourSeconds).weekStart;
        }
    }

    public int weightToUse() { return weight < 0 ? 165 : weight; }

    boolean update(byte newPlan, int[] weights) {
        SharedPreferences.Editor editor = prefs.edit();
        boolean res = newPlan != plan;
        boolean madeChange = res;
        if (madeChange) {
            plan = newPlan;
            editor.putInt(Keys.plan, newPlan);
            if (newPlan >= 0) {
                setCurrentWeek();
                setNewPlanStart();
                editor.putLong(Keys.planStart, planStart);
            }
        }

        int newWeight = weights[4];
        if (newWeight != weight) {
            madeChange = true;
            weight = newWeight;
            editor.putInt(Keys.bodyWeight, newWeight);
        }

        if (updateWeights(weights, editor, true) || madeChange) editor.apply();
        return res;
    }

    boolean clear() {
        if (completedWorkouts != 0) {
            completedWorkouts = 0;
            prefs.edit().putInt(Keys.completedWorkouts, 0).apply();
            return true;
        }
        return false;
    }

    static final class WorkoutResult {
        byte completedWorkouts;
        boolean updatedWeights;
    }

    WorkoutResult addWorkoutData(byte day, int[] weights) {
        SharedPreferences.Editor editor = prefs.edit();
        WorkoutResult result = new WorkoutResult();
        boolean madeChange = (weights[0] != -1 && updateWeights(weights, editor, false));
        result.updatedWeights = madeChange;

        if (day != -1) {
            madeChange = true;
            completedWorkouts |= (1 << day);
            result.completedWorkouts = completedWorkouts;
            editor.putInt(Keys.completedWorkouts, completedWorkouts);
        }
        if (madeChange) editor.apply();
        return result;
    }
}
