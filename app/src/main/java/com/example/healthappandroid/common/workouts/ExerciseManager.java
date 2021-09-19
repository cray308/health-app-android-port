package com.example.healthappandroid.common.workouts;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class ExerciseManager {
    private static class DictWrapper {
        final JSONObject root;
        final JSONObject lib;

        static final String[] libraryKeys = {"st", "se", "en", "hi"};

        DictWrapper(JSONObject root, JSONObject lib) {
            this.root = root;
            this.lib = lib;
        }

        JSONArray getLibraryArrayForType(byte type) {
            JSONArray res = null;
            if (type > 3) return null;
            try {
                res = lib.getJSONArray(libraryKeys[type]);
            } catch (JSONException e) {
                Log.e("getLibraryArrayForType", "Error while parsing JSON", e);
            }
            return res;
        }

        JSONArray getCurrentWeekForPlan(byte plan, int week) {
            JSONArray res = null;
            try {
                JSONObject plans = root.getJSONObject("plans");
                JSONArray weeks = plans.optJSONArray(plan == 0 ? "bb" : "cc");
                if (weeks != null && week < weeks.length()) {
                    res = weeks.getJSONArray(week);
                }
            } catch (JSONException e) {
                Log.e("getCurrentWeekForPlan", "Error while parsing JSON", e);
            }
            return res;
        }
    }

    public static final String repsKey = "reps";
    public static final String typeKey = "type";
    private static final String indexKey = "index";
    public static final String titleKey = "title";

    private static DictWrapper createRootAndLibDict(Context context) {
        DictWrapper container = null;
        try {
            InputStreamReader input = new InputStreamReader(
                context.getAssets().open("workoutData.json"), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(input);
            StringBuilder contents = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                contents.append(line);
            JSONObject root = new JSONObject(contents.toString());
            JSONObject lib  = root.getJSONObject("library");
            container = new DictWrapper(root, lib);
        } catch (IOException e) {
            Log.e("createRootAndLibDict", "Error while opening JSON", e);
        } catch (JSONException e) {
            Log.e("createRootAndLibDict", "Error while parsing JSON", e);
        }
        return container;
    }

    public static void setWeeklyWorkoutNames(Context context, byte plan, int week, String[] names) {
        DictWrapper data = createRootAndLibDict(context);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan, week);
        if (currWeek == null) return;

        for (int i = 0; i < 7; ++i) {
            try {
                JSONObject day = currWeek.getJSONObject(i);

                byte type = (byte) day.getInt(typeKey);
                int index = day.getInt(indexKey);

                JSONArray libArr = data.getLibraryArrayForType(type);
                if (libArr == null) continue;

                JSONObject foundWorkout = libArr.getJSONObject(index);
                names[i] = foundWorkout.getString(titleKey);
            } catch (JSONException e) {
                Log.e("setWeeklyWorkoutNames", "Error while parsing JSON", e);
            }
        }
    }

    public static Workout getWeeklyWorkoutAtIndex(Context context, byte plan, int week, int index) {
        Workout w = null;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan, week);
        if (currWeek == null) return null;

        try {
            JSONObject day = currWeek.getJSONObject(index);

            byte type = (byte) day.getInt(typeKey);
            JSONArray libArr = data.getLibraryArrayForType(type);
            if (libArr == null) return null;

            int idx = day.getInt(indexKey);
            int sets = day.getInt("sets");
            int reps = day.getInt(repsKey);
            int weight = day.getInt("weight");

            JSONObject foundWorkout = libArr.getJSONObject(idx);
            w = new Workout(foundWorkout, (byte) index, type, idx, sets, reps, weight);
        } catch (JSONException e) {
            Log.e("getWeeklyWorkoutAtIndex", "Error while parsing JSON", e);
        }
        return w;
    }

    public static String[] getWorkoutNamesForType(Context context, byte type) {
        String[] results;
        int len;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray libArr = data.getLibraryArrayForType(type);
        if (libArr == null || ((len = libArr.length()) == 0)) return null;

        if (type == Workout.TypeStrength)
            len = 2;
        results = new String[len];

        for (int i = 0; i < len; ++i) {
            try {
                JSONObject week = libArr.getJSONObject(i);
                results[i] = week.getString(titleKey);
            } catch (JSONException e) {
                Log.e("getWorkoutNamesForType", "Error while parsing JSON", e);
            }
        }
        return results;
    }

    public static Workout getWorkoutFromLibrary(Context context, byte type, int index,
                                                int sets, int reps, int weight) {
        Workout w = null;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray libArr = data.getLibraryArrayForType(type);
        if (libArr == null) return null;

        try {
            JSONObject foundWorkout = libArr.getJSONObject(index);
            byte day = -1;
            w = new Workout(foundWorkout, day, type, index, sets, reps, weight);
        } catch (JSONException e) {
            Log.e("getWorkoutFromLibrary", "Error while parsing JSON", e);
        }
        return w;
    }
}
