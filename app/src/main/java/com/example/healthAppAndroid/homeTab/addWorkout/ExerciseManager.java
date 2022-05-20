package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.content.res.Resources;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppUserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class ExerciseManager {
    private static final class DictWrapper {
        private final JSONObject root;
        private final JSONArray lib;

        private DictWrapper(JSONObject root, JSONArray lib) {
            this.root = root;
            this.lib = lib;
        }

        private JSONArray getLibraryArrayForType(int type) {
            JSONArray res;
            try {
                res = lib.getJSONArray(type);
            } catch (JSONException ignored) {
                res = new JSONArray();
            }
            return res;
        }

        private JSONArray getCurrentWeekForPlan(int plan) {
            JSONArray res;
            try {
                res = root.getJSONArray("P").getJSONArray(plan).getJSONArray(weekInPlan);
            } catch (JSONException ignored) {
                res = new JSONArray();
            }
            return res;
        }
    }

    public static void init(Context c, int week, boolean metric) {
        NotificationService.init(c);
        ExerciseEntry.setupData(c, metric);
        Circuit.setupHeaderData(c);
        weekInPlan = week;
    }

    static abstract class Keys {
        static final String reps = "R";
        static final String type = "T";
        static final String index = "I";
    }

    private static int weekInPlan;
    final static int[] titleKeys = {
      R.array.wkNames0, R.array.wkNames1, R.array.wkNames2, R.array.wkNames3
    };

    public static void setWeekStart(int week) { weekInPlan = week; }

    private static DictWrapper createRootAndLibDict(Context c) {
        DictWrapper container = null;
        try {
            InputStream stream = c.getAssets().open("workoutData.json");
            InputStreamReader input = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(input);
            StringBuilder contents = new StringBuilder(11000);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                contents.append(line);
            }
            JSONObject root = new JSONObject(contents.toString());
            container = new DictWrapper(root, root.getJSONArray("L"));
            reader.close();
        } catch (IOException | JSONException ignored) {}
        return container;
    }

    public static String[] getWeeklyWorkoutNames(Context c, byte plan) {
        String[] names = {null, null, null, null, null, null, null};
        JSONArray currWeek = createRootAndLibDict(c).getCurrentWeekForPlan(plan);

        Resources res = c.getResources();
        String[][] wNames = {res.getStringArray(titleKeys[0]), res.getStringArray(titleKeys[1]),
                             res.getStringArray(titleKeys[2]), res.getStringArray(titleKeys[3])};
        for (int i = 0; i < 7; ++i) {
            try {
                JSONObject day = currWeek.getJSONObject(i);
                int temp = day.getInt(Keys.type);
                if (temp > 3) continue;

                names[i] = wNames[temp][day.getInt(Keys.index)];
            } catch (JSONException ignored) {}
        }
        return names;
    }

    public static WorkoutParams getWeeklyWorkout(Context c, int index) {
        WorkoutParams params = new WorkoutParams((byte)index);
        DictWrapper data = createRootAndLibDict(c);
        JSONArray currWeek = data.getCurrentWeekForPlan(AppUserData.shared.currentPlan);

        try {
            JSONObject day = currWeek.getJSONObject(index);
            params.type = (byte)day.getInt(Keys.type);
            params.index = day.getInt(Keys.index);
            params.sets = (short)day.getInt("S");
            params.reps = (short)day.getInt(Keys.reps);
            params.weight = (short)day.getInt("W");
        } catch (JSONException ignored) {}
        return params;
    }

    public static String[] getWorkoutNamesForType(Context c, int type) {
        String[] results = c.getResources().getStringArray(titleKeys[type]);
        if (type == WorkoutType.strength) return new String[]{results[0], results[1]};
        return results;
    }

    static Workout getWorkout(Context c, WorkoutParams params) {
        Workout w;
        JSONArray libArr = createRootAndLibDict(c).getLibraryArrayForType(params.type);

        try {
            w = new Workout(c, libArr.getJSONArray(params.index), params);
        } catch (JSONException ignored) {
            w = new Workout(c, new JSONArray(), params);
        }
        return w;
    }

    static int getBodyWeightToUse() {
        int weight = AppUserData.shared.weight;
        return weight < 0 ? 165 : weight;
    }
}
