package com.example.healthAppAndroid.core;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryFragment;
import com.example.healthAppAndroid.homeTab.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class AppCoordinator {
    private final FragmentManager fm;
    private final Fragment[] children = {
        new HomeFragment(), null, new SettingsFragment()
    };
    private Fragment active;

    public static AppCoordinator shared;

    AppCoordinator(FragmentActivity activity, Object[] results) {
        fm = activity.getSupportFragmentManager();
        children[1] = HistoryFragment.init(results);

        ((BottomNavigationView)activity.findViewById(R.id.bottom_nav)).setOnItemSelectedListener(
          item -> {
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

    void updateUserInfo(byte plan, short[] lifts, short weight) {
        if (AppUserData.shared.updateSettings(plan, lifts, weight))
            ((HomeFragment) children[0]).createWorkoutsList(plan);
    }

    void deleteAppData() {
        boolean updateHome = AppUserData.shared.completedWorkouts != 0;
        AppUserData.shared.deleteSavedData();
        PersistenceService.deleteAppData();
        if (updateHome)
            ((HomeFragment) children[0]).updateWorkoutsList((byte) 0);
        ((HistoryFragment) children[1]).handleDataDeletion();
    }

    public void updateMaxWeights(short[] lifts) {
        short[] output = {0, 0, 0, 0};
        if (AppUserData.shared.updateWeightMaxes(lifts, output))
            ((SettingsFragment) children[2]).updateWeightFields(output);
    }
}
