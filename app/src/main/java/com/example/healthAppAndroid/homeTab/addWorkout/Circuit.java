package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppUserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

final class Circuit {
    static abstract class Type {
        static final byte rounds = 0;
        static final byte AMRAP = 1;
        static final byte decrement = 2;
    }

    ExerciseEntry[] exercises;
    final MutableString headerStr = new MutableString();
    int index = 0;
    final short reps;
    short completedReps = 0;
    final byte type;

    Circuit(Context context, JSONObject dict, WorkoutParams params, boolean[] isTestDay) {
        short _reps = 0;
        byte _type = 0;
        ExerciseEntry.Params exerciseParams = new ExerciseEntry.Params();
        short[] weights = {0, 0, 0, 0};

        if (params.type == WorkoutType.strength) {
            short[] lifts = AppUserData.shared.liftArray;
            float multiplier = params.weight / 100f;
            weights[0] = (short) (multiplier * lifts[0]);
            if (params.index <= 1) {
                weights[1] = (short) (multiplier * lifts[LiftType.bench]);
                if (params.index == 0) {
                    int weight = ExerciseManager.getBodyWeightToUse();
                    weights[2] = (short) ((int)((lifts[LiftType.pullUp] + weight) * multiplier) - weight);
                    if (weights[2] < 0)
                        weights[2] = 0;
                } else {
                    weights[2] = (short) (multiplier * lifts[LiftType.deadlift]);
                }
            } else {
                System.arraycopy(lifts, 1, weights, 1, 3);
                isTestDay[0] = true;
            }
        }
        try {
            _type = (byte) dict.getInt(ExerciseManager.Keys.type);
            _reps = (short) dict.getInt(ExerciseManager.Keys.reps);

            if (params.type == WorkoutType.SE) {
                _reps = params.sets;
                exerciseParams.customReps = params.reps;
            } else if (params.type == WorkoutType.endurance) {
                exerciseParams.customReps = (short) (params.reps * 60);
            } else if (params.type == WorkoutType.strength) {
                exerciseParams.customReps = params.reps;
                exerciseParams.customSets = params.sets;
            }

            exerciseParams.circuitType = _type;
            JSONArray foundExercises = dict.getJSONArray("exercises");
            int nExercises = foundExercises.length();
            exercises = new ExerciseEntry[nExercises];
            for (int i = 0; i < nExercises; ++i) {
                JSONObject ex = foundExercises.getJSONObject(i);
                if (params.type == WorkoutType.strength)
                    exerciseParams.weight = weights[i];
                exercises[i] = new ExerciseEntry(context, ex, exerciseParams);
            }

            String localHeader = null;
            if (_type == Type.decrement) {
                completedReps = exercises[0].reps;
            } else if (_type == Type.AMRAP) {
                localHeader = context.getString(R.string.circuitHeaderAMRAP, _reps);
            } else if (_reps > 1) {
                localHeader = context.getString(R.string.circuitHeaderRounds, 1, _reps);
                headerStr.index = (short) localHeader.indexOf('1');
                headerStr.end = (short) (headerStr.index + 1);
            }

            if (localHeader != null)
                headerStr.str.append(localHeader);
        } catch (JSONException e) {
            Log.e("Circuit init", "Error while parsing JSON", e);
        }
        type = _type;
        reps = _reps;
    }

    boolean didFinish() {
        if (type == Type.rounds) {
            return ++completedReps == reps;
        } else if (type == Type.decrement) {
            boolean changeRange = completedReps-- == 10;
            if (completedReps == 0) return true;

            String newReps = String.format(Locale.US, "%d", completedReps);
            for (ExerciseEntry e : exercises) {
                if (e.type == ExerciseEntry.Type.reps) {
                    e.titleStr.replace(newReps);
                    if (changeRange)
                        e.titleStr.end -= 1;
                }
            }
        }
        return false;
    }
}
