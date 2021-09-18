package com.example.healthappandroid.common.shareddata;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.healthappandroid.R;
import com.example.healthappandroid.historytab.HistoryTabCoordinator;
import com.example.healthappandroid.hometab.HomeTabCoordinator;
import com.example.healthappandroid.settingstab.SettingsTabCoordinator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AppCoordinator {
    public final FragmentManager fm;
    private Fragment active = null;

    private final NavigationBarView.OnItemSelectedListener tabListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment toShow = homeCoordinator.fragment;
            String title = "Home";
            int id = item.getItemId();
            if (id == R.id.history) {
                title = "Workout History";
                toShow = historyCoordinator.fragment;
            } else if (id == R.id.settings) {
                title = "App Settings";
                toShow = settingsCoordinator.fragment;
            }

            fm.beginTransaction().hide(active).show(toShow).commit();
            active = toShow;
            updateTitle(title);
            return true;
        }
    };

    public final HomeTabCoordinator homeCoordinator;
    public final HistoryTabCoordinator historyCoordinator;
    public final SettingsTabCoordinator settingsCoordinator;

    public static AppCoordinator shared;

    public static void create(FragmentManager fm, BottomNavigationView tabBar) {
        shared = new AppCoordinator(fm);
        shared.setupTabs(tabBar);
    }

    private AppCoordinator(FragmentManager fm) {
        this.fm = fm;
        homeCoordinator = new HomeTabCoordinator();
        historyCoordinator = new HistoryTabCoordinator();
        settingsCoordinator = new SettingsTabCoordinator();
    }

    private void setupTabs(BottomNavigationView tabBar) {
        int id = R.id.main_container;
        tabBar.setOnItemSelectedListener(tabListener);
        fm.beginTransaction().add(id, settingsCoordinator.fragment, "3").hide(settingsCoordinator.fragment).commit();
        fm.beginTransaction().add(id, historyCoordinator.fragment, "2").hide(historyCoordinator.fragment).commit();
        fm.beginTransaction().add(id, homeCoordinator.fragment, "1").commit();
        active = homeCoordinator.fragment;
        settingsCoordinator.start();
    }

    private void updateTitle(String title) {
        FragmentActivity a = active.getActivity();
        if (a != null)
            a.setTitle(title);
    }

    public void updatedUserInfo() { homeCoordinator.resetUI(); }

    public void deletedAppData() {
        homeCoordinator.updateUI();
        historyCoordinator.fetchData();
    }

    public void updateMaxWeights() { settingsCoordinator.updateWeightText(); }
}
