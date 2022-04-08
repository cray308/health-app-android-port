package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.TextValidator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class UpdateMaxesDialog extends BottomSheetDialogFragment {
    private static final String key = "UpdateMaxesDialog.Index";
    private TextValidator validator;
    private int index;
    private byte value = 1;

    static UpdateMaxesDialog init(int index) {
        UpdateMaxesDialog dialog = new UpdateMaxesDialog();
        Bundle args = new Bundle();
        args.putInt(key, index);
        dialog.setArguments(args);
        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            index = args.getInt(key, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.update_maxes_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NumberPicker picker = view.findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(9);
        picker.setDisplayedValues(getResources().getStringArray(R.array.pickerValues));
        picker.setValue(0);
        picker.setOnValueChangedListener((picker1, old, newVal) -> value = (byte)(newVal + 1));

        Button finishButton = view.findViewById(R.id.submitBtn);
        finishButton.setOnClickListener(view1 -> {
            int extra = index == LiftType.pullUp ? ExerciseManager.getBodyWeightToUse() : 0;
            int initWeight = (validator.getResults()[0] + extra) * 36;
            float reps = 37f - value;
            short weight = (short)(((initWeight / reps) + 0.5f) - extra);
            WorkoutActivity activity = (WorkoutActivity)getActivity();
            if (activity != null)
                activity.finishedBottomSheet(this, index, weight);
        });
        validator = new TextValidator(finishButton);

        int[] keys = {38, 33, 3, 12};
        TextValidator.InputView input = view.findViewById(R.id.input);
        input.field.setHint(getString(R.string.maxWeightFormat,
                                      getResources().getStringArray(R.array.exNames)[keys[index]]));
        validator.addChild((short)0, (short)999, input);
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
