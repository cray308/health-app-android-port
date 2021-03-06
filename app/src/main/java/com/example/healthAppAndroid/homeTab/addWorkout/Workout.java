package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.Instant;

final class Workout {
    static abstract class Transition {
        static final byte completedWorkout = 0;
        static final byte finishedCircuitDeleteFirst = 1;
        static final byte finishedCircuit = 2;
        static final byte finishedExercise = 3;
        static final byte noChange = 4;
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

    Workout(Context c, JSONArray acts, WorkoutParams params) {
        day = params.day;
        type = params.type;
        String[] exNames = c.getResources().getStringArray(R.array.exNames);
        boolean[] isTestDay = {false};
        try {
            int nActivities = acts.length();
            Circuit.Params cParams = new Circuit.Params(params, isTestDay, nActivities);

            activities = new Circuit[nActivities];
            for (int i = 0; i < nActivities; ++i) {
                cParams.index = i + 1;
                activities[i] = new Circuit(c, acts.getJSONObject(i), exNames, cParams);
            }
        } catch (JSONException ignored) {}

        title = c.getResources().getStringArray(ExerciseManager.titleKeys[type])[params.index];
        testMax = isTestDay[0];
        group = activities[0];
    }

    void startGroup(Context c, boolean startTimer) {
        group.index = 0;
        for (ExerciseEntry e : group.exercises) {
            e.state = ExerciseEntry.State.disabled;
            e.completedSets = 0;
        }

        if (group.type == Circuit.Type.AMRAP && startTimer)
            NotificationService.scheduleAlarm(c, 60L * group.reps, 1, index, 0);
        group.exercises[0].cycle(c, index, 0);
    }

    int findTransition(Context c, boolean exerciseDone) {
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
                        startGroup(c, true);
                    }
                } else {
                    startGroup(c, false);
                }
            } else {
                group.exercises[group.index].cycle(c, index, group.index);
            }
        }
        return t;
    }

    void setDuration() {
        duration = (short)(((int)((Instant.now().getEpochSecond() - startTime) / 60f)) + 1);
        if (MainActivity.onEmulator()) duration *= 10;
    }

    boolean isCompleted() {
        int groupIndex = group.index;
        if (index != activities.length - 1 || groupIndex != group.exercises.length - 1) return false;
        if (type == WorkoutType.endurance) return duration >= activities[0].exercises[0].reps / 60;

        if (group.type == Circuit.Type.rounds && group.completedReps == group.reps - 1) {
            ExerciseEntry e = group.exercises[groupIndex];
            return e.state == ExerciseEntry.State.resting && e.completedSets == e.sets - 1;
        }
        return false;
    }
}
