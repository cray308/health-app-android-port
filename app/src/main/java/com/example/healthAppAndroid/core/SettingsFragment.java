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

import java.util.Locale;

public final class SettingsFragment extends Fragment {
    private SegmentedControl picker;
    private TextValidator validator;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] ids = {R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth};
        picker = view.findViewById(R.id.planPicker);
        picker.setSelectedIndex((byte) (AppUserData.shared.currentPlan + 1));

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            String neutral = getString(com.google.android.material.R.string.mtrl_picker_save);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageSave))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(neutral, (dialogInterface, i)
                -> AppCoordinator.shared.updateUserInfo((byte) (picker.selectedIndex - 1),
                                                        validator.getResults()));
            builder.create().show();
        });

        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(view2 -> {
            String neutral = getString(
              androidx.appcompat.R.string.abc_menu_delete_shortcut_label);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
              .setTitle(getString(R.string.settingsAlertTitle))
              .setMessage(getString(R.string.settingsAlertMessageDelete))
              .setNegativeButton(getString(R.string.cancel), null)
              .setNeutralButton(neutral, (dialogInterface, i)
                -> AppCoordinator.shared.deleteAppData());
            builder.create().show();
        });

        validator = new TextValidator(saveButton);
        for (int i = 0; i < 4; ++i)
            validator.addChild((short) 999, view.findViewById(ids[i]));
        updateWeightFields();
    }

    public void updateWeightFields() {
        for (int i = 0; i < 4; ++i) {
            short value = AppUserData.shared.liftArray[i];
            validator.children[i].result = value;
            validator.children[i].valid = true;
            validator.children[i].field.setError(null);
            validator.children[i].textField.setText(String.format(Locale.US, "%d", value));
        }
        validator.enableButton();
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            for (int i = 0; i < 4; ++i) {
                validator.children[i].textField.clearFocus();
            }
        }
    }
}
