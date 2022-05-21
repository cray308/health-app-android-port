package com.example.healthAppAndroid.core;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.homeTab.HomeFragment;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.NotificationService;
import com.example.healthAppAndroid.homeTab.addWorkout.Workout;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.navigation.NavigationBarView;

public final class MainActivity extends AppCompatActivity {
    private static abstract class Tab {
        private static final int home = 0;
        private static final int history = 1;
        private static final int settings = 2;
    }

    public static UserData userData;
    private static boolean changedMode;

    private static int nightMode() {
        return userData.darkMode == 1
               ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
    }

    private final Fragment[] tabs = {
      new HomeFragment(), new HistoryFragment(), new SettingsFragment()
    };
    private int index;

    protected void onCreate(Bundle savedInstanceState) {
        boolean modern = Build.VERSION.SDK_INT > 28;
        int tzDiff = 0, week = 0;
        SharedPreferences prefs = getSharedPreferences("HealthAppPrefs", MODE_PRIVATE);
        if (prefs.getLong(UserData.Keys.weekStart, 0) != 0) {
            UserData.TimeData[] timeData = {null};
            userData = new UserData(prefs, timeData, modern);
            tzDiff = timeData[0].tzDiff;
            week = timeData[0].week;
            PersistenceManager.init(this);
        } else {
            userData = new UserData(prefs, modern);
            NotificationService.setupAppNotifications(this);
            PersistenceManager.create(this);
        }

        if (savedInstanceState == null && !modern) AppCompatDelegate.setDefaultNightMode(nightMode());

        super.onCreate(null);
        setContentView(R.layout.activity_main);

        Utils.init(this);
        ExerciseManager.init(week);

        FragmentManager fm = getSupportFragmentManager();

        if (changedMode) index = 2;
        changedMode = false;

        ((NavigationBarView)findViewById(R.id.bottom_nav)).setOnItemSelectedListener(item -> {
            int newIndex = Tab.home;
            int id = item.getItemId();
            if (id == R.id.history) {
                newIndex = Tab.history;
            } else if (id == R.id.settings) {
                newIndex = Tab.settings;
            }

            fm.beginTransaction().hide(tabs[index]).show(tabs[newIndex]).commit();
            index = newIndex;
            return true;
        });

        for (int i = 0; i < 3; ++i) {
            String tag = String.valueOf(i + 1);
            FragmentTransaction transaction = fm.beginTransaction().add(R.id.container, tabs[i], tag);
            if (i != index) transaction.hide(tabs[i]);
            transaction.commit();
        }

        HistoryFragment.Block block = new HistoryFragment.Block((HistoryFragment)tabs[1]);
        new Thread(new PersistenceManager.StartupTask(block, userData.weekStart, tzDiff)).start();
    }

    void updateUserInfo(byte plan, byte darkMode, int[] weights) {
        byte updates = userData.update(plan, darkMode, weights);
        if ((updates & UserData.Mask.darkMode) != 0) {
            changedMode = true;
            AppCompatDelegate.setDefaultNightMode(nightMode());
            return;
        }

        if ((updates & UserData.Mask.plan) != 0)
            ((HomeFragment)tabs[Tab.home]).createWorkoutsList(userData);
    }

    void deleteAppData() {
        if (userData.clear()) ((HomeFragment)tabs[Tab.home]).updateWorkoutsList((byte)0);
        ((HistoryFragment)tabs[Tab.history]).clearData();
        new Thread(new PersistenceManager.DeleteDataTask()).start();
    }

    public byte addWorkoutData(Workout.Output data) {
        if (data.duration < Workout.minDuration) return 0;

        UserData.WorkoutResult result = userData.addWorkoutData(data.day, data.weights);
        if (result.updatedWeights)
            ((SettingsFragment)tabs[Tab.settings]).updateFields(userData.lifts);
        new Thread(new PersistenceManager.WorkoutDataTask(data)).start();
        return result.completedWorkouts;
    }
}
