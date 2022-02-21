package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

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
        short customReps = 0;
        short customSets = 0;
        short weight = -1;
        byte circuitType;
    }

    final MutableString headerStr = new MutableString();
    final MutableString titleStr = new MutableString();
    final String restStr;
    final short reps;
    final short sets;
    short completedSets = 0;
    final byte type;
    byte state = 0;

    ExerciseEntry(Context context, JSONObject dict, Params params) {
        short _reps = 0;
        short _sets = 1;
        int _type = 0;
        String _rest = null;
        try {
            _type = dict.getInt(ExerciseManager.Keys.type);
            _reps = (short) dict.getInt(ExerciseManager.Keys.reps);
            int rest = dict.getInt("rest");
            String name = dict.getString("name");

            if (params.customReps != 0)
                _reps = params.customReps;
            if (params.customSets != 0)
                _sets = params.customSets;
            if (rest != 0)
                _rest = context.getString(R.string.exerciseTitleRest, rest);

            if (_sets > 1) {
                String numberStr = context.getString(R.string.exerciseHeader, 1, _sets);
                headerStr.index = (short) numberStr.indexOf('1');
                headerStr.end = (short) (headerStr.index + 1);
                headerStr.str.append(numberStr);
            }

            String title;
            switch (_type) {
                case Type.reps:
                    if (params.weight >= 0) {
                        title = context.getString(R.string.exerciseTitleRepsWithWeight,
                                                  name, _reps, params.weight);
                    } else {
                        title = context.getString(R.string.exerciseTitleReps, name, _reps);
                    }
                    break;

                case Type.duration:
                    if (_reps > 120) {
                        title = context.getString(R.string.exerciseTitleDurationMinutes,
                                                  name, _reps / 60f);
                    } else {
                        title = context.getString(R.string.exerciseTitleDurationSeconds,
                                                  name, _reps);
                    }
                    break;

                default:
                    title = context.getString(R.string.exerciseTitleDistance,
                                              _reps, ((5 * _reps) >> 2));
            }

            titleStr.str.append(title);
            if (params.circuitType == Circuit.Type.decrement && _type == Type.reps) {
                titleStr.index = (short) titleStr.str.indexOf("10");
                titleStr.end = (short) (titleStr.index + 2);
            }

        } catch (JSONException ex) {
            Log.e("ExerciseEntry init", "Error while parsing JSON", ex);
        }
        reps = _reps;
        sets = _sets;
        type = (byte) _type;
        restStr = _rest;
    }

    boolean cycle(Context context, int group, int index) {
        switch (state) {
            case State.disabled:
                ++state;
                if (type == Type.duration) {
                    NotificationService.scheduleAlarm(
                      context, reps, NotificationService.Type.Exercise, group, index);
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
                    headerStr.replace(String.format(Locale.US, "%d", completedSets + 1));
                }
            default:
        }
        return false;
    }
}
