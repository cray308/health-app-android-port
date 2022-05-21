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

import java.util.Locale;

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

        MainActivity.UserData data = MainActivity.getUserData();
        MaterialButtonToggleGroup picker = view.findViewById(R.id.planPicker);
        selected = data.currentPlan + 1;
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

        if (data.darkMode >= 0) {
            view.findViewById(R.id.switchContainer).setVisibility(View.VISIBLE);
            switchView = view.findViewById(R.id.switchView);
            switchView.setChecked(data.darkMode == 1);
        }

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            MainActivity a = (MainActivity)getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(a)
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageSave))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(com.google.android.material.R.string.mtrl_picker_save), (d, x) -> {
                  short[] results = {0, 0, 0, 0, 0};
                  for (int i = 0; i < 5; ++i)
                      results[i] = (short)Math.round(validator.children[i].result * MainActivity.toSavedMass);
                  byte dm = -1;
                  if (switchView != null) dm = (byte)(switchView.isChecked() ? 1 : 0);
                  if (a != null) a.updateUserInfo((byte)(selected - 1), dm, results);
              });
            builder.create().show();
        });

        view.findViewById(R.id.deleteButton).setOnClickListener(view2 -> {
            MainActivity a = (MainActivity)getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(a)
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageDelete))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(androidx.appcompat.R.string.abc_menu_delete_shortcut_label), (d, x) -> {
                  if (a != null) a.deleteAppData();
              });
            builder.create().show();
        });

        validator = new TextValidator(saveButton);
        String[] exNames = getResources().getStringArray(R.array.exNames);
        int[] ids = {R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth};
        int kb = MainActivity.metric ? 8192 : 0;
        Locale l = Locale.getDefault();
        for (int i = 0; i < 4; ++i) {
            TextValidator.InputView v = view.findViewById(ids[i]);
            v.field.setHint(getString(R.string.maxWeightFormat, exNames[i].toLowerCase(l)));
            validator.addChild(0, 999, R.plurals.inputFieldError, kb, v);
        }
        validator.addChild(1, 999, R.plurals.inputFieldErrorEmpty,
                           kb, view.findViewById(R.id.inputWeight));
        validator.children[4].valid = true;
        validator.children[4].field.setError(null);
        validator.children[4].field.setHint(getString(R.string.bodyWeightHint));
        if (data.weight > 0) {
            if (MainActivity.metric) {
                float fWeight = data.weight * 0.453592f;
                validator.children[4].textField.setText(String.format(Locale.US, "%.2f", fWeight));
                validator.children[4].result = fWeight;
            } else {
                validator.children[4].textField.setText(String.format(Locale.US, "%d", data.weight));
                validator.children[4].result = data.weight;
            }
        }
        updateWeightFields(data.liftArray);
    }

    void updateWeightFields(short[] lifts) {
        if (MainActivity.metric) {
            for (int i = 0; i < 4; ++i) {
                float value = lifts[i] * 0.453592f;
                validator.children[i].textField.setText(String.format(Locale.US, "%.2f", value));
                validator.children[i].valid = true;
                validator.children[i].field.setError(null);
                validator.children[i].result = value;
            }
        } else {
            for (int i = 0; i < 4; ++i) {
                validator.children[i].textField.setText(String.format(Locale.US, "%d", lifts[i]));
                validator.children[i].valid = true;
                validator.children[i].field.setError(null);
                validator.children[i].result = lifts[i];
            }
        }
        if (validator.children[4].valid) validator.enableButton();
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
