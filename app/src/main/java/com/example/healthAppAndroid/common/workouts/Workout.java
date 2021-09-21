package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.WorkoutNotifService;
import com.example.healthAppAndroid.homeTab.addWorkout.views.ExerciseView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Workout {
    public static abstract class Type {
        public static final byte Strength = 0, SE = 1, Endurance = 2, HIC = 3;
    }
    public static abstract class Transition {
        public static final byte CompletedWorkout = 0;
        public static final byte FinishedCircuitDeleteFirst = 1, FinishedCircuit = 2;
        public static final byte FinishedExercise = 3, NoChange = 4;
    }
    public static abstract class EventOption {
        public static final byte StartGroup = 1, FinishGroup = 2;
    }
    public static final long MinWorkoutDuration = 15;

    private static abstract class LiftWorkout {
        private static abstract class Index {
            static final int main = 0, test = 2;
        }
        private static abstract class Main {
            static final byte pullUp = 2;
        }
        private static abstract class Aux {
            static final byte deadlift = 2;
        }
        private static final byte benchIndex = 1;

        static void populateWeights(ExerciseEntry[] exercises, int index, float multiplier) {
            LiftData lifts = AppUserData.shared.liftData;
            exercises[LiftType.squat].weight = (int) (multiplier * (float) lifts.squat);
            if (index != Index.test) {
                exercises[benchIndex].weight = (int) (multiplier * (float) lifts.bench);
                if (index == Index.main) {
                    exercises[Main.pullUp].weight = (int) (multiplier * (float) lifts.pullUp);
                } else {
                    exercises[Aux.deadlift].weight = (int) (multiplier * (float) lifts.deadlift);
                }
            } else {
                exercises[LiftType.pullUp].weight = (int) (multiplier * (float) lifts.pullUp);
                exercises[LiftType.bench].weight = (int) (multiplier * (float) lifts.bench);
                exercises[LiftType.deadlift].weight = (int) (multiplier * (float) lifts.deadlift);
            }
        }
    }

    public static class Params {
        public final byte day;
        public byte type;
        public int index, sets = 1, reps = 1, weight = 1;

        public Params(byte day) { this.day = day; }
    }

    public static class LiftData {
        public short squat, pullUp, bench, deadlift;

        public LiftData() {}
        public LiftData(short squat, short pullUp, short bench, short deadlift) {
            this.squat = squat;
            this.pullUp = pullUp;
            this.bench = bench;
            this.deadlift = deadlift;
        }
    }

    public final byte type, day;
    public int index;
    public long startTime, duration;
    public LiftData newLifts;
    public String title;
    public ExerciseGroup group;
    public ExerciseEntry entry;
    public ExerciseGroup[] activities;

    public Workout(JSONObject dict, Params params) {
        day = params.day;
        type = params.type;
        boolean success = true;
        try {
            JSONArray foundActivities = dict.getJSONArray("activities");
            int nActivities = foundActivities.length();
            if (nActivities == 0) return;

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
            case Type.Strength:
                for (ExerciseEntry e : exercises) {
                    e.sets = params.sets;
                    e.reps = params.reps;
                }
                LiftWorkout.populateWeights(exercises, params.index, params.weight / 100f);
                break;
            case Type.SE:
                activities[0].reps = params.sets;
                for (ExerciseEntry e : exercises)
                    e.reps = params.reps;
                break;
            case Type.Endurance:
                int duration = params.reps * 60;
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
            e.state = ExerciseEntry.State.Disabled;
            e.completedSets = 0;
        }

        if (group.type == ExerciseGroup.Type.AMRAP && startTimer) {
            int duration = 60 * group.reps;
            WorkoutNotifService.scheduleAlarm(context, duration, WorkoutNotifService.Type.Circuit);
        }
    }

    public byte findTransitionForEvent(Context context, ExerciseView view, byte option) {
        byte t = Transition.NoChange;
        if (option != 0) {
            t = Transition.FinishedCircuit;
            if (option == EventOption.FinishGroup) {
                if (++index == activities.length)
                    return Transition.CompletedWorkout;
                group = activities[index];
                t = Transition.FinishedCircuitDeleteFirst;
            }
            startGroup(context, true);
            entry.cycle(context);
            return t;
        }

        if (entry.type == ExerciseEntry.Type.Duration &&
            entry.state == ExerciseEntry.State.Active && !view.userInteractionEnabled) {
            view.userInteractionEnabled = true;
            view.button.setEnabled(true);
            if (type == Type.Endurance)
                return Transition.NoChange;
        }

        boolean exerciseDone = entry.cycle(context);
        view.configure(entry);

        if (exerciseDone) {
            t = Transition.FinishedExercise;
            if (++group.index == group.exercises.length) {
                t = Transition.FinishedCircuit;
                if (group.didFinish()) {
                    if (++index == activities.length) {
                        t = Transition.CompletedWorkout;
                    } else {
                        t = Transition.FinishedCircuitDeleteFirst;
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
        long stopTime = DateHelper.getCurrentTime() + 1;
        duration = (long) ((stopTime - startTime) / 60.0);
        if (BuildConfig.DEBUG)
            duration *= 10;
    }
}
