package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;
import com.example.healthAppAndroid.homeTab.addWorkout.views.ExerciseView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

public final class Workout {
    public static abstract class Type {
        public static final byte strength = 0;
        public static final byte SE = 1;
        public static final byte endurance = 2;
        public static final byte HIC = 3;
    }

    public static abstract class Transition {
        public static final byte completedWorkout = 0;
        public static final byte finishedCircuitDeleteFirst = 1;
        public static final byte finishedCircuit = 2;
        public static final byte finishedExercise = 3;
        private static final byte noChange = 4;
    }

    public static abstract class EventOption {
        public static final byte startGroup = 1;
        public static final byte finishGroup = 2;
    }

    public static final long MinWorkoutDuration = 15;
    public static final String finishedNotification = "FinishedWorkoutNotification";
    public static final String userInfoKey = "totalWorkouts";

    public final static class Params implements Parcelable {
        private final byte day;
        public byte type;
        public int index;
        public int sets = 1;
        public int reps = 1;
        public int weight = 1;

        public Params(byte day) { this.day = day; }

        public int describeContents() { return 0; }

        public void writeToParcel(Parcel out, int flags) {
            out.writeByte(day);
            out.writeByte(type);
            out.writeInt(index);
            out.writeInt(sets);
            out.writeInt(reps);
            out.writeInt(weight);
        }

        public static final Parcelable.Creator<Params> CREATOR = new Parcelable.Creator<Params>() {
            public Params createFromParcel(Parcel in) { return new Params(in); }

            public Params[] newArray(int size) { return new Params[size]; }
        };

        private Params(Parcel in) {
            day = in.readByte();
            type = in.readByte();
            index = in.readInt();
            sets = in.readInt();
            reps = in.readInt();
            weight = in.readInt();
        }
    }

    public final byte type;
    public final byte day;
    public int index;
    public long startTime;
    public long duration;
    public final String title;
    public Circuit group;
    private ExerciseEntry entry;
    public Circuit[] activities;

    Workout(Context context, JSONObject dict, Params params) {
        day = params.day;
        type = params.type;
        String workoutName = null;
        try {
            JSONArray foundActivities = dict.getJSONArray("activities");
            int nActivities = foundActivities.length();

            workoutName = dict.getString(ExerciseManager.Keys.title);
            activities = new Circuit[nActivities];

            for (int i = 0; i < nActivities; ++i) {
                JSONObject act = foundActivities.getJSONObject(i);
                activities[i] = new Circuit(context, act, params);
            }
        } catch (JSONException e) {
            Log.e("Workout init", "Error while parsing JSON", e);
        }

        title = workoutName;
        group = activities[0];
        entry = group.exercises[0];
    }

    private void startGroup(Context context, boolean startTimer) {
        group.index = 0;
        entry = group.exercises[0];
        for (ExerciseEntry e : group.exercises) {
            e.state = ExerciseEntry.State.disabled;
            e.completedSets = 0;
        }

        if (group.type == Circuit.Type.AMRAP && startTimer) {
            int minutes = 60 * group.reps;
            NotificationService.scheduleAlarm(context, minutes, NotificationService.Type.Circuit);
        }
    }

    public byte findTransitionForEvent(Context context, ExerciseView view, byte option) {
        byte t = Transition.noChange;
        if (option != 0) {
            t = Transition.finishedCircuit;
            if (option == EventOption.finishGroup) {
                if (++index == activities.length) return Transition.completedWorkout;
                group = activities[index];
                t = Transition.finishedCircuitDeleteFirst;
            }
            startGroup(context, true);
            entry.cycle(context);
            return t;
        }

        if (entry.type == ExerciseEntry.Type.duration &&
            entry.state == ExerciseEntry.State.active && !view.userInteractionEnabled) {
            view.userInteractionEnabled = true;
            view.button.setEnabled(true);
            if (type == Type.endurance) return Transition.noChange;
        }

        boolean exerciseDone = entry.cycle(context);
        view.configure();

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
        duration = ((long) ((Instant.now().getEpochSecond() - startTime) / 60f)) + 1;
        if (BuildConfig.DEBUG)
            duration *= 10;
    }

    public boolean checkEnduranceDuration() {
        if (type != Type.endurance) return false;
        int planDuration = activities[0].exercises[0].reps / 60;
        return duration >= planDuration;
    }
}
