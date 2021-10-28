package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ExerciseGroup {
    public static abstract class Type {
        private static final byte rounds = 0;
        static final byte AMRAP = 1;
        private static final byte decrement = 2;
    }

    public byte type;
    int reps;
    private int completedReps;
    public int index;
    public ExerciseEntry[] exercises;

    ExerciseGroup(JSONObject g) {
        try {
            type = (byte) g.getInt(ExerciseManager.Keys.type);
            reps = g.getInt(ExerciseManager.Keys.reps);
            JSONArray foundExercises = g.getJSONArray("exercises");
            int nExercises = foundExercises.length();
            exercises = new ExerciseEntry[nExercises];
            for (int i = 0; i < nExercises; ++i) {
                JSONObject ex = foundExercises.getJSONObject(i);
                exercises[i] = new ExerciseEntry(ex);
            }
            if (type == 2)
                completedReps = exercises[0].reps;
        } catch (JSONException e) {
            Log.e("ExerciseGroup init", "Error while parsing JSON", e);
        }
    }

    public String createHeader(Context context) {
        if (type == Type.rounds && reps > 1) {
            int completed = completedReps == reps ? reps : completedReps + 1;
            return context.getString(R.string.circuitHeaderRounds, completed, reps);
        } else if (type == Type.AMRAP) {
            return context.getString(R.string.circuitHeaderAMRAP, reps);
        }
        return null;
    }

    boolean didFinish() {
        boolean isDone = false;
        switch (type) {
            case Type.rounds:
                if (++completedReps == reps)
                    isDone = true;
                break;

            case Type.decrement:
                if (--completedReps == 0) {
                    isDone = true;
                } else {
                    for (ExerciseEntry e : exercises) {
                        if (e.type == ExerciseEntry.Type.reps)
                            e.reps -= 1;
                    }
                }
            default:
        }
        return isDone;
    }
}
