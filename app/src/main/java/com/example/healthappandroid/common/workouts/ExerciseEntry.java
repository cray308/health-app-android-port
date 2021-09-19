package com.example.healthappandroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthappandroid.common.helpers.ViewHelper;
import com.example.healthappandroid.hometab.addWorkout.utils.WorkoutNotifService;

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

    public String createSetsTitle() {
        if (sets == 1) return null;
        int completed = completedSets == sets ? sets : completedSets + 1;
        return ViewHelper.format("Set %d of %d", completed, sets);
    }

    public String createTitle() {
        if (state == StateResting)
            return ViewHelper.format("Rest: %d s", rest);
        switch (type) {
            case TypeReps:
                if (weight > 1)
                    return ViewHelper.format("%s x %d @ %d lbs", name, reps, weight);
                return ViewHelper.format("%s x %d", name, reps);
            case TypeDuration:
                if (reps > 120) {
                    double minutes = reps / 60.0;
                    return ViewHelper.format("%s for %.1f mins", name, minutes);
                }
                return ViewHelper.format("%s for %d sec", name, reps);
            default:
                int rowingDist = (5 * reps) / 4;
                return ViewHelper.format("Run/row %d/%d meters", reps, rowingDist);
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
