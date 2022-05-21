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
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;

import java.util.Locale;

public final class SettingsFragment extends Fragment {
    private TextValidator validator;
    private SwitchCompat darkModeSwitch;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, null);

        UserData data = MainActivity.userData;
        SegmentedControl planControl = view.findViewById(R.id.planControl);
        planControl.setSelectedIndex(data.plan + 1);

        if (data.darkMode >= 0) {
            LinearLayout switchContainer = view.findViewById(R.id.switchContainer);
            switchContainer.setVisibility(View.VISIBLE);
            darkModeSwitch = (SwitchCompat)switchContainer.getChildAt(2);
            darkModeSwitch.setChecked(data.darkMode == 1);
        }

        boolean metric = Macros.isMetric(Locale.getDefault());
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            MainActivity activity = (MainActivity)getActivity();
            new AlertDialog.Builder(activity)
              .setTitle(getString(R.string.settingsAlert))
              .setMessage(getString(R.string.settingsMsgSave))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(R.string.save), (d, x) -> {
                  float toSavedMass = Macros.savedMassFactor(metric);
                  int[] results = {0, 0, 0, 0, 0};
                  for (int i = 0; i < 5; ++i) {
                      results[i] = Math.round(validator.children[i].result * toSavedMass);
                  }
                  byte darkMode = -1;
                  if (darkModeSwitch != null) darkMode = (byte)(darkModeSwitch.isChecked() ? 1 : 0);
                  if (activity != null)
                      activity.updateUserInfo((byte)(planControl.selectedIndex - 1), darkMode, results);
              }).create().show();
        });

        view.findViewById(R.id.deleteButton).setOnClickListener(view2 -> {
            MainActivity activity = (MainActivity)getActivity();
            new AlertDialog.Builder(activity)
              .setTitle(getString(R.string.settingsAlert))
              .setMessage(getString(R.string.settingsMsgDelete))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(androidx.appcompat.R.string.abc_menu_delete_shortcut_label), (d, x) -> {
                  if (activity != null) activity.deleteAppData();
              }).create().show();
        });

        validator = new TextValidator(saveButton);
        LinearLayout stack = view.findViewById(R.id.fieldStack);
        int inputType = TextValidator.inputForLocale(metric);
        for (int i = 0; i < 4; ++i) {
            validator.addChild((TextValidator.InputView)stack.getChildAt(i + 1), inputType);
        }
        validator.addChild((TextValidator.InputView)stack.getChildAt(0), inputType);

        if (data.weight > 0) {
            if (metric) {
                float weight = data.weight * Macros.toKg;
                validator.children[4].field.setText(String.format(Locale.US, "%.2f", weight));
                validator.children[4].result = weight;
            } else {
                validator.children[4].field.setText(String.format(Locale.US, "%d", data.weight));
                validator.children[4].result = data.weight;
            }
        }

        updateFields(data.lifts);
    }

    void updateFields(int[] lifts) {
        if (Macros.isMetric(Locale.getDefault())) {
            for (int i = 0; i < 4; ++i) {
                float value = lifts[i] * Macros.toKg;
                validator.children[i].field.setText(String.format(Locale.US, "%.2f", value));
                validator.children[i].reset(value);
            }
        } else {
            for (int i = 0; i < 4; ++i) {
                validator.children[i].field.setText(String.format(Locale.US, "%d", lifts[i]));
                validator.children[i].reset(lifts[i]);
            }
        }
        if (validator.children[4].valid) validator.button.setEnabled(true);
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            for (TextValidator.InputView v : validator.children) { v.field.clearFocus(); }
        }
    }
}
