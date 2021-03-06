package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.MainActivity;
import com.example.healthAppAndroid.core.TextValidator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public final class UpdateMaxesDialog extends BottomSheetDialogFragment {
    private static final String key = "UpdateMaxesKey";
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
        if (args != null) index = args.getInt(key, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.update_maxes_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NumberPicker picker = view.findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(9);
        String[] values = new String[10];
        Locale l = Locale.getDefault();
        for (int i = 0; i < 10; ++i) {
            values[i] = String.format(l, "%d", i + 1);
        }
        picker.setDisplayedValues(values);
        picker.setValue(0);
        picker.setOnValueChangedListener((picker1, old, newVal) -> value = (byte)(newVal + 1));

        Button finishButton = view.findViewById(R.id.submitBtn);
        finishButton.setOnClickListener(view1 -> {
            int extra = index == LiftType.pullUp ? MainActivity.getUserData().weightToUse() : 0;
            float initWeight = (validator.children[0].result * MainActivity.toSavedMass + extra) * 36;
            float reps = 37f - value;
            short weight = (short)(Math.round(initWeight / reps) - extra);
            WorkoutActivity activity = (WorkoutActivity)getActivity();
            if (activity != null) activity.finishedBottomSheet(this, index, weight);
        });
        validator = new TextValidator(finishButton);

        TextValidator.InputView input = view.findViewById(R.id.input);
        String name = getResources().getStringArray(R.array.exNames)[index].toLowerCase(l);
        input.field.setHint(getString(R.string.maxWeightFormat, name));
        validator.addChild(0, 999, R.plurals.inputFieldError, MainActivity.metric ? 8192 : 0, input);
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
