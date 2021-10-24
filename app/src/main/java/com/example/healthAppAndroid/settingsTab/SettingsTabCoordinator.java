package com.example.healthAppAndroid.settingsTab;

import android.app.AlertDialog;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppCoordinator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.shareddata.PersistenceService;

public class SettingsTabCoordinator {
    private final SettingsFragment fragment;

    public SettingsTabCoordinator(Fragment fragment) {
        this.fragment = (SettingsFragment) fragment;
        this.fragment.delegate = this;
    }

    public void handleSaveTap(short[] newLifts, byte plan) {
        String neutral = fragment.getString(com.google.android.material.R.string.mtrl_picker_save);
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity())
            .setTitle(fragment.getString(R.string.settingsAlertTitle))
            .setMessage(fragment.getString(R.string.settingsAlertMessageSave))
            .setNegativeButton(fragment.getString(R.string.cancel), null)
            .setPositiveButton(neutral, (dialogInterface, i) -> {
                AppUserData.shared.updateWeightMaxes(newLifts);
                AppUserData.shared.setWorkoutPlan(plan);
                AppCoordinator.shared.updatedUserInfo();
            });
        builder.create().show();
    }

    public void handleDeleteTap() {
        String neutral = fragment.getString(
          androidx.appcompat.R.string.abc_menu_delete_shortcut_label);
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity())
            .setTitle(fragment.getString(R.string.settingsAlertTitle))
            .setMessage(fragment.getString(R.string.settingsAlertMessageDelete))
            .setNegativeButton(fragment.getString(R.string.cancel), null)
            .setNeutralButton(neutral, (dialogInterface, i) -> {
                AppUserData.shared.deleteSavedData();
                AppCoordinator.shared.deletedAppData();
                PersistenceService.deleteAppData();
            });
        builder.create().show();
    }

    public void updateWeightText() { fragment.updateWeightFields(); }
}
