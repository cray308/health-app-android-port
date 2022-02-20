package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.Log;

import com.example.healthAppAndroid.core.AppUserData;

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

        private JSONArray getLibraryArrayForType(int type) {
            JSONArray res;
            try {
                res = lib.getJSONArray(libraryKeys[type]);
            } catch (JSONException e) {
                Log.e("getLibraryArrayForType", "Error while parsing JSON", e);
                res = new JSONArray();
            }
            return res;
        }

        private JSONArray getCurrentWeekForPlan(int plan) {
            JSONArray res;
            try {
                JSONObject plans = root.getJSONObject("plans");
                JSONArray weeks = plans.getJSONArray(planKeys[plan]);
                res = weeks.getJSONArray(weekInPlan);
            } catch (JSONException e) {
                Log.e("getCurrentWeekForPlan", "Error while parsing JSON", e);
                res = new JSONArray();
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

    private static int weekInPlan;

    public static void setWeekStart(int week) { weekInPlan = week; }

    private static DictWrapper createRootAndLibDict(Context context) {
        DictWrapper container = null;
        try {
            InputStreamReader input = new InputStreamReader(
              context.getAssets().open("workoutData.json"), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(input);
            StringBuilder contents = new StringBuilder(20000);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                contents.append(line);
            }
            JSONObject root = new JSONObject(contents.toString());
            JSONObject lib = root.getJSONObject("library");
            container = new DictWrapper(root, lib);
            reader.close();
        } catch (IOException e) {
            Log.e("createRootAndLibDict", "Error while opening JSON", e);
        } catch (JSONException e) {
            Log.e("createRootAndLibDict", "Error while parsing JSON", e);
        }
        return container;
    }

    public static String[] getWeeklyWorkoutNames(Context context, byte plan) {
        String[] names = {null, null, null, null, null, null, null};
        DictWrapper data = createRootAndLibDict(context);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan);

        for (int i = 0; i < 7; ++i) {
            try {
                JSONObject day = currWeek.getJSONObject(i);
                int temp = day.getInt(Keys.type);
                if (temp > 3) continue;

                JSONArray libArr = data.getLibraryArrayForType(temp);
                temp = day.getInt(Keys.index);
                JSONObject foundWorkout = libArr.getJSONObject(temp);
                names[i] = foundWorkout.getString(Keys.title);
            } catch (JSONException e) {
                Log.e("setWeeklyWorkoutNames", "Error while parsing JSON", e);
            }
        }
        return names;
    }

    public static WorkoutParams getWeeklyWorkout(Context context, int index, byte plan) {
        WorkoutParams params = new WorkoutParams((byte) index);
        DictWrapper data = createRootAndLibDict(context);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan);

        try {
            JSONObject day = currWeek.getJSONObject(index);
            params.type = (byte) day.getInt(Keys.type);
            params.index = day.getInt(Keys.index);
            params.sets = (short) day.getInt("sets");
            params.reps = (short) day.getInt(Keys.reps);
            params.weight = (short) day.getInt("weight");
        } catch (JSONException e) {
            Log.e("getWeeklyWorkoutParams", "Error while parsing JSON", e);
        }
        return params;
    }

    public static String[] getWorkoutNamesForType(Context context, int type) {
        DictWrapper data = createRootAndLibDict(context);
        JSONArray libArr = data.getLibraryArrayForType(type);
        int len = libArr.length();

        if (type == WorkoutType.strength)
            len = 2;
        String[] results = new String[len];

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

    static Workout getWorkoutFromLibrary(Context context, WorkoutParams params) {
        Workout w;
        DictWrapper data = createRootAndLibDict(context);
        JSONArray libArr = data.getLibraryArrayForType(params.type);

        try {
            JSONObject foundWorkout = libArr.getJSONObject(params.index);
            w = new Workout(context, foundWorkout, params);
        } catch (JSONException e) {
            Log.e("getWorkoutFromLibrary", "Error while parsing JSON", e);
            w = new Workout(context, new JSONObject(), params);
        }
        return w;
    }

    static int getBodyWeightToUse() {
        int weight = AppUserData.shared.weight;
        return weight < 0 ? 145 : weight;
    }
}
