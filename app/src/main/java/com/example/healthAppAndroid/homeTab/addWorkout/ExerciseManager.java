package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.content.res.Resources;

import com.example.healthAppAndroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public abstract class ExerciseManager {
    static abstract class Keys {
        static final String reps = "R";
        static final String type = "T";
        static final String index = "I";
    }

    static final class WorkoutJSON {
        private final JSONObject root;
        final JSONArray lib;

        private WorkoutJSON(Context context) {
            JSONObject _root;
            JSONArray _lib;
            try {
                InputStream stream = context.getAssets().open("workoutData.json");
                InputStreamReader input = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(input);
                StringBuilder contents = new StringBuilder(11000);
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    contents.append(line);
                }
                _root = new JSONObject(contents.toString());
                _lib = _root.getJSONArray("L");
                reader.close();
            } catch (IOException | JSONException ignored) {
                _root = new JSONObject();
                _lib = new JSONArray();
            }
            root = _root;
            lib = _lib;
        }

        private JSONArray currentWeekForPlan(byte plan) {
            try {
                return root.getJSONArray("P").getJSONArray(plan).getJSONArray(weekInPlan);
            } catch (JSONException ignored) {
                return new JSONArray();
            }
        }
    }

    static final int[] TitleKeys = {
      R.array.wkNames0, R.array.wkNames1, R.array.wkNames2, R.array.wkNames3
    };
    private static int weekInPlan;

    public static void init(int week) {
        Workout.setupData();
        MutableString.one = String.format(Locale.getDefault(), "%d", 1);
        weekInPlan = week;
    }

    public static void setCurrentWeek(int week) { weekInPlan = week; }

    public static String[] weeklyWorkoutNames(Context context, byte plan) {
        String[] names = {null, null, null, null, null, null, null};
        JSONArray currWeek = new WorkoutJSON(context).currentWeekForPlan(plan);

        Resources res = context.getResources();
        String[][] workoutNames = {
          res.getStringArray(TitleKeys[0]), res.getStringArray(TitleKeys[1]),
          res.getStringArray(TitleKeys[2]), res.getStringArray(TitleKeys[3])
        };
        for (int i = 0; i < 7; ++i) {
            try {
                JSONObject day = currWeek.getJSONObject(i);
                int type = day.getInt(Keys.type);
                if (type > Workout.Type.HIC) continue;
                names[i] = workoutNames[type][day.getInt(Keys.index)];
            } catch (JSONException ignored) {}
        }
        return names;
    }

    public static Workout.Params weeklyWorkout(Context context, int index,
                                               byte plan, int bodyWeight, int[] lifts) {
        Workout.Params params = new Workout.Params((byte)index);

        try {
            JSONObject day = new WorkoutJSON(context).currentWeekForPlan(plan).getJSONObject(index);
            params.type = (byte)day.getInt(Keys.type);
            params.index = day.getInt(Keys.index);
            params.sets = day.getInt("S");
            params.reps = day.getInt(Keys.reps);
            params.weight = day.getInt("W");
            params.bodyWeight = bodyWeight;
            params.lifts = lifts;
        } catch (JSONException ignored) {}
        return params;
    }

    public static String[] workoutNamesForType(Context context, byte type) {
        String[] results = context.getResources().getStringArray(TitleKeys[type]);
        if (type == Workout.Type.strength) return new String[]{results[0], results[1]};
        return results;
    }

    static Workout workout(Context context, Workout.Params params) {
        return new Workout(context, new WorkoutJSON(context), params);
    }
}
