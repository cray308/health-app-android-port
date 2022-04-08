package com.example.healthAppAndroid.core;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.healthAppAndroid.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

public final class SettingsFragment extends Fragment {
    private TextValidator validator;
    private SwitchCompat switchView;
    private final int[] segmentIds = {R.id.buttonLeft, R.id.buttonMid, R.id.buttonRight};
    private int selected;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, null);

        String[] exNames = getResources().getStringArray(R.array.exNames);
        int[] ids = {R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth};
        int[] keys = {38, 33, 3, 12};
        MaterialButtonToggleGroup picker = view.findViewById(R.id.planPicker);
        selected = AppUserData.shared.currentPlan + 1;
        picker.check(segmentIds[selected]);
        picker.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == segmentIds[0]) {
                selected = 0;
            } else if (checkedId == segmentIds[1]) {
                selected = 1;
            } else {
                selected = 2;
            }
        });

        byte darkMode = AppUserData.shared.darkMode;
        if (darkMode >= 0) {
            view.findViewById(R.id.switchContainer).setVisibility(View.VISIBLE);
            switchView = view.findViewById(R.id.switchView);
            switchView.setChecked(darkMode == 1);
        }

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageSave))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(com.google.android.material.R.string.mtrl_picker_save), (dialog, i) -> {
                  short[] results = validator.getResults();
                  byte dm = -1;
                  if (switchView != null)
                      dm = (byte)(switchView.isChecked() ? 1 : 0);
                  AppCoordinator.shared.updateUserInfo((byte)(selected - 1), dm, results);
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
            TextValidator.InputView v = view.findViewById(ids[i]);
            v.field.setHint(getString(R.string.maxWeightFormat, exNames[keys[i]]));
            validator.addChild((short)0, (short)999, v);
        }
        validator.addChild((short)1, (short)999, view.findViewById(R.id.inputWeight));
        short weight = AppUserData.shared.weight;
        validator.children[4].result = weight;
        validator.children[4].valid = true;
        validator.children[4].field.setError(null);
        validator.children[4].field.setHint(getString(R.string.bodyWeightHint));
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
