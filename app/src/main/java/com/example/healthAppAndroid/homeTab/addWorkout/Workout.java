package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

final class Workout {
    static abstract class Transition {
        static final byte completedWorkout = 0;
        static final byte finishedCircuitDeleteFirst = 1;
        static final byte finishedCircuit = 2;
        static final byte finishedExercise = 3;
        static final byte noChange = 4;
    }

    static abstract class EventOption {
        static final byte startGroup = 1;
        static final byte finishGroup = 2;
        static final byte finishExercise = 3;
    }

    final String title;
    Circuit group;
    Circuit[] activities;
    long startTime;
    int index;
    short duration;
    final byte type;
    final byte day;
    final boolean testMax;

    Workout(Context context, JSONObject dict, WorkoutParams params) {
        day = params.day;
        type = params.type;
        String workoutName = null;
        boolean[] isTestDay = {false};
        try {
            JSONArray foundActivities = dict.getJSONArray("activities");
            int nActivities = foundActivities.length();

            workoutName = dict.getString(ExerciseManager.Keys.title);
            activities = new Circuit[nActivities];

            for (int i = 0; i < nActivities; ++i) {
                JSONObject act = foundActivities.getJSONObject(i);
                activities[i] = new Circuit(context, act, params, isTestDay);
            }
        } catch (JSONException e) {
            Log.e("Workout init", "Error while parsing JSON", e);
        }

        title = workoutName;
        testMax = isTestDay[0];
        group = activities[0];
    }

    void startGroup(Context context, boolean startTimer) {
        group.index = 0;
        for (ExerciseEntry e : group.exercises) {
            e.state = ExerciseEntry.State.disabled;
            e.completedSets = 0;
        }

        if (group.type == Circuit.Type.AMRAP && startTimer)
            NotificationService.scheduleAlarm(context, 60L * group.reps, (byte) 1, index, 0);
        group.exercises[0].cycle(context, index, 0);
    }

    int findTransitionForEvent(Context context, boolean exerciseDone) {
        int t = Transition.noChange;
        if (exerciseDone) {
            t = Transition.finishedExercise;
            if (++group.index == group.exercises.length) {
                t = Transition.finishedCircuit;
                if (group.didFinish()) {
                    if (++index == activities.length) {
                        t = Transition.completedWorkout;
                    } else {
                        t = Transition.finishedCircuitDeleteFirst;
                        group = activities[index];
                        startGroup(context, true);
                    }
                } else {
                    startGroup(context, false);
                }
            } else {
                group.exercises[group.index].cycle(context, index, group.index);
            }
        }
        return t;
    }

    void setDuration() {
        duration = (short) (((int) ((Instant.now().getEpochSecond() - startTime) / 60f)) + 1);
        if (BuildConfig.DEBUG)
            duration *= 10;
    }

    boolean checkEnduranceDuration() {
        if (type != WorkoutType.endurance) return false;
        short planDuration = (short) (activities[0].exercises[0].reps / 60);
        return duration >= planDuration;
    }

    boolean longEnough() { return duration >= 15; }
}
