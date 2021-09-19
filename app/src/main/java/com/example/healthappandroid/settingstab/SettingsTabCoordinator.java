package com.example.healthappandroid.settingstab;

import android.app.AlertDialog;

import com.example.healthappandroid.common.shareddata.AppCoordinator;
import com.example.healthappandroid.common.shareddata.AppUserData;

public class SettingsTabCoordinator {
    public final SettingsFragment fragment = new SettingsFragment();

    public void start() {
        fragment.delegate = this;
    }

    public void handleSaveTap(short[] weights, byte plan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle("Are you sure?").setMessage("This will save the currently entered data.");
        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            AppUserData.shared.updateWeightMaxes(weights);
            AppUserData.shared.setWorkoutPlan(plan);
            AppCoordinator.shared.updatedUserInfo();
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void handleDeleteTap() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setTitle("Are you sure?").setMessage("This will delete all workout history.");
        builder.setNeutralButton("Delete",
                (dialogInterface, i) -> new SettingsDeleteTask().execute());
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void updateWeightText() { fragment.updateWeightFields(); }
}
