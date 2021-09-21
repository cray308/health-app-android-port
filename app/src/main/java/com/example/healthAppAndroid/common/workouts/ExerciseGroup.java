package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExerciseGroup {
    public static abstract class Type {
        public static final byte Rounds = 0, AMRAP = 1, Decrement = 2;
    }

    public byte type;
    public int reps, completedReps, index;
    public ExerciseEntry[] exercises;

    public ExerciseGroup(JSONObject g) {
        try {
            type = (byte) g.getInt(ExerciseManager.typeKey);
            reps = g.getInt(ExerciseManager.repsKey);
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
        if (type == Type.Rounds && reps > 1) {
            int completed = completedReps == reps ? reps : completedReps + 1;
            return context.getString(R.string.circuitHeaderRounds, completed, reps);
        } else if (type == Type.AMRAP) {
            return context.getString(R.string.circuitHeaderAMRAP, reps);
        }
        return null;
    }

    public boolean didFinish() {
        boolean isDone = false;
        switch (type) {
            case Type.Rounds:
                if (++completedReps == reps)
                    isDone = true;
                break;

            case Type.Decrement:
                if (--completedReps == 0) {
                    isDone = true;
                } else {
                    for (ExerciseEntry e : exercises) {
                        if (e.type == ExerciseEntry.Type.Reps)
                            e.reps -= 1;
                    }
                }
            default:
        }
        return isDone;
    }
}
