package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.AppUserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
            } catch (JSONException e) {
                Log.e("getLibraryArrayForType", "Error while parsing JSON", e);
                res = new JSONArray();
            }
            return res;
        }

        private JSONArray getCurrentWeekForPlan(int plan) {
            JSONArray res;
            try {
                JSONArray plans = root.getJSONArray("P");
                JSONArray weeks = plans.getJSONArray(plan);
                res = weeks.getJSONArray(weekInPlan);
            } catch (JSONException e) {
                Log.e("getCurrentWeekForPlan", "Error while parsing JSON", e);
                res = new JSONArray();
            }
            return res;
        }
    }

    public static void init(Context c, int week) {
        NotificationService.init(c);
        ExerciseEntry.setupHeaderData(c);
        Circuit.setupHeaderData(c);
        weekInPlan = week;
        oneCount = one.length();
    }

    static abstract class Keys {
        static final String reps = "R";
        static final String type = "T";
        static final String index = "I";
    }

    static String one = String.format(Locale.getDefault(), "%d", 1);
    static int oneCount;
    private static int weekInPlan;
    final static int[] titleKeys = {
      R.array.wkNames0, R.array.wkNames1, R.array.wkNames2, R.array.wkNames3
    };

    public static void setWeekStart(int week) { weekInPlan = week; }

    private static DictWrapper createRootAndLibDict(Context c) {
        DictWrapper container = null;
        try {
            InputStreamReader input = new InputStreamReader(
              c.getAssets().open("workoutData.json"), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(input);
            StringBuilder contents = new StringBuilder(11000);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                contents.append(line);
            }
            JSONObject root = new JSONObject(contents.toString());
            container = new DictWrapper(root, root.getJSONArray("L"));
            reader.close();
        } catch (IOException e) {
            Log.e("createRootAndLibDict", "Error while opening JSON", e);
        } catch (JSONException e) {
            Log.e("createRootAndLibDict", "Error while parsing JSON", e);
        }
        return container;
    }

    public static String[] getWeeklyWorkoutNames(Context c, byte plan) {
        String[] names = {null, null, null, null, null, null, null};
        DictWrapper data = createRootAndLibDict(c);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan);

        Resources res = c.getResources();
        String[][] wNames = {res.getStringArray(titleKeys[0]), res.getStringArray(titleKeys[1]),
                             res.getStringArray(titleKeys[2]), res.getStringArray(titleKeys[3])};
        for (int i = 0; i < 7; ++i) {
            try {
                JSONObject day = currWeek.getJSONObject(i);
                int temp = day.getInt(Keys.type);
                if (temp > 3) continue;

                names[i] = wNames[temp][day.getInt(Keys.index)];
            } catch (JSONException e) {
                Log.e("setWeeklyWorkoutNames", "Error while parsing JSON", e);
            }
        }
        return names;
    }

    public static WorkoutParams getWeeklyWorkout(Context c, int index, byte plan) {
        WorkoutParams params = new WorkoutParams((byte)index);
        DictWrapper data = createRootAndLibDict(c);
        JSONArray currWeek = data.getCurrentWeekForPlan(plan);

        try {
            JSONObject day = currWeek.getJSONObject(index);
            params.type = (byte)day.getInt(Keys.type);
            params.index = day.getInt(Keys.index);
            params.sets = (short)day.getInt("S");
            params.reps = (short)day.getInt(Keys.reps);
            params.weight = (short)day.getInt("W");
        } catch (JSONException e) {
            Log.e("getWeeklyWorkoutParams", "Error while parsing JSON", e);
        }
        return params;
    }

    public static String[] getWorkoutNamesForType(Context c, int type) {
        String[] results = c.getResources().getStringArray(titleKeys[type]);
        if (type == WorkoutType.strength) return new String[]{results[0], results[1]};
        return results;
    }

    static Workout getWorkoutFromLibrary(Context c, WorkoutParams params) {
        Workout w;
        DictWrapper data = createRootAndLibDict(c);
        JSONArray libArr = data.getLibraryArrayForType(params.type);

        try {
            w = new Workout(c, libArr.getJSONArray(params.index), params);
        } catch (JSONException e) {
            Log.e("getWorkoutFromLibrary", "Error while parsing JSON", e);
            w = new Workout(c, new JSONArray(), params);
        }
        return w;
    }

    static int getBodyWeightToUse() {
        int weight = AppUserData.shared.weight;
        return weight < 0 ? 165 : weight;
    }
}
