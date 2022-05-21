package com.example.healthAppAndroid.core;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.content.SharedPreferences;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.healthAppAndroid.BuildConfig;
import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.homeTab.HomeFragment;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.NotificationService;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutData;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutType;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.navigation.NavigationBarView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class MainActivity extends AppCompatActivity {
    private static abstract class Keys {
        private static final String weekStart = "weekStart";
        private static final String planStart = "planStart";
        private static final String tzOffset = "tzOffset";
        private static final String currentPlan = "currentPlan";
        private static final String completed = "completedWorkouts";
        private static final String darkMode = "darkMode";
        private static final String bodyWeight = "weight";
        private static final String[] liftKeys = {"squatMax", "pullUpMax", "benchMax", "deadLiftMax"};
    }

    private static UserData userData;
    private static AppDB service;
    private static final String DBName = "HealthApp-db";
    private final static long weekSeconds = 604800;
    public static float toSavedMass;
    public static boolean metric;
    private static boolean changedMode = false;

    public final static class UserData {
        public long planStart;
        private final long weekStart;
        public final short[] liftArray = {0, 0, 0, 0};
        public short weight = -1;
        public byte currentPlan = -1;
        public byte completedWorkouts;
        byte darkMode;

        private UserData(long weekStart) { this.weekStart = weekStart; }

        public int weightToUse() { return weight < 0 ? 165 : weight; }
    }

    private SharedPreferences prefs;
    private FragmentManager fm;
    private Fragment[] children;
    private int index;

    protected void onCreate(Bundle savedInstanceState) {
        ZoneId zoneId = ZoneId.systemDefault();
        long now = Instant.now().getEpochSecond();
        Instant instant = Instant.ofEpochSecond(now);
        int tzOffset = OffsetDateTime.ofInstant(instant, zoneId).getOffset().getTotalSeconds();
        LocalDateTime tm = LocalDateTime.ofInstant(instant, zoneId);
        int weekday = tm.getDayOfWeek().getValue(), tzDiff = 0, week = 0;
        if (weekday != 1) {
            now = now - weekSeconds + (((8 - weekday) % 7) * 86400);
            tm = LocalDateTime.ofInstant(Instant.ofEpochSecond(now), zoneId);
        }
        long weekStart = now - ((tm.getHour() * 3600L) + (tm.getMinute() * 60L) + tm.getSecond());

        prefs = getSharedPreferences("HealthAppPrefs", 0);
        userData = new UserData(weekStart);
        SharedPreferences sp = getSharedPreferences("AppDelPrefs", 0);
        String hasLaunchedKey = "hasLaunched";
        Object[][] args = {new Object[]{null}, new Object[]{null}};
        boolean modern = Build.VERSION.SDK_INT > 28;

        if (sp.getBoolean(hasLaunchedKey, false)) {
            int[] planLengths = {8, 13};
            byte changes = 0;
            userData.planStart = prefs.getLong(Keys.planStart, 0);
            long savedWeekStart = prefs.getLong(Keys.weekStart, 0);
            int savedTzOffset = prefs.getInt(Keys.tzOffset, 0);
            userData.currentPlan = (byte)prefs.getInt(Keys.currentPlan, -1);
            userData.completedWorkouts = (byte)prefs.getInt(Keys.completed, 0);
            userData.darkMode = (byte)prefs.getInt(Keys.darkMode, -1);

            if ((tzDiff = savedTzOffset - tzOffset) != 0) {
                userData.planStart += tzDiff;
                if (weekStart != savedWeekStart) {
                    changes = 7;
                    savedWeekStart += tzDiff;
                } else {
                    changes = 6;
                    tzDiff = 0;
                }
            }

            week = (int)((weekStart - userData.planStart) / weekSeconds);
            if (weekStart != savedWeekStart) {
                changes |= 17;
                userData.completedWorkouts = 0;

                if (userData.currentPlan >= 0 && week >= planLengths[userData.currentPlan]) {
                    if (userData.currentPlan == 0) {
                        userData.currentPlan = 1;
                        changes |= 8;
                    }
                    userData.planStart = weekStart;
                    changes |= 2;
                    week = 0;
                }
            }

            if (userData.darkMode >= 0 && modern) {
                userData.darkMode = -1;
                changes |= 32;
            }

            userData.weight = (short)prefs.getInt(Keys.bodyWeight, -1);
            for (int i = 0; i < 4; ++i) {
                userData.liftArray[i] = (short)prefs.getInt(Keys.liftKeys[i], 0);
            }

            if (changes != 0) {
                SharedPreferences.Editor editor = prefs.edit();
                if ((changes & 1) != 0) editor.putLong(Keys.weekStart, weekStart);
                if ((changes & 2) != 0) editor.putLong(Keys.planStart, userData.planStart);
                if ((changes & 4) != 0) editor.putInt(Keys.tzOffset, tzOffset);
                if ((changes & 8) != 0) editor.putInt(Keys.currentPlan, userData.currentPlan);
                if ((changes & 16) != 0) editor.putInt(Keys.completed, userData.completedWorkouts);
                if ((changes & 32) != 0) editor.putInt(Keys.darkMode, userData.darkMode);
                editor.apply();
            }
            initDB();
        } else {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(hasLaunchedKey, true);
            editor.apply();
            handleFirstLaunch(weekStart, tzOffset, modern);
        }

        if (savedInstanceState == null && !modern) {
            int mode = userData.darkMode == 0
                       ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(mode);
        }

        super.onCreate(null);
        setContentView(R.layout.activity_main);
        LocaleData.MeasurementSystem sys = LocaleData.getMeasurementSystem(ULocale.getDefault());
        metric = !sys.equals(LocaleData.MeasurementSystem.US);
        toSavedMass = metric ? 2.204623f : 1;

        AppColors.setColors(this);
        Utils.init(this);
        ExerciseManager.init(this, week, metric);

        fm = getSupportFragmentManager();
        children = new Fragment[]{new HomeFragment(), HistoryFragment.init(args), new SettingsFragment()};
        index = changedMode ? 2 : 0;
        changedMode = false;
        ((NavigationBarView)findViewById(R.id.bottom_nav)).setOnItemSelectedListener(item -> {
            int newIndex = 0;
            int id = item.getItemId();
            if (id == R.id.history) {
                newIndex = 1;
            } else if (id == R.id.settings) {
                newIndex = 2;
            }

            fm.beginTransaction().hide(children[index]).show(children[newIndex]).commit();
            index = newIndex;
            return true;
        });

        for (int i = 0; i < 3; ++i) {
            String tag = String.valueOf(i + 1);
            FragmentTransaction t = fm.beginTransaction().add(R.id.container, children[i], tag);
            if (i != index) t.hide(children[i]);
            t.commit();
        }

        int diff = tzDiff;
        AsyncTask.execute(() -> runStartupJob(zoneId, weekStart, diff, args));
    }

    private void handleFirstLaunch(long weekStart, int tzOffset, boolean modern) {
        userData.planStart = weekStart;
        userData.darkMode = (byte)(modern ? -1 : 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Keys.planStart, weekStart);
        editor.putLong(Keys.weekStart, weekStart);
        editor.putInt(Keys.tzOffset, tzOffset);
        editor.putInt(Keys.currentPlan, -1);
        editor.putInt(Keys.completed, 0);
        editor.putInt(Keys.darkMode, userData.darkMode);
        editor.putInt(Keys.bodyWeight, -1);
        editor.putInt(Keys.liftKeys[0], 0);
        editor.putInt(Keys.liftKeys[1], 0);
        editor.putInt(Keys.liftKeys[2], 0);
        editor.putInt(Keys.liftKeys[3], 0);
        editor.apply();

        NotificationService.setupAppNotifications(this);
        if (BuildConfig.DEBUG) {
            service = Room.databaseBuilder(this, AppDB.class, DBName)
                          .createFromAsset("test.db").build();
        } else {
            initDB();
        }
    }

    private void initDB() {
        if (service == null) service = Room.databaseBuilder(this, AppDB.class, DBName).build();
    }

    private static void fetchHistory(ZoneId zoneId, Object[][] args, AppDB.DAO dao) {
        AppDB.WeeklyData[] data = dao.getAllSorted();
        WeekDataModel model = new WeekDataModel(data.length);
        args[0][0] = model;
        if (model.size != 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            for (int i = 0; i < model.size; ++i) {
                model.arr[i] = new WeekDataModel.Week(data[i], zoneId, formatter);
            }
        }
        new Handler(Looper.getMainLooper()).post(((HistoryFragment.Block)args[1][0])::completion);
    }

    private static void runStartupJob(ZoneId zoneId, long weekStart, int tzOffset, Object[][] args) {
        long endPt = weekStart - 63244800;
        AppDB.DAO dao = service.dao();
        AppDB.WeeklyData[] data = dao.getAllSorted();
        int count = data.length;

        if (count == 0) {
            AppDB.WeeklyData first = new AppDB.WeeklyData();
            first.start = weekStart;
            dao.insertWeeks(new AppDB.WeeklyData[]{first});
            fetchHistory(zoneId, args, dao);
            return;
        }

        if (tzOffset != 0) {
            for (AppDB.WeeklyData datum : data) {
                datum.start += tzOffset;
            }
            dao.updateWeeks(data);
        }

        AppDB.WeeklyData[] newEntries = new AppDB.WeeklyData[count];
        AppDB.WeeklyData[] oldEntries = new AppDB.WeeklyData[count];
        int oldCount = 0, newCount = 0;
        AppDB.WeeklyData last = data[count - 1];
        long start = last.start;
        if (start != weekStart) {
            AppDB.WeeklyData currWeek = new AppDB.WeeklyData();
            currWeek.start = weekStart;
            currWeek.copyLiftMaxes(last);
            newEntries[newCount++] = currWeek;
        }

        for (AppDB.WeeklyData d : data) {
            if (d.start < endPt) oldEntries[oldCount++] = d;
        }

        for (start = last.start + weekSeconds; start < weekStart; start += weekSeconds) {
            AppDB.WeeklyData curr = new AppDB.WeeklyData();
            curr.start = start;
            curr.copyLiftMaxes(last);
            newEntries[newCount++] = curr;
        }

        if (oldCount != 0) {
            AppDB.WeeklyData[] deleted = new AppDB.WeeklyData[oldCount];
            System.arraycopy(oldEntries, 0, deleted, 0, oldCount);
            dao.delete(deleted);
        }
        if (newCount != 0) {
            AppDB.WeeklyData[] inserted = new AppDB.WeeklyData[newCount];
            System.arraycopy(newEntries, 0, inserted, 0, newCount);
            dao.insertWeeks(inserted);
        }
        fetchHistory(zoneId, args, dao);
    }

    public static boolean onEmulator() {
        String hw = Build.HARDWARE;
        return hw.contains("goldfish") || hw.contains("ranchu");
    }

    public static UserData getUserData() { return userData; }

    private static boolean updateWeights(short[] newLifts, short[] output, SharedPreferences.Editor editor) {
        boolean madeChange = false;
        for (int i = 0; i < 4; ++i) {
            short old = userData.liftArray[i];
            short newVal = newLifts[i];
            if (newVal > old) {
                madeChange = true;
                userData.liftArray[i] = newVal;
                editor.putInt(Keys.liftKeys[i], newVal);
                output[i] = newVal;
            } else {
                output[i] = old;
            }
        }
        return madeChange;
    }

    void updateUserInfo(byte plan, byte darkMode, short[] newArr) {
        SharedPreferences.Editor editor = prefs.edit();
        int changes = plan == userData.currentPlan ? 0 : 1;
        if (changes != 0) {
            userData.currentPlan = plan;
            editor.putInt(Keys.currentPlan, plan);
            if (plan >= 0) {
                if (onEmulator()) {
                    userData.planStart = userData.weekStart;
                    ExerciseManager.setWeekStart(0);
                } else {
                    userData.planStart = userData.weekStart + weekSeconds;
                }
                editor.putLong(Keys.planStart, userData.planStart);
            }
        }

        if (darkMode != userData.darkMode) {
            changes |= 2;
            userData.darkMode = darkMode;
            editor.putInt(Keys.darkMode, darkMode);
        }

        short newWeight = newArr[4];
        if (newWeight != userData.weight) {
            changes |= 4;
            userData.weight = newWeight;
            editor.putInt(Keys.bodyWeight, newWeight);
        }

        if (updateWeights(newArr, new short[]{0, 0, 0, 0}, editor) || changes != 0) editor.apply();
        if ((changes & 2) != 0) {
            changedMode = true;
            int mode = darkMode == 1
                       ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);
            return;
        }
        if ((changes & 1) != 0) ((HomeFragment)children[0]).createWorkoutsList(userData);
    }

    void deleteAppData() {
        new Thread(new DeleteDataTask()).start();
        if (userData.completedWorkouts != 0) {
            userData.completedWorkouts = 0;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Keys.completed, 0);
            editor.apply();
            ((HomeFragment)children[0]).updateWorkoutsList((byte)0);
        }
        ((HistoryFragment)children[1]).handleDataDeletion();
    }

    private static final class DeleteDataTask implements Runnable {
        public void run() {
            AppDB.DAO dao = service.dao();
            AppDB.WeeklyData[] data = dao.getAllSorted();
            int count = data.length;
            if (count == 0) return;

            int end = count - 1;
            if (end != 0) {
                AppDB.WeeklyData[] toDelete = new AppDB.WeeklyData[end];
                System.arraycopy(data, 0, toDelete, 0, end);
                dao.delete(toDelete);
            }
            AppDB.WeeklyData currWeek = data[end];
            currWeek.totalWorkouts = 0;
            currWeek.timeEndurance = 0;
            currWeek.timeHIC = 0;
            currWeek.timeSE = 0;
            currWeek.timeStrength = 0;
            dao.updateWeeks(new AppDB.WeeklyData[]{currWeek});
        }
    }

    public byte addWorkoutData(WorkoutData data) {
        new Thread(new WorkoutDataTask(data)).start();
        SharedPreferences.Editor editor = prefs.edit();
        byte completed = 0;
        boolean madeChange = false;
        short[] output = {0, 0, 0, 0};
        if (data.weights[0] != -1 && updateWeights(data.weights, output, editor)) {
            madeChange = true;
            ((SettingsFragment)children[2]).updateWeightFields(output);
        }
        if (data.day >= 0) {
            madeChange = true;
            userData.completedWorkouts |= (1 << data.day);
            completed = userData.completedWorkouts;
            editor.putInt(Keys.completed, completed);
        }
        if (madeChange) editor.apply();
        return completed;
    }

    private static final class WorkoutDataTask implements Runnable {
        private final WorkoutData data;

        private WorkoutDataTask(WorkoutData data) { this.data = data; }

        public void run() {
            AppDB.DAO dao = service.dao();
            AppDB.WeeklyData curr = dao.findCurrentWeek();
            curr.totalWorkouts += 1;
            if (data.type == WorkoutType.strength) {
                curr.timeStrength += data.duration;
            } else if (data.type == WorkoutType.SE) {
                curr.timeSE += data.duration;
            } else if (data.type == WorkoutType.endurance) {
                curr.timeEndurance += data.duration;
            } else {
                curr.timeHIC += data.duration;
            }

            if (data.weights[0] != -1) {
                curr.bestSquat = data.weights[0];
                curr.bestPullup = data.weights[1];
                curr.bestBench = data.weights[2];
                curr.bestDeadlift = data.weights[3];
            }
            dao.updateWeeks(new AppDB.WeeklyData[]{curr});
        }
    }
}
