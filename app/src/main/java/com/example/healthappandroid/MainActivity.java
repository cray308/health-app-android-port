package com.example.healthappandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.healthappandroid.common.helpers.DateHelper;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;
import com.example.healthappandroid.common.shareddata.PersistenceService;
import com.example.healthappandroid.hometab.addWorkout.utils.WorkoutNotifService;
import com.github.mikephil.charting.utils.Utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private final String hasLaunchedKey = "hasLaunched";

    private long getStartOfDay(long date, LocalDateTime info) {
        int seconds = (info.getHour() * 3600) + (info.getMinute() * 60) + info.getSecond();
        return date - seconds;
    }

    private long calcStartOfWeek(long date) {
        LocalDateTime localInfo = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(date), TimeZone.getDefault().toZoneId());
        int weekday = localInfo.getDayOfWeek().getValue();

        if (weekday == 1) return getStartOfDay(date, localInfo);

        date -= DateHelper.weekSeconds;
        while (weekday != 1) {
            date += DateHelper.daySeconds;
            weekday = weekday == 7 ? 1 : weekday + 1;
        }
        localInfo = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(date), TimeZone.getDefault().toZoneId());
        return getStartOfDay(date, localInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("MainActivity onCreate()");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences("AppDelPrefs", Context.MODE_PRIVATE);
        boolean hasLaunched = prefs.getBoolean(hasLaunchedKey, false);
        long now = Instant.now().getEpochSecond();
        long weekStart = calcStartOfWeek(now);

        if (!hasLaunched) {
            setupData(prefs, now, weekStart);
        } else {
            PersistenceService.create(this);
        }

        AppUserData.create(this);
        AppColors.setColors(this);
        Utils.init(this);

        int tzOffset = AppUserData.shared.checkTimezone(now);
        if (weekStart != AppUserData.shared.weekStart)
            AppUserData.shared.handleNewWeek(weekStart);

        AppCoordinator.create(getSupportFragmentManager(), findViewById(R.id.bottom_nav));
        AsyncTask.execute(() -> PersistenceService.setup(tzOffset));
    }

    private void setupData(SharedPreferences prefs, long now, long weekStart) {
        PersistenceService.createFromDB(this);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(hasLaunchedKey, true);
        editor.apply();
        AppUserData.setup(this, now, weekStart);

        WorkoutNotifService.setupAppNotifications(this);
    }
}