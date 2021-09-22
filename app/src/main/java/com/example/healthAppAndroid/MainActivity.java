package com.example.healthAppAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.healthAppAndroid.common.helpers.DateHelper;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;
import com.github.mikephil.charting.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private final String hasLaunchedKey = "hasLaunched";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences("AppDelPrefs", Context.MODE_PRIVATE);
        boolean hasLaunched = prefs.getBoolean(hasLaunchedKey, false);
        long now = DateHelper.getCurrentTime();
        long weekStart = DateHelper.calcStartOfWeek(now);

        if (!hasLaunched) {
            setupData(prefs, now, weekStart);
        } else {
            AppUserData.create(this);
            PersistenceService.create(this);
        }

        AppColors.setColors(this);
        NotificationService.init(this);
        Utils.init(this);

        int tzOffset = AppUserData.shared.checkTimezone(now);
        if (weekStart != AppUserData.shared.weekStart)
            AppUserData.shared.handleNewWeek(weekStart);

        AppCoordinator.create(this);
        AsyncTask.execute(() -> PersistenceService.setup(tzOffset));
    }

    private void setupData(SharedPreferences prefs, long now, long weekStart) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(hasLaunchedKey, true);
        editor.apply();
        AppUserData.setup(this, now, weekStart);
        NotificationService.setupAppNotifications(this);
        if (BuildConfig.DEBUG) {
            PersistenceService.createFromDB(this);
        } else {
            PersistenceService.create(this);
        }
    }
}
