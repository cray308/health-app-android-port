package com.example.healthAppAndroid.settingsTab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.BaseTextValidator;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.workouts.LiftType;

public class SettingsFragment extends Fragment {
    public SettingsTabCoordinator delegate;
    private RadioGroup picker;
    private BaseTextValidator validator;

    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.saveButton) {
                int segmentId = picker.getCheckedRadioButtonId();
                byte plan = -1;
                if (segmentId == R.id.segmentBaseBuilding) {
                    plan = 0;
                } else if (segmentId == R.id.segmentContinuation) {
                    plan = 1;
                }
                delegate.handleSaveTap(validator.getLiftData(), plan);
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
        int[] ids = {R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth};

        Button saveButton = view.findViewById(R.id.saveButton);
        picker = view.findViewById(R.id.planPicker);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        saveButton.setOnClickListener(listener);
        deleteButton.setOnClickListener(listener);
        validator = new BaseTextValidator(saveButton, AppColors.blue);
        for (int i = 0; i < 4; ++i)
            validator.addChild(i, (short) 0, (short) 999, view.findViewById(ids[i]));
        updateWeightFields();
        byte plan = AppUserData.shared.currentPlan;
        int checked = R.id.segmentNoPlan;
        if (plan == 0) {
            checked = R.id.segmentBaseBuilding;
        } else if (plan == 1) {
            checked = R.id.segmentContinuation;
        }
        picker.check(checked);
    }

    public void updateWeightFields() {
        validator.reset(LiftType.squat, AppUserData.shared.liftData.squat);
        validator.reset(LiftType.pullUp, AppUserData.shared.liftData.pullUp);
        validator.reset(LiftType.bench, AppUserData.shared.liftData.bench);
        validator.reset(LiftType.deadlift, AppUserData.shared.liftData.deadlift);
        validator.enableButton();
    }
}
