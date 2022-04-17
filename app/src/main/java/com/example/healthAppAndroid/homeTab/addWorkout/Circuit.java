package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppCoordinator;
import com.example.healthAppAndroid.core.AppUserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

final class Circuit {
    static abstract class Type {
        static final byte rounds = 0;
        static final byte AMRAP = 1;
        private static final byte decrement = 2;
    }

    static final class Params {
        private final Locale l = Locale.getDefault();
        final String one;
        final int oneCount;
        final short customSets;
        final short customReps;
        private final short customCircuitReps;
        private final short[] weights = {-1, -1, -1, -1};
        int location;
        private final int nActivities;
        final byte workoutType;

        Params(WorkoutParams params, boolean[] isTestDay, int length) {
            short exSets = 1, exReps = 0, circuitReps = 0;
            if (params.type == WorkoutType.strength) {
                short[] lifts = AppUserData.shared.liftArray;
                float multiplier = params.weight / 100f;
                weights[0] = (short)(multiplier * lifts[0]);
                if (params.index <= 1) {
                    weights[1] = (short)(multiplier * lifts[LiftType.bench]);
                    if (params.index == 0) {
                        int weight = ExerciseManager.getBodyWeightToUse();
                        weights[2] =
                          (short)((int)((lifts[LiftType.pullUp] + weight) * multiplier) - weight);
                        weights[2] = (short)Math.max(weights[2], 0);
                    } else {
                        weights[2] = (short)(multiplier * lifts[LiftType.deadlift]);
                    }
                } else {
                    System.arraycopy(lifts, 1, weights, 1, 3);
                    isTestDay[0] = true;
                }
                exReps = params.reps;
                exSets = params.sets;
            } else if (params.type == WorkoutType.SE) {
                circuitReps = params.sets;
                exReps = params.reps;
            } else if (params.type == WorkoutType.endurance) {
                exReps = (short)(params.reps * 60);
            }
            one = String.format(l, "%d", 1);
            oneCount = one.length();
            customSets = exSets;
            customReps = exReps;
            customCircuitReps = circuitReps;
            nActivities = length;
            workoutType = params.type;
        }
    }

    static void setupHeaderData(Context c) {
        rounds1S = c.getString(R.string.rounds1S);
        rounds1M = c.getString(R.string.rounds1M);
    }

    private static String rounds1S;
    private static String rounds1M;
    ExerciseEntry[] exercises;
    final MutableString headerStr = new MutableString();
    int index = 0;
    final short reps;
    short completedReps = 0;
    final byte type;

    Circuit(Context c, JSONObject dict, String[] exNames, Params params) {
        short _reps = params.customCircuitReps;
        byte _type = 0;
        boolean multiple = params.nActivities > 1;

        try {
            _type = (byte)dict.getInt(ExerciseManager.Keys.type);
            ExerciseEntry.Params exerciseParams = new ExerciseEntry.Params(params);

            if (_reps == 0)
                _reps = (short)dict.getInt(ExerciseManager.Keys.reps);
            if (AppCoordinator.shared.onEmulator && _type == Type.AMRAP)
                _reps = (short)(params.nActivities > 1 ? 1 : 2);

            JSONArray foundExercises = dict.getJSONArray("E");
            int nExercises = foundExercises.length();
            if (params.workoutType == WorkoutType.HIC && _type == 0 && nExercises == 1) {
                exerciseParams.customSets = _reps;
                _reps = 1;
            }
            if (_type == Type.AMRAP) {
                String h;
                if (multiple) {
                    h = c.getString(R.string.circuitHeaderAMRAPM,
                                    params.location, params.nActivities, _reps);
                } else {
                    h = c.getString(R.string.circuitHeaderAMRAP, _reps);
                }
                headerStr.str.append(h);
            } else if (_reps > 1) {
                String h, s;
                if (multiple) {
                    h = c.getString(R.string.circuitHeaderRoundsM,
                                    params.location, params.nActivities, 1, _reps);
                    s = rounds1M;
                } else {
                    h = c.getString(R.string.circuitHeaderRounds, 1, _reps);
                    s = rounds1S;
                }
                headerStr.str.append(h);
                int subIdx = h.indexOf(s);
                String subhead = h.substring(subIdx, subIdx + s.length());
                int numIdx = subhead.indexOf(params.one);
                headerStr.index = subIdx + numIdx;
                headerStr.length = params.oneCount;
            } else if (multiple) {
                headerStr.str.append(
                  c.getString(R.string.circuitProgress, params.location, params.nActivities));
            }

            exercises = new ExerciseEntry[nExercises];
            for (int i = 0; i < nExercises; ++i) {
                JSONObject ex = foundExercises.getJSONObject(i);
                if (params.workoutType == WorkoutType.strength)
                    exerciseParams.weight = params.weights[i];
                ExerciseEntry e = new ExerciseEntry(c, ex, exNames, exerciseParams);
                exercises[i] = e;
                if (_type == Type.decrement && e.type == ExerciseEntry.Type.reps) {
                    String ten = String.format(params.l, "%d", 10);
                    e.titleStr.index = e.titleStr.str.indexOf(ten);
                    e.titleStr.length = ten.length();
                }
            }

            if (_type == Type.decrement)
                completedReps = exercises[0].reps;
        } catch (JSONException ignored) {}
        type = _type;
        reps = _reps;
    }

    boolean didFinish() {
        if (type == Type.rounds) {
            return ++completedReps == reps;
        } else if (type == Type.decrement) {
            if (--completedReps == 0) return true;

            String newReps = String.format(Locale.getDefault(), "%d", completedReps);
            int len = newReps.length();
            for (ExerciseEntry e : exercises) {
                if (e.type == ExerciseEntry.Type.reps) {
                    e.titleStr.replace(newReps);
                    e.titleStr.length = len;
                }
            }
        }
        return false;
    }
}
