package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;

import java.util.Locale;

final class Circuit {
    static abstract class Type {
        static final byte rounds = 0;
        static final byte AMRAP = 1;
        static final byte decrement = 2;
    }

    final Exercise[] exercises;
    final MutableString header = new MutableString();
    int index;
    final int reps;
    int completedReps;
    final byte type;

    Circuit(Exercise[] exercises, byte type, int reps) {
        this.exercises = exercises;
        this.type = type;
        this.reps = reps;
        if (type != Type.decrement) return;

        completedReps = exercises[0].reps;
        String ten = String.format(Locale.getDefault(), "%d", 10);
        for (Exercise e : exercises) {
            if (e.type == Exercise.Type.reps) e.title.setup(ten, null);
        }
    }

    void start(Context context, int section, boolean startTimer) {
        index = 0;
        for (Exercise e : exercises) {
            e.state = Exercise.State.disabled;
            e.completedSets = 0;
        }

        if (type == Type.AMRAP && startTimer) {
            NotificationService.scheduleAlarm(context, 60 * reps,
                                              NotificationService.Type.circuit, section, 0);
        }
        exercises[0].cycle(context, section, 0);
    }

    int increment(Context context, int section) {
        if (++index != exercises.length) {
            exercises[index].cycle(context, section, index);
            return Workout.Transition.finishedExercise;
        }

        boolean endCircuit = false;
        switch (type) {
            case Type.rounds:
                endCircuit = ++completedReps == reps;
                break;

            case Type.decrement:
                if (endCircuit = (--completedReps == 0)) break;

                String newReps = String.format(Locale.getDefault(), "%d", completedReps);
                for (Exercise e : exercises) {
                    if (e.type == Exercise.Type.reps) e.title.replace(newReps);
                }
            default:
        }

        if (!endCircuit) start(context, section, false);
        return Workout.Transition.finishedCircuit + (endCircuit ? 1 : 0);
    }
}
