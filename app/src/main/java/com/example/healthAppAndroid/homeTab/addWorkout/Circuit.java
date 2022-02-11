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
    final StringRange numberRange = new StringRange();
    @SuppressWarnings("StringBufferField") final StringBuilder headerStr = new StringBuilder(16);
    final byte type;
    final byte reps;
    byte completedReps = 0;
    byte index = 0;

    Circuit(Context context, JSONObject dict, WorkoutParams params) {
        byte _reps = 0;
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
                    short weight = ExerciseManager.getBodyWeightToUse();
                    weights[2] = (short) ((int)((lifts[LiftType.pullUp] + weight) * multiplier) - weight);
                    if (weights[2] < 0)
                        weights[2] = 0;
                } else {
                    weights[2] = (short) (multiplier * lifts[LiftType.deadlift]);
                }
            } else if (params.index == 2) {
                System.arraycopy(lifts, 1, weights, 1, 3);
            }
        }
        try {
            _type = (byte) dict.getInt(ExerciseManager.Keys.type);
            _reps = (byte) dict.getInt(ExerciseManager.Keys.reps);

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
                completedReps = (byte) exercises[0].reps;
            } else if (_type == Type.AMRAP) {
                localHeader = context.getString(R.string.circuitHeaderAMRAP, _reps);
            } else if (_reps > 1) {
                localHeader = context.getString(R.string.circuitHeaderRounds, 1, _reps);
                numberRange.index = (short) localHeader.indexOf('1');
                numberRange.end = (short) (numberRange.index + 1);
            }

            if (localHeader != null)
                headerStr.append(localHeader);
        } catch (JSONException e) {
            Log.e("Circuit init", "Error while parsing JSON", e);
        }
        type = _type;
        reps = _reps;
    }

    boolean didFinish() {
        boolean isDone = false, changeRange;
        switch (type) {
            case Type.rounds:
                if (++completedReps == reps)
                    isDone = true;
                break;

            case Type.decrement:
                changeRange = completedReps == 10;
                if (--completedReps == 0) {
                    isDone = true;
                } else {
                    String newReps = String.format(Locale.US, "%d", completedReps);
                    for (ExerciseEntry e : exercises) {
                        if (e.type == ExerciseEntry.Type.reps) {
                            e.titleStr.replace(e.titleRange.index, e.titleRange.end, newReps);
                            if (changeRange)
                                e.titleRange.end -= 1;
                        }
                    }
                }
            default:
        }
        return isDone;
    }
}
