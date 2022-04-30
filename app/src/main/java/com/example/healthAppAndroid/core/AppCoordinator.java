package com.example.healthAppAndroid.core;

import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
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
    private Fragment active;
    public final float toSavedMass;
    public final boolean onEmulator;
    public final boolean metric;

    public static AppCoordinator shared;

    private static boolean isOnEmulator() {
        String fp = Build.FINGERPRINT, hw = Build.HARDWARE, pr = Build.PRODUCT, m = Build.MODEL;
        String gen = "generic", gSDK = "google_sdk";
        return (Build.BRAND.startsWith(gen) && Build.DEVICE.startsWith(gen)) || fp.startsWith(gen)
               || fp.startsWith("unknown") || hw.contains("goldfish") || hw.contains("ranchu")
               || m.contains(gSDK) || m.contains("Emulator") || m.contains("Android SDK built for x86")
               || Build.MANUFACTURER.contains("Genymotion") || pr.contains("sdk_google") || pr.contains(gSDK)
               || pr.contains("sdk") || pr.contains("sdk_x86") || pr.contains("sdk_gphone64_arm64")
               || pr.contains("vbox86p") || pr.contains("emulator") || pr.contains("simulator");
    }

    AppCoordinator(FragmentActivity activity, boolean isMetric) {
        onEmulator = isOnEmulator();
        fm = activity.getSupportFragmentManager();
        children[1] = new HistoryFragment();
        metric = isMetric;
        toSavedMass = isMetric ? 2.204623f : 1;
        init(activity, 2);
    }

    AppCoordinator(FragmentActivity activity, Object[] results, boolean isMetric) {
        onEmulator = isOnEmulator();
        fm = activity.getSupportFragmentManager();
        children[1] = new HistoryFragment(results);
        metric = isMetric;
        toSavedMass = isMetric ? 2.204623f : 1;
        init(activity, 0);
    }

    private void init(FragmentActivity activity, int activeIndex) {
        ((NavigationBarView)activity.findViewById(R.id.bottom_nav)).setOnItemSelectedListener(item -> {
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

        for (int i = 0; i < 3; ++i) {
            FragmentTransaction t = fm.beginTransaction().add(
              R.id.container, children[i], String.valueOf(i + 1));
            if (i != activeIndex) t.hide(children[i]);
            t.commit();
        }
        active = children[activeIndex];
    }

    void updateUserInfo(byte plan, byte darkMode, short[] newArr) {
        int rv = AppUserData.shared.updateSettings(plan, darkMode, newArr);
        if ((rv & 2) != 0) {
            int mode = AppUserData.shared.darkMode == 0
                       ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(mode);
            return;
        }
        if ((rv & 1) != 0)
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
