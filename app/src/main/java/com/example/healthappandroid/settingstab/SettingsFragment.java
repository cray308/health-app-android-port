package com.example.healthappandroid.settingstab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.helpers.InputValidationDelegate;
import com.example.healthappandroid.common.helpers.InputValidator;
import com.example.healthappandroid.common.helpers.ViewHelper;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.common.shareddata.AppUserData;

public class SettingsFragment extends Fragment implements InputValidationDelegate {
    public SettingsTabCoordinator delegate;
    Button saveButton;
    private RadioGroup picker;
    private final InputValidator[] validators = {null, null, null, null};

    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.settingsSaveButton) {
                int segmentId = picker.getCheckedRadioButtonId();
                byte plan = -1;
                if (segmentId == R.id.baseBuildingSegment) {
                    plan = 0;
                } else if (segmentId == R.id.continuationSegment) {
                    plan = 1;
                }
                short[] results = {validators[0].result, validators[1].result,
                        validators[2].result, validators[3].result};
                delegate.handleSaveTap(results, plan);
            } else {
                delegate.handleDeleteTap();
            }
        }
    };

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int[] ids = {R.id.settingsMaxSquats, R.id.settingsMaxPullup,
                R.id.settingsMaxBench, R.id.settingsMaxDeadlift};

        saveButton = view.findViewById(R.id.settingsSaveButton);
        picker = view.findViewById(R.id.settingsPlanPicker);
        Button deleteButton = view.findViewById(R.id.settingsDeleteButton);
        saveButton.setOnClickListener(listener);
        deleteButton.setOnClickListener(listener);
        for (int i = 0; i < 4; ++i) {
            validators[i] = new InputValidator((short) 999,
                    view.findViewById(ids[i]), this);
        }
        updateWeightFields();
        byte plan = AppUserData.shared.currentPlan;
        int checked = R.id.noPlanSegment;
        if (plan == 0) {
            checked = R.id.baseBuildingSegment;
        } else if (plan == 1) {
            checked = R.id.continuationSegment;
        }
        picker.check(checked);
    }

    public void updateWeightFields() {
        for (int i = 0; i < 4; ++i) {
            short res = AppUserData.shared.liftMaxes[i];
            validators[i].result = res;
            validators[i].valid = true;
            String text = ViewHelper.format("%d", res);
            validators[i].view.field.setError(null);
            validators[i].view.textField.setText(text);
        }
        saveButton.setEnabled(true);
        saveButton.setTextColor(AppColors.blue);
    }

    public void disableButton() {
        saveButton.setEnabled(false);
        saveButton.setTextColor(AppColors.labelDisabled);
    }

    public void checkFields() {
        for (int i = 0; i < 4; ++i) {
            if (!validators[i].valid) return;
        }
        saveButton.setEnabled(true);
        saveButton.setTextColor(AppColors.blue);
    }
}