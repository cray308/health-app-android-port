package com.example.healthAppAndroid.core;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import android.os.AsyncTask;
import android.os.Build;
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
        LocaleData.MeasurementSystem sys = LocaleData.getMeasurementSystem(ULocale.getDefault());
        boolean metric =
          sys.equals(LocaleData.MeasurementSystem.SI) || sys.equals(LocaleData.MeasurementSystem.UK);
        if (AppCoordinator.shared != null) {
            super.onCreate(null);
            setContentView(R.layout.activity_main);
            AppColors.setColors(this);
            AppCoordinator.shared = new AppCoordinator(this, null, metric);
            return;
        }

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
        int[] results = {0, 0};
        Object[][] args = {new Object[]{null}, new Object[]{null}};
        boolean modern = Build.VERSION.SDK_INT > 28;

        if (!prefs.getBoolean(hasLaunchedKey, false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(hasLaunchedKey, true);
            editor.apply();
            AppUserData.shared = new AppUserData(this, weekStart, tzOffset, modern);
            NotificationService.setupAppNotifications(this);
            PersistenceService.create(this);
        } else {
            AppUserData.shared = new AppUserData(this, results, weekStart, tzOffset, modern);
            PersistenceService.init(this);
        }

        if (!modern) {
            int mode = AppUserData.shared.darkMode == 0
                       ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(mode);
        }

        super.onCreate(null);
        setContentView(R.layout.activity_main);

        AppColors.setColors(this);
        Utils.init(this);
        ExerciseManager.init(this, results[1], metric);

        AppCoordinator.shared = new AppCoordinator(this, args, metric);
        AsyncTask.execute(() -> PersistenceService.start(zoneId, weekStart, results[0], args));
    }
}
