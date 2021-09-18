package com.example.healthappandroid.common.workouts;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ExerciseGroup {
    public static final byte TypeRounds = 0;
    public static final byte TypeAMRAP = 1;
    public static final byte TypeDecrement = 2;

    public byte type;
    public int reps, completedReps;
    public int index;
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

    public String createHeader() {
        if (type == TypeRounds && reps > 1) {
            int completed = completedReps == reps ? reps : completedReps + 1;
            return String.format(Locale.US, "Round %d of %d", completed, reps);
        } else if (type == TypeAMRAP) {
            return String.format(Locale.US, "AMRAP %d mins", reps);
        }
        return null;
    }

    public boolean didFinish() {
        boolean isDone = false;
        switch (type) {
            case TypeRounds:
                if (++completedReps == reps)
                    isDone = true;
                break;

            case TypeDecrement:
                if (--completedReps == 0) {
                    isDone = true;
                } else {
                    for (ExerciseEntry e : exercises) {
                        if (e.type == ExerciseEntry.TypeReps)
                            e.reps -= 1;
                    }
                }
            default:
        }
        return isDone;
    }
}
