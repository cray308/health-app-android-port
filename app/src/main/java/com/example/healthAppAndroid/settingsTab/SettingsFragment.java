package com.example.healthAppAndroid.settingsTab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.TextValidator;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.shareddata.AppUserData;

public class SettingsFragment extends Fragment {
    public SettingsTabCoordinator delegate;
    private RadioGroup picker;
    private TextValidator validator;

    public SettingsFragment() {}

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] ids = {R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth};
        picker = view.findViewById(R.id.planPicker);

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {
            int segmentId = picker.getCheckedRadioButtonId();
            byte plan = AppUserData.Plans.noPlan;
            if (segmentId == R.id.segmentBaseBuilding) {
                plan = AppUserData.Plans.baseBuilding;
            } else if (segmentId == R.id.segmentContinuation) {
                plan = AppUserData.Plans.continuation;
            }
            delegate.handleSaveTap(validator.getResults(), plan);
        });

        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(view2 -> delegate.handleDeleteTap());

        validator = new TextValidator(saveButton, AppColors.blue);
        for (int i = 0; i < 4; ++i)
            validator.addChild((short) 0, (short) 999, view.findViewById(ids[i]));
        updateWeightFields();
        byte plan = AppUserData.shared.currentPlan;
        int checked = R.id.segmentNoPlan;
        if (plan == AppUserData.Plans.baseBuilding) {
            checked = R.id.segmentBaseBuilding;
        } else if (plan == AppUserData.Plans.continuation) {
            checked = R.id.segmentContinuation;
        }
        picker.check(checked);
    }

    public void updateWeightFields() {
        validator.reset(AppUserData.shared.liftArray);
        validator.enableButton();
    }
}
