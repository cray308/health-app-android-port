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

    static Object create(FragmentActivity activity) {
        Object[] blockArray = {null};
        shared = new AppCoordinator(activity, blockArray);
        shared.setupTabs(activity.findViewById(R.id.bottom_nav));
        return blockArray[0];
    }

    private AppCoordinator(FragmentActivity activity, Object[] blockArray) {
        fm = activity.getSupportFragmentManager();
        children[1] = HistoryFragment.init(blockArray);
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

    void updateUserInfo(byte plan, short[] lifts, short weight) {
        boolean updateHome = plan != AppUserData.shared.currentPlan;
        AppUserData.shared.updateSettings(plan, lifts, weight);
        if (updateHome)
            ((HomeFragment) children[0]).createWorkoutsList();
    }

    void deleteAppData() {
        boolean updateHome = AppUserData.shared.completedWorkouts != 0;
        AppUserData.shared.deleteSavedData();
        PersistenceService.deleteAppData();
        if (updateHome)
            ((HomeFragment) children[0]).updateWorkoutsList();
        ((HistoryFragment) children[1]).handleDataDeletion();
    }

    public void updateMaxWeights(short[] lifts) {
        if (AppUserData.shared.updateWeightMaxes(lifts))
            ((SettingsFragment) children[2]).updateWeightFields();
    }
}
