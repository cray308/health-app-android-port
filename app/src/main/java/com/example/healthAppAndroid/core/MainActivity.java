package com.example.healthAppAndroid.core;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.NotificationService;
import com.github.mikephil.charting.utils.Utils;

public final class MainActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("AppDelPrefs", Context.MODE_PRIVATE);
        String hasLaunchedKey = "hasLaunched";
        int[] tzArr = {0}, weekArr = {0};
        long[] weekStartArr = {0};
        Object[] args = {null, null};

        if (!prefs.getBoolean(hasLaunchedKey, false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(hasLaunchedKey, true);
            editor.apply();
            AppUserData.shared = new AppUserData(this, weekStartArr);
            NotificationService.setupAppNotifications(this);
            PersistenceService.create(this);
        } else {
            AppUserData.shared = new AppUserData(this, weekStartArr, tzArr, weekArr);
            PersistenceService.init(this);
        }

        AppColors.setColors(this);
        NotificationService.init(this);
        Utils.init(this);
        ExerciseManager.setWeekStart(weekArr[0]);

        AppCoordinator.shared = new AppCoordinator(this, args);
        AsyncTask.execute(() -> PersistenceService.start(weekStartArr[0], tzArr[0], args));
    }
}
