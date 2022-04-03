package com.example.healthAppAndroid.core;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.healthAppAndroid.R;

public final class SettingsFragment extends Fragment {
    private SegmentedControl picker;
    private TextValidator validator;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] ids = {
          R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth, R.id.inputWeight
        };
        picker = view.findViewById(R.id.planPicker);
        picker.setSelectedIndex((byte) (AppUserData.shared.currentPlan + 1));

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageSave))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(com.google.android.material.R.string.mtrl_picker_save), (dialog, i) -> {
                  short[] results = validator.getResults();
                  AppCoordinator.shared.updateUserInfo((byte) (picker.selectedIndex - 1), results);
              });
            builder.create().show();
        });

        view.findViewById(R.id.deleteButton).setOnClickListener(view2 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageDelete))
              .setNegativeButton(getString(R.string.cancel), null)
              .setNeutralButton(getString(androidx.appcompat.R.string.abc_menu_delete_shortcut_label),
                                (dialog, i) -> AppCoordinator.shared.deleteAppData());
            builder.create().show();
        });

        validator = new TextValidator(saveButton);
        for (int i = 0; i < 4; ++i) {
            validator.addChild((short) 0, (short) 999, view.findViewById(ids[i]));
        }
        validator.addChild((short) 1, (short) 999, view.findViewById(ids[4]));
        short weight = AppUserData.shared.weight;
        validator.children[4].result = weight;
        validator.children[4].valid = true;
        validator.children[4].field.setError(null);
        if (weight > 0)
            validator.children[4].textField.setText(String.valueOf(weight));
        updateWeightFields(AppUserData.shared.liftArray);
    }

    public void updateWeightFields(short[] lifts) {
        for (int i = 0; i < 4; ++i) {
            short value = lifts[i];
            validator.children[i].result = value;
            validator.children[i].valid = true;
            validator.children[i].field.setError(null);
            validator.children[i].textField.setText(String.valueOf(value));
        }
        if (validator.children[4].valid)
            validator.enableButton();
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            for (int i = 0; i < 5; ++i) {
                validator.children[i].textField.clearFocus();
            }
        }
    }
}
