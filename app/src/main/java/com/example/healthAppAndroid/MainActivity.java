package com.example.healthAppAndroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.homeTab.addWorkout.utils.NotificationService;
import com.github.mikephil.charting.utils.Utils;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences("AppDelPrefs", Context.MODE_PRIVATE);
        String hasLaunchedKey = "hasLaunched";
        boolean hasLaunched = prefs.getBoolean(hasLaunchedKey, false);
        int tzOffset = 0;

        if (!hasLaunched) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(hasLaunchedKey, true);
            editor.apply();
            AppUserData.create(this);
            NotificationService.setupAppNotifications(this);
            PersistenceService.create(this);
        } else {
            tzOffset = AppUserData.setupFromStorage(this);
            PersistenceService.init(this);
        }

        AppColors.setColors(this);
        NotificationService.init(this);
        Utils.init(this);

        AppCoordinator.create(this);
        int finalTzOffset = tzOffset;
        AsyncTask.execute(() -> PersistenceService.setup(finalTzOffset));
    }
}
