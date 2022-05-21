package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;

import com.example.healthAppAndroid.R;

import java.util.Locale;

final class Exercise {
    static abstract class Type {
        static final byte reps = 0;
        static final byte duration = 1;
    }

    static abstract class State {
        static final byte disabled = 0;
        static final byte active = 1;
        static final byte activeCont = 2;
        static final byte resting = 3;
        static final byte finished = 4;
    }

    final MutableString header = new MutableString();
    final MutableString title = new MutableString();
    final String rest;
    final int reps;
    final int sets;
    int completedSets;
    final byte type;
    byte state;

    Exercise(Context context, byte type, int reps, int sets, int rest) {
        this.type = type;
        this.reps = reps;
        this.sets = sets;
        this.rest = rest != 0 ? context.getString(R.string.exerciseRest, rest) : null;

        if (sets > 1) {
            header.str.append(context.getString(R.string.exerciseHeader, 1, sets));
            header.setup(MutableString.one, context.getString(R.string.sets1));
        }
    }

    boolean cycle(Context context, int section, int index) {
        switch (state) {
            case State.disabled:
                ++state;
                if (type == Type.duration) {
                    NotificationService.scheduleAlarm(
                      context, reps, NotificationService.Type.exercise, section, index);
                }
                break;

            case State.active:
            case State.activeCont:
                if (rest != null) {
                    state = State.resting;
                    break;
                }
            case State.resting:
                if (++completedSets == sets) {
                    state = State.finished;
                    return true;
                } else {
                    state = State.activeCont;
                    header.replace(String.format(Locale.getDefault(), "%d", completedSets + 1));
                    if (type == Type.duration) {
                        NotificationService.scheduleAlarm(
                          context, reps, NotificationService.Type.exercise, section, index);
                    }
                }
            default:
        }
        return false;
    }
}
