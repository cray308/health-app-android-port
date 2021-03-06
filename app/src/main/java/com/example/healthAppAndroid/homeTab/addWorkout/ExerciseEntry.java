package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.icu.text.MeasureFormat;
import android.icu.util.MeasureUnit;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.MainActivity;

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
        private final String one;
        private final int oneCount;
        private final short customReps;
        short customSets;
        float weight = -1;
        private final byte type;

        Params(Circuit.Params params) {
            one = params.one;
            oneCount = params.oneCount;
            customSets = params.customSets;
            customReps = params.customReps;
            type = params.type;
        }
    }

    static void setupData(Context c, boolean metric) {
        setsSub = c.getString(R.string.sets1);
        weightUnit = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.NARROW)
                                  .getUnitDisplayName(metric ? MeasureUnit.KILOGRAM : MeasureUnit.POUND);
    }

    private static String setsSub;
    private static String weightUnit;
    final MutableString headerStr = new MutableString();
    final MutableString titleStr = new MutableString();
    final String restStr;
    final short reps;
    final short sets;
    short completedSets = 0;
    final byte type;
    byte state = 0;

    ExerciseEntry(Context c, JSONObject dict, String[] exNames, Params params) {
        sets = params.customSets;
        short _reps = params.customReps;
        byte _type = 0;
        String _rest = null;
        try {
            _type = (byte)dict.getInt(ExerciseManager.Keys.type);
            if (_reps == 0) _reps = (short)dict.getInt(ExerciseManager.Keys.reps);
            if (MainActivity.onEmulator() && _type == 1) _reps = (short)(params.type == 3 ? 15 : 120);

            int rest = dict.getInt("B");
            if (rest != 0) _rest = c.getString(R.string.exerciseRest, rest);

            if (sets > 1) {
                String h = c.getString(R.string.exerciseHeader, 1, sets);
                headerStr.str.append(h);
                int subIdx = h.indexOf(setsSub);
                int numIdx = h.substring(subIdx, subIdx + setsSub.length()).indexOf(params.one);
                headerStr.index = subIdx + numIdx;
                headerStr.length = params.oneCount;
            }

            String title, name = exNames[dict.getInt(ExerciseManager.Keys.index)];
            if (_type == Type.reps) {
                if (params.type == 0) {
                    title = c.getString(R.string.exWeight, name, _reps, params.weight, weightUnit);
                } else {
                    title = c.getString(R.string.exReps, name, _reps);
                }
            } else if (_type == Type.duration) {
                if (_reps > 120) {
                    title = c.getString(R.string.exMinutes, name, _reps / 60f);
                } else {
                    title = c.getString(R.string.exSeconds, name, _reps);
                }
            } else {
                title = c.getString(R.string.exDistance, name, _reps, ((5 * _reps) >> 2));
            }
            titleStr.str.append(title);
        } catch (JSONException ignored) {}
        reps = _reps;
        type = _type;
        restStr = _rest;
    }

    boolean cycle(Context c, int group, int index) {
        switch (state) {
            case State.disabled:
                ++state;
                if (type == Type.duration) NotificationService.scheduleAlarm(c, reps, 0, group, index);
                break;

            case State.active:
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
                    String newNum = String.format(Locale.getDefault(), "%d", completedSets + 1);
                    headerStr.replace(newNum);
                    headerStr.length = newNum.length();
                    if (type == Type.duration)
                        NotificationService.scheduleAlarm(c, reps, 0, group, index);
                }
            default:
        }
        return false;
    }
}
