package com.example.healthAppAndroid.settingsTab;

import android.app.AlertDialog;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;

public class SettingsTabCoordinator {
    private static class DeleteHandler implements PersistenceService.Block {
        public void completion() {
            AppUserData.shared.deleteSavedData();
            AppCoordinator.shared.deletedAppData();
        }
    }

    private final SettingsFragment fragment;

    public SettingsTabCoordinator(Fragment fragment) {
        this.fragment = (SettingsFragment) fragment;
        this.fragment.delegate = this;
    }

    public void handleSaveTap(short[] newLifts, byte plan) {
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
            .setNeutralButton(fragment.getString(R.string.delete), (dialogInterface, i) ->
                PersistenceService.deleteAppData(new DeleteHandler()));
        builder.create().show();
    }

    public void updateWeightText() {
        fragment.updateWeightFields();
    }
}
