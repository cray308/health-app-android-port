package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.ControlState;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

public final class ExerciseEntry {
    public static abstract class Type {
        public static final byte reps = 0;
        public static final byte duration = 1;
    }

    public byte type;
    public byte state;
    public int weight;
    public int reps;
    public int sets = 1;
    private int rest;
    public int completedSets;
    private String name;

    public ExerciseEntry(JSONObject e) {
        try {
            type = (byte) e.getInt(ExerciseManager.Keys.type);
            reps = e.getInt(ExerciseManager.Keys.reps);
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
        if (state == ControlState.resting)
            return context.getString(R.string.exerciseTitleRest, rest);
        switch (type) {
            case Type.reps:
                if (weight > 1) {
                    return context.getString(R.string.exerciseTitleRepsWithWeight,
                                             name, reps, weight);
                }
                return context.getString(R.string.exerciseTitleReps, name, reps);
            case Type.duration:
                if (reps > 120) {
                    double minutes = reps / 60.0;
                    return context.getString(R.string.exerciseTitleDurationMinutes, name, minutes);
                }
                return context.getString(R.string.exerciseTitleDurationSeconds, name, reps);
            default:
                int rowingDist = (5 * reps) >> 2;
                return context.getString(R.string.exerciseTitleDistance, reps, rowingDist);
        }
    }

    public boolean cycle(Context context) {
        boolean completed = false;
        switch (state) {
            case ControlState.disabled:
                state = ControlState.active;
                if (type == Type.duration)
                    NotificationService.scheduleAlarm(context, reps,
                                                      NotificationService.Type.Exercise);
                break;

            case ControlState.active:
                if (rest != 0) {
                    state = ControlState.resting;
                    break;
                }
            case ControlState.resting:
                if (++completedSets == sets) {
                    state = ControlState.finished;
                    completed = true;
                } else {
                    state = ControlState.active;
                }
            default:
        }
        return completed;
    }
}
