package com.example.healthAppAndroid.common.shareddata;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryTabCoordinator;
import com.example.healthAppAndroid.historyTab.view.HistoryFragment;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;
import com.example.healthAppAndroid.homeTab.view.HomeFragment;
import com.example.healthAppAndroid.settingsTab.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class AppCoordinator {
    private final FragmentManager fm;
    private final Fragment[] children = {
        new HomeFragment(), new HistoryFragment(), new SettingsFragment()
    };
    public final HomeTabCoordinator homeCoordinator;
    final HistoryTabCoordinator historyCoordinator;
    private Fragment active;

    public static AppCoordinator shared;

    public static void create(FragmentActivity activity) {
        shared = new AppCoordinator(activity);
        shared.setupTabs(activity.findViewById(R.id.bottom_nav));
    }

    private AppCoordinator(FragmentActivity activity) {
        fm = activity.getSupportFragmentManager();
        homeCoordinator = new HomeTabCoordinator(children[0]);
        historyCoordinator = new HistoryTabCoordinator(children[1]);
    }

    private void setupTabs(BottomNavigationView tabBar) {
        tabBar.setOnItemSelectedListener(item -> {
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

        for (int i = children.length - 1; i > 0; --i) {
            String tag = Integer.toString(i + 1);
            fm.beginTransaction().add(R.id.container, children[i], tag).hide(children[i]).commit();
        }
        fm.beginTransaction().add(R.id.container, children[0], "1").commit();
        active = children[0];
    }

    public void updateUserInfo(byte plan, short[] lifts) {
        boolean updateHome = plan != AppUserData.shared.currentPlan;
        AppUserData.shared.updateSettings(plan, lifts);
        if (updateHome)
            homeCoordinator.fragment.createWorkoutsList();
    }

    public void deleteAppData() {
        boolean updateHome = AppUserData.shared.completedWorkouts != 0;
        AppUserData.shared.deleteSavedData();
        PersistenceService.deleteAppData();
        if (updateHome)
            homeCoordinator.fragment.updateWorkoutsList();
        historyCoordinator.handleDataDeletion();
    }

    public void updateMaxWeights(short[] lifts) {
        boolean updateSettings = AppUserData.shared.updateWeightMaxes(lifts);
        if (updateSettings) {
            SettingsFragment frag = (SettingsFragment) children[2];
            frag.updateWeightFields();
        }
    }
}
