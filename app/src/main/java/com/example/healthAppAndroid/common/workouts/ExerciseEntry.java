package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.WorkoutNotifService;

import org.json.JSONException;
import org.json.JSONObject;

public class ExerciseEntry {
    public static abstract class Type {
        public static final byte Reps = 0, Duration = 1;
    }
    public static abstract class State {
        public static final byte Disabled = 0, Active = 1, Resting = 2, Completed = 3;
    }

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
        if (state == State.Resting)
            return context.getString(R.string.exerciseTitleRest, rest);
        switch (type) {
            case Type.Reps:
                if (weight > 1) {
                    return context.getString(R.string.exerciseTitleRepsWithWeight,
                                             name, reps, weight);
                }
                return context.getString(R.string.exerciseTitleReps, name, reps);
            case Type.Duration:
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
            case State.Disabled:
                state = State.Active;
                if (type == Type.Duration)
                    WorkoutNotifService.scheduleAlarm(context, reps,
                                                      WorkoutNotifService.Type.Exercise);
                break;

            case State.Active:
                if (rest != 0) {
                    state = State.Resting;
                    break;
                }

            case State.Resting:
                if (++completedSets == sets) {
                    state = State.Completed;
                    completed = true;
                } else {
                    state = State.Active;
                }
            default:
        }
        return completed;
    }
}
