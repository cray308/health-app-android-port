package com.example.healthAppAndroid.common.workouts;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.StringRange;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public final class ExerciseEntry {
    public static abstract class Type {
        static final byte reps = 0;
        public static final byte duration = 1;
    }

    public static abstract class State {
        public final static byte disabled = 0;
        public final static byte active = 1;
        public static final byte resting = 2;
        private final static byte finished = 3;
    }

    static final class Params {
        int customSets = 0;
        int customReps = 0;
        int weight = -1;
        byte circuitType;
    }

    public final byte type;
    public byte state = 0;
    public int reps = 0;
    private final int sets;
    int completedSets = 0;
    private final StringRange headerRange = new StringRange();
    final StringRange titleRange = new StringRange();
    public final String restStr;
    @SuppressWarnings("StringBufferField")
    public final StringBuilder headerStr = new StringBuilder(16);
    @SuppressWarnings("StringBufferField")
    public final StringBuilder titleStr = new StringBuilder(16);

    ExerciseEntry(Context context, JSONObject dict, Params params) {
        int localSets = 1;
        byte localType = 0;
        String localRest = null;
        try {
            localType = (byte) dict.getInt(ExerciseManager.Keys.type);
            reps = dict.getInt(ExerciseManager.Keys.reps);
            int rest = dict.getInt("rest");
            String name = dict.getString("name");

            if (params.customReps != 0)
                reps = params.customReps;
            if (params.customSets != 0)
                localSets = params.customSets;
            if (rest != 0)
                localRest = context.getString(R.string.exerciseTitleRest, rest);

            if (localSets > 1) {
                String numberStr = context.getString(R.string.exerciseHeader, 1, localSets);
                headerRange.index = numberStr.indexOf('1');
                headerRange.end = headerRange.index + 1;
                headerStr.append(numberStr);
            }

            String title;
            switch (localType) {
                case Type.reps:
                    if (params.weight >= 0) {
                        title = context.getString(R.string.exerciseTitleRepsWithWeight,
                                                  name, reps, params.weight);
                    } else {
                        title = context.getString(R.string.exerciseTitleReps, name, reps);
                    }
                    break;

                case Type.duration:
                    if (reps > 120) {
                        title = context.getString(R.string.exerciseTitleDurationMinutes,
                                                  name, reps / 60f);
                    } else {
                        title = context.getString(R.string.exerciseTitleDurationSeconds,
                                                  name, reps);
                    }
                    break;

                default:
                    title = context.getString(R.string.exerciseTitleDistance,
                                              reps, ((5 * reps) >> 2));
            }

            titleStr.append(title);
            if (params.circuitType == Circuit.Type.decrement
                && localType == ExerciseEntry.Type.reps) {
                titleRange.index = titleStr.indexOf("10");
                titleRange.end = titleRange.index + 2;
            }

        } catch (JSONException ex) {
            Log.e("ExerciseEntry init", "Error while parsing JSON", ex);
        }
        sets = localSets;
        type = localType;
        restStr = localRest;
    }

    boolean cycle(Context context) {
        boolean completed = false;
        switch (state) {
            case State.disabled:
                state = State.active;
                if (type == Type.duration)
                    NotificationService.scheduleAlarm(context, reps,
                                                      NotificationService.Type.Exercise);
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
                    completed = true;
                } else {
                    state = State.active;
                    String newSets = String.format(Locale.US, "%d", completedSets + 1);
                    headerStr.replace(headerRange.index, headerRange.end, newSets);
                }
            default:
        }
        return completed;
    }
}
