package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppCoordinator;
import com.example.healthAppAndroid.core.AppUserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class Circuit {
    static abstract class Type {
        static final byte rounds = 0;
        static final byte AMRAP = 1;
        private static final byte decrement = 2;
    }

    static final class Params {
        final short customSets;
        final short customReps;
        private final short customCircuitReps;
        private final short[] weights = {-1, -1, -1, -1};
        int location;
        private final int nActivities;
        final byte workoutType;

        Params(WorkoutParams params, boolean[] isTestDay, int length) {
            short exSets = 1, exReps = 0, circuitReps = 0;
            if (params.type == WorkoutType.strength) {
                short[] lifts = AppUserData.shared.liftArray;
                float multiplier = params.weight / 100f;
                weights[0] = (short)(multiplier * lifts[0]);
                if (params.index <= 1) {
                    weights[1] = (short)(multiplier * lifts[LiftType.bench]);
                    if (params.index == 0) {
                        int weight = ExerciseManager.getBodyWeightToUse();
                        weights[2] =
                          (short)((int)((lifts[LiftType.pullUp] + weight) * multiplier) - weight);
                        if (weights[2] < 0)
                            weights[2] = 0;
                    } else {
                        weights[2] = (short)(multiplier * lifts[LiftType.deadlift]);
                    }
                } else {
                    System.arraycopy(lifts, 1, weights, 1, 3);
                    isTestDay[0] = true;
                }
                exReps = params.reps;
                exSets = params.sets;
            } else if (params.type == WorkoutType.SE) {
                circuitReps = params.sets;
                exReps = params.reps;
            } else if (params.type == WorkoutType.endurance) {
                exReps = (short)(params.reps * 60);
            }
            customSets = exSets;
            customReps = exReps;
            customCircuitReps = circuitReps;
            nActivities = length;
            workoutType = params.type;
        }
    }

    static void setupHeaderData(Context c) {
        roundsLoc = c.getString(R.string.circuitHeaderRounds, 5).indexOf('1');
    }

    private static int roundsLoc;
    ExerciseEntry[] exercises;
    final MutableString headerStr = new MutableString();
    int index = 0;
    final short reps;
    short completedReps = 0;
    final byte type;

    Circuit(Context c, JSONObject dict, Params params) {
        short _reps = params.customCircuitReps;
        byte _type = 0;

        try {
            _type = (byte)dict.getInt(ExerciseManager.Keys.type);
            ExerciseEntry.Params exerciseParams = new ExerciseEntry.Params(params);
            String separator = "";

            if (_reps == 0)
                _reps = (short)dict.getInt(ExerciseManager.Keys.reps);
            if (AppCoordinator.shared.onEmulator && _type == Type.AMRAP)
                _reps = (short)(params.nActivities > 1 ? 1 : 2);

            JSONArray foundExercises = dict.getJSONArray("exercises");
            int nExercises = foundExercises.length();
            if (params.workoutType == WorkoutType.HIC && _type == 0 && nExercises == 1) {
                exerciseParams.customSets = _reps;
                _reps = 1;
            }
            if (params.nActivities > 1) {
                headerStr.str.append(
                  c.getString(R.string.circuitProgress, params.location, params.nActivities));
                separator = " - ";
            }
            if (_type == Type.AMRAP) {
                headerStr.str.append(separator);
                headerStr.str.append(c.getString(R.string.circuitHeaderAMRAP, _reps));
            } else if (_reps > 1) {
                headerStr.str.append(separator);
                headerStr.index = roundsLoc + headerStr.str.length();
                headerStr.end = headerStr.index + 1;
                headerStr.str.append(c.getString(R.string.circuitHeaderRounds, _reps));
            }

            exercises = new ExerciseEntry[nExercises];
            for (int i = 0; i < nExercises; ++i) {
                JSONObject ex = foundExercises.getJSONObject(i);
                if (params.workoutType == WorkoutType.strength)
                    exerciseParams.weight = params.weights[i];
                ExerciseEntry e = new ExerciseEntry(c, ex, exerciseParams);
                exercises[i] = e;
                if (_type == Type.decrement && e.type == ExerciseEntry.Type.reps) {
                    e.titleStr.index = e.titleStr.str.indexOf("10");
                    e.titleStr.end = e.titleStr.index + 2;
                }
            }

            if (_type == Type.decrement)
                completedReps = exercises[0].reps;
        } catch (JSONException e) {
            Log.e("Circuit init", "Error while parsing JSON", e);
        }
        type = _type;
        reps = _reps;
    }

    boolean didFinish() {
        if (type == Type.rounds) {
            return ++completedReps == reps;
        } else if (type == Type.decrement) {
            boolean changeRange = completedReps-- == 10;
            if (completedReps == 0) return true;

            String newReps = String.valueOf(completedReps);
            for (ExerciseEntry e : exercises) {
                if (e.type == ExerciseEntry.Type.reps) {
                    e.titleStr.replace(newReps);
                    if (changeRange)
                        e.titleStr.end -= 1;
                }
            }
        }
        return false;
    }
}
