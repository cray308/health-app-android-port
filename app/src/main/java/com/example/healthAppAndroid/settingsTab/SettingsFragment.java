package com.example.healthAppAndroid.settingsTab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.TextValidator;
import com.example.healthAppAndroid.common.shareddata.AppUserData;
import com.example.healthAppAndroid.common.views.SegmentedControl;

public class SettingsFragment extends Fragment {
    public SettingsTabCoordinator delegate;
    private SegmentedControl picker;
    private TextValidator validator;

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] ids = {R.id.inputFirst, R.id.inputSecond, R.id.inputThird, R.id.inputFourth};
        picker = view.findViewById(R.id.planPicker);
        picker.setSelectedIndex((byte) (AppUserData.shared.currentPlan + 1));

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> delegate.handleSaveTap(
          validator.getResults(), (byte) (picker.selectedIndex - 1)));

        Button deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(view2 -> delegate.handleDeleteTap());

        validator = new TextValidator(saveButton);
        for (int i = 0; i < 4; ++i)
            validator.addChild((short) 999, view.findViewById(ids[i]));
        updateWeightFields();
    }

    public void updateWeightFields() {
        validator.reset(AppUserData.shared.liftArray);
        validator.enableButton();
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) validator.clearFocus();
    }
}
