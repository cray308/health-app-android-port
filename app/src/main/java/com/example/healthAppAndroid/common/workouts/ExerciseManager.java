package com.example.healthAppAndroid.common.workouts;

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
    private static final class DictWrapper {
        private final JSONObject root;
        private final JSONObject lib;
        private static final String[] libraryKeys = {"st", "se", "en", "hi"};
        private static final String[] planKeys = {"bb", "cc"};

        private DictWrapper(JSONObject root, JSONObject lib) {
            this.root = root;
            this.lib = lib;
        }

        private JSONArray getLibraryArrayForType(byte type) {
            if (type > 3) return null;
            JSONArray res = null;
            try {
                res = lib.getJSONArray(libraryKeys[type]);
            } catch (JSONException e) {
                Log.e("getLibraryArrayForType", "Error while parsing JSON", e);
            }
            return res;
        }

        private JSONArray getCurrentWeekForPlan(byte plan, int week) {
            JSONArray res = null;
            try {
                JSONObject plans = root.getJSONObject("plans");
                JSONArray weeks = plans.getJSONArray(planKeys[plan]);
                if (week < weeks.length())
                    res = weeks.getJSONArray(week);
            } catch (JSONException e) {
                Log.e("getCurrentWeekForPlan", "Error while parsing JSON", e);
            }
            return res;
        }
    }
    public static abstract class Keys {
        static final String reps = "reps";
        public static final String type = "type";
        private static final String index = "index";
        public static final String title = "title";
    }

    private static DictWrapper createRootAndLibDict(Context context) {
        DictWrapper container = null;
        try {
            InputStreamReader input = new InputStreamReader(
                context.getAssets().open("workoutData.json"), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(input);
            StringBuilder contents = new StringBuilder(20000);
            for (String line = reader.readLine(); line != null; line = reader.readLine())
                contents.append(line);
            JSONObject root = new JSONObject(contents.toString());
            JSONObject lib  = root.getJSONObject("library");
            container = new DictWrapper(root, lib);
            reader.close();
        } catch (IOException e) {
            Log.e("createRootAndLibDict", "Error while opening JSON", e);
        } catch (JSONException e) {
            Log.e("createRootAndLibDict", "Error while parsing JSON", e);
        }
        return container;
    }

    public static String[] getWeeklyWorkoutNames(Context context, byte plan, int week) {
        String[] names = {null, null, null, null, null, null, null};
        DictWrapper data = createRootAndLibDict(context);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan, week);
        if (currWeek == null) return names;

        for (int i = 0; i < 7; ++i) {
            try {
                JSONObject day = currWeek.getJSONObject(i);

                byte type = (byte) day.getInt(Keys.type);
                int index = day.getInt(Keys.index);

                JSONArray libArr = data.getLibraryArrayForType(type);
                if (libArr == null) continue;

                JSONObject foundWorkout = libArr.getJSONObject(index);
                names[i] = foundWorkout.getString(Keys.title);
            } catch (JSONException e) {
                Log.e("setWeeklyWorkoutNames", "Error while parsing JSON", e);
            }
        }
        return names;
    }

    public static Workout.Params getWeeklyWorkoutParams(Context context,
                                                        byte plan, int week, int index) {
        Workout.Params params = null;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan, week);
        if (currWeek == null) return null;

        try {
            JSONObject day = currWeek.getJSONObject(index);
            byte type = (byte) day.getInt(Keys.type);
            JSONArray libArr = data.getLibraryArrayForType(type);
            if (libArr == null) return null;

            params = new Workout.Params((byte) index);
            params.type = type;
            params.index = day.getInt(Keys.index);
            params.sets = day.getInt("sets");
            params.reps = day.getInt(Keys.reps);
            params.weight = day.getInt("weight");
        } catch (JSONException e) {
            Log.e("getWeeklyWorkoutParams", "Error while parsing JSON", e);
        }
        return params;
    }

    public static String[] getWorkoutNamesForType(Context context, byte type) {
        String[] results;
        int len;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray libArr = data.getLibraryArrayForType(type);
        if (libArr == null || ((len = libArr.length()) == 0)) return null;

        if (type == Workout.Type.strength)
            len = 2;
        results = new String[len];

        for (int i = 0; i < len; ++i) {
            try {
                JSONObject week = libArr.getJSONObject(i);
                results[i] = week.getString(Keys.title);
            } catch (JSONException e) {
                Log.e("getWorkoutNamesForType", "Error while parsing JSON", e);
            }
        }
        return results;
    }

    public static Workout getWorkoutFromLibrary(Context context, Workout.Params params) {
        Workout w = null;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray libArr = data.getLibraryArrayForType(params.type);
        if (libArr == null) return null;

        try {
            JSONObject foundWorkout = libArr.getJSONObject(params.index);
            w = new Workout(context, foundWorkout, params);
        } catch (JSONException e) {
            Log.e("getWorkoutFromLibrary", "Error while parsing JSON", e);
        }
        return w;
    }
}
