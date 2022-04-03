package com.example.healthAppAndroid.core;

import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.homeTab.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class AppCoordinator {
    private final FragmentManager fm;
    private final Fragment[] children = {new HomeFragment(), null, new SettingsFragment()};
    private Fragment active;

    public static AppCoordinator shared;
    public final boolean onEmulator;

    AppCoordinator(FragmentActivity activity, Object[] results) {
        String fp = Build.FINGERPRINT, hw = Build.HARDWARE, pr = Build.PRODUCT, m = Build.MODEL;
        onEmulator = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                     || fp.startsWith("generic") || fp.startsWith("unknown") || hw.contains("goldfish")
                     || hw.contains("ranchu") || m.contains("google_sdk") || m.contains("Emulator")
                     || m.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion")
                     || pr.contains("sdk_google") || pr.contains("google_sdk") || pr.contains("sdk")
                     || pr.contains("sdk_x86") || pr.contains("sdk_gphone64_arm64") || pr.contains("vbox86p")
                     || pr.contains("emulator") || pr.contains("simulator");

        fm = activity.getSupportFragmentManager();
        children[1] = new HistoryFragment(results);

        ((BottomNavigationView)activity.findViewById(R.id.bottom_nav)).setOnItemSelectedListener(item -> {
            int index = 0;
            int id = item.getItemId();
            if (id == R.id.history) {
                index = 1;
            } else if (id == R.id.settings) {
                index = 2;
            }

            fm.beginTransaction().hide(active).show(children[index]).commit();
            active = children[index];
            return true;
        });

        fm.beginTransaction().add(R.id.container, children[2], "3").hide(children[2]).commit();
        fm.beginTransaction().add(R.id.container, children[1], "2").hide(children[1]).commit();
        fm.beginTransaction().add(R.id.container, children[0], "1").commit();
        active = children[0];
    }

    void updateUserInfo(byte plan, short[] newArr) {
        if (AppUserData.shared.updateSettings(plan, newArr))
            ((HomeFragment)children[0]).createWorkoutsList(plan);
    }

    void deleteAppData() {
        PersistenceService.deleteAppData();
        if (AppUserData.shared.deleteSavedData())
            ((HomeFragment)children[0]).updateWorkoutsList((byte)0);
        ((HistoryFragment)children[1]).handleDataDeletion();
    }

    public byte addWorkoutData(byte day, byte type, short duration, short[] weights) {
        short[] output = {0, 0, 0, 0};
        boolean[] updatedWeight = {false};
        PersistenceService.updateCurrentWeek(type, duration, weights);
        byte completed = AppUserData.shared.addWorkoutData(day, weights, output, updatedWeight);
        if (updatedWeight[0])
            ((SettingsFragment)children[2]).updateWeightFields(output);
        return completed;
    }
}
