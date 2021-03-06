package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.MainActivity;

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
        private final float[] weights = {-1, -1, -1, -1};
        int index;
        private final int size;
        final byte type;

        Params(WorkoutParams params, boolean[] isTestDay, int length) {
            short exSets = 1, exReps = 0, circuitReps = 0;
            if (params.type == WorkoutType.strength) {
                MainActivity.UserData data = MainActivity.getUserData();
                short[] lifts = data.liftArray;
                float multiplier = params.weight / 100f;
                weights[0] = multiplier * lifts[0];
                if (params.index <= 1) {
                    weights[1] = multiplier * lifts[LiftType.bench];
                    if (params.index == 0) {
                        int w = data.weightToUse();
                        int weight = ((int)((lifts[LiftType.pullUp] + w) * multiplier) - w);
                        weights[2] = Math.max(weight, 0);
                    } else {
                        weights[2] = multiplier * lifts[LiftType.deadlift];
                    }
                } else {
                    for (int i = 1; i < 4; ++i) weights[i] = lifts[i];
                    isTestDay[0] = true;
                }
                if (MainActivity.metric) {
                    for (int i = 0; i < 4; ++i) weights[i] *= 0.453592f;
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
            size = length;
            type = params.type;
        }
    }

    static void setupHeaderData(Context c) { rounds1 = c.getString(R.string.rounds1); }

    private static String rounds1;
    ExerciseEntry[] exercises;
    final MutableString headerStr = new MutableString();
    int index = 0;
    final short reps;
    short completedReps = 0;
    final byte type;

    Circuit(Context c, JSONObject dict, String[] exNames, Params params) {
        short _reps = params.customCircuitReps;
        byte _type = 0;
        boolean multiple = params.size > 1;

        try {
            _type = (byte)dict.getInt(ExerciseManager.Keys.type);
            ExerciseEntry.Params eParams = new ExerciseEntry.Params(params);

            if (_reps == 0) _reps = (short)dict.getInt(ExerciseManager.Keys.reps);
            if (MainActivity.onEmulator() && _type == 1) _reps = (short)(params.size > 1 ? 1 : 2);

            JSONArray foundExercises = dict.getJSONArray("E");
            int nExercises = foundExercises.length();
            if (params.type == WorkoutType.HIC && _type == 0 && nExercises == 1) {
                eParams.customSets = _reps;
                _reps = 1;
            }
            if (_type == Type.AMRAP) {
                String h;
                if (multiple) {
                    h = c.getString(R.string.circuitAMRAPM, params.index, params.size, _reps);
                } else {
                    h = c.getString(R.string.circuitAMRAP, _reps);
                }
                headerStr.str.append(h);
            } else if (_reps > 1) {
                String h;
                if (multiple) {
                    h = c.getString(R.string.circuitRoundsM, params.index, params.size, 1, _reps);
                } else {
                    h = c.getString(R.string.circuitRounds, 1, _reps);
                }
                headerStr.str.append(h);
                int subIdx = h.indexOf(rounds1);
                int numIdx = h.substring(subIdx, subIdx + rounds1.length()).indexOf(params.one);
                headerStr.index = subIdx + numIdx;
                headerStr.length = params.oneCount;
            } else if (multiple) {
                headerStr.str.append(c.getString(R.string.circuitProgress, params.index, params.size));
            }

            exercises = new ExerciseEntry[nExercises];
            for (int i = 0; i < nExercises; ++i) {
                JSONObject ex = foundExercises.getJSONObject(i);
                if (params.type == WorkoutType.strength) eParams.weight = params.weights[i];
                ExerciseEntry e = new ExerciseEntry(c, ex, exNames, eParams);
                exercises[i] = e;
                if (_type == Type.decrement && e.type == ExerciseEntry.Type.reps) {
                    String ten = String.format(params.l, "%d", 10);
                    e.titleStr.index = e.titleStr.str.indexOf(ten);
                    e.titleStr.length = ten.length();
                }
            }

            if (_type == Type.decrement) completedReps = exercises[0].reps;
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
