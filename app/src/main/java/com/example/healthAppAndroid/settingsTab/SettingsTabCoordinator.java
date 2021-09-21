package com.example.healthAppAndroid.settingsTab;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;
import com.example.healthAppAndroid.common.shareddata.WeeklyData;
import com.example.healthAppAndroid.common.shareddata.WeeklyDataDao;
import com.example.healthAppAndroid.common.workouts.Workout;

public class SettingsTabCoordinator {
    private static class ClearDataThread extends Thread {
        private ClearDataThread() {}

        @Override
        public void run() {
            WeeklyDataDao dao = PersistenceService.shared.dao();
            WeeklyData[] data = dao.getDataInInterval(0, AppUserData.shared.weekStart);
            PersistenceService.shared.deleteEntries(dao, data);
            new Handler(Looper.getMainLooper()).post(() -> {
                AppUserData.shared.deleteSavedData();
                AppCoordinator.shared.deletedAppData();
            });
        }
    }

    private final SettingsFragment fragment;

    public SettingsTabCoordinator(Fragment fragment) {
        this.fragment = (SettingsFragment) fragment;
        this.fragment.delegate = this;
    }

    public void handleSaveTap(Workout.LiftData newLifts, byte plan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity())
            .setTitle(fragment.getString(R.string.settingsAlertTitle))
            .setMessage(fragment.getString(R.string.settingsAlertMessageSave))
            .setNegativeButton(fragment.getString(R.string.cancel), null)
            .setPositiveButton(fragment.getString(R.string.save), (dialogInterface, i) -> {
                AppUserData.shared.updateWeightMaxes(newLifts);
                AppUserData.shared.setWorkoutPlan(plan);
                AppCoordinator.shared.updatedUserInfo();
        });
        builder.create().show();
    }

    public void handleDeleteTap() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity())
            .setTitle(fragment.getString(R.string.settingsAlertTitle))
            .setMessage(fragment.getString(R.string.settingsAlertMessageDelete))
            .setNegativeButton(fragment.getString(R.string.cancel), null)
            .setNeutralButton(fragment.getString(R.string.delete),
                              (dialogInterface, i) -> new ClearDataThread().start());
        builder.create().show();
    }

    public void updateWeightText() {
        fragment.updateWeightFields();
    }
}
