package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.WorkoutNotifService;

import org.json.JSONException;
import org.json.JSONObject;

public class ExerciseEntry {
    public static final byte TypeReps = 0;
    public static final byte TypeDuration = 1;

    public static final byte StateDisabled = 0;
    public static final byte StateActive = 1;
    public static final byte StateResting = 2;
    public static final byte StateCompleted = 3;

    public byte type, state;
    public int weight, reps, sets = 1, rest, completedSets;
    String name;

    public ExerciseEntry(JSONObject e) {
        try {
            type = (byte) e.getInt(ExerciseManager.typeKey);
            reps = e.getInt(ExerciseManager.repsKey);
            rest = e.getInt("rest");
            name = e.getString("name");
        } catch (JSONException ex) {
            Log.e("ExerciseEntry init", "Error while parsing JSON", ex);
        }
    }

    public String createSetsTitle(Context context) {
        if (sets == 1) return null;
        int completed = completedSets == sets ? sets : completedSets + 1;
        return context.getString(R.string.exerciseHeader, completed, sets);
    }

    public String createTitle(Context context) {
        if (state == StateResting)
            return context.getString(R.string.exerciseTitleRest, rest);
        switch (type) {
            case TypeReps:
                if (weight > 1) {
                    return context.getString(R.string.exerciseTitleRepsWithWeight,
                                             name, reps, weight);
                }
                return context.getString(R.string.exerciseTitleReps, name, reps);
            case TypeDuration:
                if (reps > 120) {
                    double minutes = reps / 60.0;
                    return context.getString(R.string.exerciseTitleDurationMinutes, name, minutes);
                }
                return context.getString(R.string.exerciseTitleDurationSeconds, name, reps);
            default:
                int rowingDist = (5 * reps) / 4;
                return context.getString(R.string.exerciseTitleDistance, reps, rowingDist);
        }
    }

    public boolean cycle(Context context) {
        boolean completed = false;
        switch (state) {
            case StateDisabled:
                state = StateActive;
                if (type == TypeDuration)
                    WorkoutNotifService.scheduleAlarm(
                        context, reps, WorkoutNotifService.NotificationFinishExercise);
                break;

            case StateActive:
                if (rest != 0) {
                    state = StateResting;
                    break;
                }

            case StateResting:
                if (++completedSets == sets) {
                    state = StateCompleted;
                    completed = true;
                } else {
                    state = StateActive;
                }
            default:
        }
        return completed;
    }
}
