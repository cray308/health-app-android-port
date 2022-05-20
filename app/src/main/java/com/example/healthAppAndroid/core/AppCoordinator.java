package com.example.healthAppAndroid.core;

import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.homeTab.HomeFragment;
import com.google.android.material.navigation.NavigationBarView;

public final class AppCoordinator {
    private final FragmentManager fm;
    private final Fragment[] children = {new HomeFragment(), null, new SettingsFragment()};
    public final float toSavedMass;
    private int index;
    public final boolean metric;

    public static AppCoordinator shared;

    AppCoordinator(FragmentActivity activity, Object[][] args, int startIndex, boolean isMetric) {
        fm = activity.getSupportFragmentManager();
        children[1] = HistoryFragment.init(args);
        metric = isMetric;
        toSavedMass = isMetric ? 2.204623f : 1;
        index = startIndex;

        ((NavigationBarView)activity.findViewById(R.id.bottom_nav)).setOnItemSelectedListener(item -> {
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
    }

    public static boolean onEmulator() {
        String hw = Build.HARDWARE;
        return hw.contains("goldfish") || hw.contains("ranchu");
    }

    void updateUserInfo(byte plan, byte darkMode, short[] newArr) {
        int rv = AppUserData.shared.updateSettings(plan, darkMode, newArr);
        if ((rv & 2) != 0) {
            MainActivity.changeMode(darkMode == 1);
            return;
        }
        if ((rv & 1) != 0) ((HomeFragment)children[0]).createWorkoutsList(plan);
    }

    void deleteAppData() {
        new Thread(new PersistenceService.DeleteDataTask()).start();
        if (AppUserData.shared.deleteSavedData())
            ((HomeFragment)children[0]).updateWorkoutsList((byte)0);
        ((HistoryFragment)children[1]).handleDataDeletion();
    }

    public byte addWorkoutData(byte day, byte type, short duration, short[] weights) {
        short[] output = {0, 0, 0, 0};
        boolean[] updatedWeight = {false};
        new Thread(new PersistenceService.UpdateCurrentWeekTask(type, duration, weights)).start();
        byte completed = AppUserData.shared.addWorkoutData(day, weights, output, updatedWeight);
        if (updatedWeight[0]) ((SettingsFragment)children[2]).updateWeightFields(output);
        return completed;
    }
}
