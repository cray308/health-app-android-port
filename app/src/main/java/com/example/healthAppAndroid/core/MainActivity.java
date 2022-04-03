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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class MainActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ZoneId zoneId = ZoneId.systemDefault();
        long now = Instant.now().getEpochSecond();
        Instant instant = Instant.ofEpochSecond(now);
        int tzOffset = OffsetDateTime.ofInstant(instant, zoneId).getOffset().getTotalSeconds();
        LocalDateTime tm = LocalDateTime.ofInstant(instant, zoneId);
        int weekday = tm.getDayOfWeek().getValue();
        if (weekday != 1) {
            now = now - AppUserData.weekSeconds + (((8 - weekday) % 7) * 86400);
            tm = LocalDateTime.ofInstant(Instant.ofEpochSecond(now), zoneId);
        }
        long weekStart = now - ((tm.getHour() * 3600L) + (tm.getMinute() * 60L) + tm.getSecond());

        SharedPreferences prefs = getSharedPreferences("AppDelPrefs", Context.MODE_PRIVATE);
        String hasLaunchedKey = "hasLaunched";
        int[] tzArr = {0}, weekArr = {0};
        Object[] args = {null, null};

        if (!prefs.getBoolean(hasLaunchedKey, false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(hasLaunchedKey, true);
            editor.apply();
            AppUserData.shared = new AppUserData(this, weekStart, tzOffset);
            NotificationService.setupAppNotifications(this);
            PersistenceService.create(this);
        } else {
            AppUserData.shared = new AppUserData(this, tzArr, weekArr, weekStart, tzOffset);
            PersistenceService.init(this);
        }

        AppColors.setColors(this);
        Utils.init(this);
        ExerciseManager.init(this, weekArr[0]);
        SegmentedControl.selectedStr = getString(R.string.segCtrlSelected);

        AppCoordinator.shared = new AppCoordinator(this, args);
        AsyncTask.execute(() -> PersistenceService.start(zoneId, weekStart, tzArr[0], args));
    }
}
