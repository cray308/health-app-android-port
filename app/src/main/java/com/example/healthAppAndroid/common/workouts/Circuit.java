package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.StringRange;
import com.example.healthAppAndroid.common.shareddata.AppUserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public final class Circuit {
    public static abstract class Type {
        public static final byte rounds = 0;
        static final byte AMRAP = 1;
        static final byte decrement = 2;
    }

    public final byte type;
    public final int reps;
    public int completedReps = 0;
    public int index = 0;
    public ExerciseEntry[] exercises;
    public final StringRange numberRange = new StringRange();
    @SuppressWarnings("StringBufferField")
    public final StringBuilder headerStr = new StringBuilder(16);

    Circuit(Context context, JSONObject dict, Workout.Params params) {
        int localReps = 0;
        byte localType = 0;
        ExerciseEntry.Params exerciseParams = new ExerciseEntry.Params();
        int[] weights = {0, 0, 0, 0};

        if (params.type == Workout.Type.strength) {
            short[] lifts = AppUserData.shared.liftArray;
            float multiplier = params.weight / 100f;
            weights[0] = (int) (multiplier * lifts[0]);
            if (params.index <= 1) {
                weights[1] = (int) (multiplier * lifts[LiftType.bench]);
                if (params.index == 0) {
                    weights[2] = (int) (multiplier * lifts[LiftType.pullUp]);
                } else {
                    weights[2] = (int) (multiplier * lifts[LiftType.deadlift]);
                }
            } else if (params.index == 2) {
                for (int i = 1; i < 4; ++i) {
                    weights[i] = lifts[i];
                }
            }
        }
        try {
            localType = (byte) dict.getInt(ExerciseManager.Keys.type);
            localReps = dict.getInt(ExerciseManager.Keys.reps);

            if (params.type == Workout.Type.SE) {
                localReps = params.sets;
                exerciseParams.customReps = params.reps;
            } else if (params.type == Workout.Type.endurance) {
                exerciseParams.customReps = params.reps * 60;
            } else if (params.type == Workout.Type.strength) {
                exerciseParams.customReps = params.reps;
                exerciseParams.customSets = params.sets;
            }

            exerciseParams.circuitType = localType;
            JSONArray foundExercises = dict.getJSONArray("exercises");
            int nExercises = foundExercises.length();
            exercises = new ExerciseEntry[nExercises];
            for (int i = 0; i < nExercises; ++i) {
                JSONObject ex = foundExercises.getJSONObject(i);
                if (params.type == Workout.Type.strength)
                    exerciseParams.weight = weights[i];
                exercises[i] = new ExerciseEntry(context, ex, exerciseParams);
            }

            String localHeader = null;
            if (localType == Type.decrement) {
                completedReps = exercises[0].reps;
            } else if (localType == Type.AMRAP) {
                localHeader = context.getString(R.string.circuitHeaderAMRAP, localReps);
            } else if (localReps > 1) {
                localHeader = context.getString(R.string.circuitHeaderRounds, 1, localReps);
                numberRange.index = localHeader.indexOf('1');
                numberRange.end = numberRange.index + 1;
            }

            if (localHeader != null)
                headerStr.append(localHeader);
        } catch (JSONException e) {
            Log.e("Circuit init", "Error while parsing JSON", e);
        }
        type = localType;
        reps = localReps;
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
