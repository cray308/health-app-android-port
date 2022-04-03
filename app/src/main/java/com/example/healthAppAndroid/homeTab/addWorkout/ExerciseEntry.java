package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppCoordinator;

import org.json.JSONException;
import org.json.JSONObject;

final class ExerciseEntry {
    static abstract class Type {
        static final byte reps = 0;
        static final byte duration = 1;
    }

    static abstract class State {
        final static byte disabled = 0;
        final static byte active = 1;
        static final byte resting = 2;
        private final static byte finished = 3;
    }

    static final class Params {
        private final short customReps;
        short customSets;
        short weight = -1;
        private final byte workoutType;

        Params(Circuit.Params params) {
            customSets = params.customSets;
            customReps = params.customReps;
            workoutType = params.workoutType;
        }
    }

    static void setupHeaderData(Context c) {
        headerLoc = c.getString(R.string.exerciseHeader, 5).indexOf('1');
    }

    private static int headerLoc;
    final MutableString headerStr = new MutableString();
    final MutableString titleStr = new MutableString();
    final String restStr;
    final short reps;
    final short sets;
    short completedSets = 0;
    final byte type;
    byte state = 0;

    ExerciseEntry(Context c, JSONObject dict, Params params) {
        sets = params.customSets;
        short _reps = params.customReps;
        byte _type = 0;
        String _rest = null;
        try {
            _type = (byte)dict.getInt(ExerciseManager.Keys.type);
            if (_reps == 0)
                _reps = (short)dict.getInt(ExerciseManager.Keys.reps);
            if (AppCoordinator.shared.onEmulator && _type == Type.duration)
                _reps = (short)(params.workoutType == WorkoutType.HIC ? 15 : 120);

            int rest = dict.getInt("rest");
            if (rest != 0)
                _rest = c.getString(R.string.exerciseRest, rest);

            if (sets > 1) {
                headerStr.index = headerLoc;
                headerStr.end = headerStr.index + 1;
                headerStr.str.append(c.getString(R.string.exerciseHeader, sets));
            }

            String title, name = dict.getString("name");
            if (_type == Type.reps) {
                if (params.workoutType == 0) {
                    title = c.getString(R.string.exerciseRepsWeight, name, _reps, params.weight);
                } else {
                    title = c.getString(R.string.exerciseReps, name, _reps);
                }
            } else if (_type == Type.duration) {
                if (_reps > 120) {
                    title = c.getString(R.string.exerciseDurationMinutes, name, _reps / 60f);
                } else {
                    title = c.getString(R.string.exerciseDurationSeconds, name, _reps);
                }
            } else {
                title = c.getString(R.string.exerciseDistance, _reps, ((5 * _reps) >> 2));
            }
            titleStr.str.append(title);
        } catch (JSONException ex) {
            Log.e("ExerciseEntry init", "Error while parsing JSON", ex);
        }
        reps = _reps;
        type = _type;
        restStr = _rest;
    }

    boolean cycle(Context c, int group, int index) {
        switch (state) {
            case State.disabled:
                ++state;
                if (type == Type.duration) {
                    NotificationService.scheduleAlarm(
                      c, reps, NotificationService.Type.Exercise, group, index);
                }
                break;

            case State.active:
                //noinspection VariableNotUsedInsideIf
                if (restStr != null) {
                    state = State.resting;
                    break;
                }
            case State.resting:
                if (++completedSets == sets) {
                    state = State.finished;
                    return true;
                } else {
                    state = State.active;
                    headerStr.replace(String.valueOf(completedSets + 1));
                    if (type == Type.duration) {
                        NotificationService.scheduleAlarm(
                          c, reps, NotificationService.Type.Exercise, group, index);
                    }
                }
            default:
        }
        return false;
    }
}
