package com.example.healthAppAndroid;

import androidx.appcompat.app.AppCompatActivity;

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

public final class MainActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("AppDelPrefs", Context.MODE_PRIVATE);
        String hasLaunchedKey = "hasLaunched";
        int tzOffset = 0;

        if (!prefs.getBoolean(hasLaunchedKey, false)) {
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

        PersistenceService.Block block = (PersistenceService.Block) AppCoordinator.create(this);
        int finalTzOffset = tzOffset;
        AsyncTask.execute(() -> PersistenceService.start(finalTzOffset, block));
    }
}
