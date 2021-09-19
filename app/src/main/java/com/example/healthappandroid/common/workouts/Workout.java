package com.example.healthappandroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.hometab.addWorkout.utils.WorkoutNotifService;
import com.example.healthappandroid.hometab.addWorkout.views.ExerciseView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

public class Workout {
    public static final byte TypeStrength = 0;
    public static final byte TypeSE = 1;
    public static final byte TypeEndurance = 2;
    public static final byte TypeHIC = 3;

    public static final byte TransitionCompletedWorkout = 0;
    public static final byte TransitionFinishedCircuitDeleteFirst = 1;
    public static final byte TransitionFinishedCircuit = 2;
    public static final byte TransitionFinishedExercise = 3;
    public static final byte TransitionNoChange = 4;

    public static final byte EventOptionStartGroup = 1;
    public static final byte EventOptionFinishGroup = 2;

    public static final long MinWorkoutDuration = 15;

    public byte type;
    public final byte day;
    public int index;
    public long startTime;
    public long duration;
    public short[] newLifts;
    public String title;
    public ExerciseGroup group;
    public ExerciseEntry entry;
    public ExerciseGroup[] activities;

    public Workout(JSONObject dict, byte day, byte type, int index,
                   int sets, int reps, int weight) {
        this.day = day;
        boolean success = true;
        try {
            JSONArray foundActivities = dict.getJSONArray("activities");
            int nActivities = foundActivities.length();
            if (nActivities <= 0) return;

            this.type = type;
            title = dict.getString(ExerciseManager.titleKey);
            activities = new ExerciseGroup[nActivities];

            for (int i = 0; i < nActivities; ++i) {
                JSONObject act = foundActivities.getJSONObject(i);
                activities[i] = new ExerciseGroup(act);
            }
        } catch (JSONException e) {
            success = false;
            Log.e("Workout init", "Error while parsing JSON", e);
        }

        if (!success) return;
        ExerciseEntry[] exercises = activities[0].exercises;
        switch (type) {
            case TypeStrength:
                short[] lifts = AppUserData.shared.liftMaxes;
                int nExercises = exercises.length;
                double weightMultiplier = (double) weight / 100.0;
                for (ExerciseEntry e : exercises) {
                    e.sets = sets;
                    e.reps = reps;
                }
                exercises[0].weight = (int) (weightMultiplier * (double) lifts[0]);

                if (nExercises >= 3 && index <= 1) {
                    exercises[1].weight = (int) (weightMultiplier * (double) lifts[2]);
                    if (index == 0) {
                        exercises[2].weight = (int) (weightMultiplier * (double) lifts[1]);
                    } else {
                        exercises[2].weight = (int) (weightMultiplier * (double) lifts[3]);
                    }
                } else if (nExercises >= 4 && index == 2) {
                    for (int i = 1; i < 4; ++i)
                        exercises[i].weight = lifts[i];
                }
                break;
            case TypeSE:
                activities[0].reps = sets;
                for (ExerciseEntry e : exercises)
                    e.reps = reps;
                break;
            case TypeEndurance:
                int duration = reps * 60;
                for (ExerciseEntry e : exercises)
                    e.reps = duration;
            default:
                break;
        }
        group = activities[0];
        entry = group.exercises[0];
    }

    private void startGroup(Context context, boolean startTimer) {
        group.index = 0;
        entry = group.exercises[0];
        for (ExerciseEntry e : group.exercises) {
            e.state = ExerciseEntry.StateDisabled;
            e.completedSets = 0;
        }

        if (group.type == ExerciseGroup.TypeAMRAP && startTimer) {
            int duration = 60 * group.reps;
            WorkoutNotifService.scheduleAlarm(context, duration,
                                              WorkoutNotifService.NotificationFinishCircuit);
        }
    }

    public byte findTransitionForEvent(Context context, ExerciseView view, byte option) {
        byte t = TransitionNoChange;
        if (option != 0) {
            t = TransitionFinishedCircuit;
            if (option == EventOptionFinishGroup) {
                if (++index == activities.length)
                    return TransitionCompletedWorkout;
                group = activities[index];
                t = TransitionFinishedCircuitDeleteFirst;
            }
            startGroup(context, true);
            entry.cycle(context);
            return t;
        }

        if (entry.type == ExerciseEntry.TypeDuration &&
            entry.state == ExerciseEntry.StateActive && !view.userInteractionEnabled) {
            view.userInteractionEnabled = true;
            view.button.setEnabled(true);
            if (type == TypeEndurance)
                return TransitionNoChange;
        }

        boolean exerciseDone = entry.cycle(context);
        view.configure(entry);

        if (exerciseDone) {
            t = TransitionFinishedExercise;
            if (++group.index == group.exercises.length) {
                t = TransitionFinishedCircuit;
                if (group.didFinish()) {
                    if (++index == activities.length) {
                        t = TransitionCompletedWorkout;
                    } else {
                        t = TransitionFinishedCircuitDeleteFirst;
                        group = activities[index];
                        startGroup(context, true);
                        entry.cycle(context);
                    }
                } else {
                    startGroup(context, false);
                    entry.cycle(context);
                }
            } else {
                entry = group.exercises[group.index];
                entry.cycle(context);
            }
        }
        return t;
    }

    public void setDuration() {
        long stopTime = Instant.now().getEpochSecond() + 1;
        duration = (long) ((stopTime - startTime) / 60.0);
    }
}
