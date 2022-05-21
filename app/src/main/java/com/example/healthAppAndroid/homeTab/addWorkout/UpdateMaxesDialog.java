package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.Macros;
import com.example.healthAppAndroid.core.TextValidator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public final class UpdateMaxesDialog extends BottomSheetDialogFragment {
    private static final String IndexKey = "UpdateMaxesDialog.Index";
    private static final String WeightKey = "UpdateMaxesDialog.Weight";

    static UpdateMaxesDialog init(int index, int bodyWeight) {
        UpdateMaxesDialog dialog = new UpdateMaxesDialog();
        Bundle args = new Bundle();
        args.putInt(IndexKey, index);
        args.putInt(WeightKey, bodyWeight);
        dialog.setArguments(args);
        return dialog;
    }

    private int index;
    private int bodyWeight;
    private int value = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            index = args.getInt(IndexKey, 0);
            bodyWeight = args.getInt(WeightKey, 0);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.update_maxes_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Locale locale = Locale.getDefault();
        boolean metric = Macros.isMetric(locale);
        NumberPicker picker = view.findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(9);
        String[] values = new String[10];
        for (int i = 0; i < 10; ++i) {
            values[i] = String.format(locale, "%d", i + 1);
        }
        picker.setDisplayedValues(values);
        picker.setValue(0);
        picker.setOnValueChangedListener((picker1, old, newVal) -> value = newVal + 1);

        Button finishButton = view.findViewById(R.id.submitButton);
        finishButton.setText(androidx.appcompat.R.string.abc_action_mode_done);
        TextValidator validator = new TextValidator(finishButton);
        finishButton.setOnClickListener(view1 -> {
            int extra = index == LiftType.pullUp ? bodyWeight : 0;
            float initWeight = ((validator.children[0].result * Macros.savedMassFactor(metric))
                                + extra) * 36;
            int weight = Math.round(initWeight / (37f - value)) - extra;
            WorkoutActivity activity = (WorkoutActivity)getActivity();
            if (activity != null) activity.finishedBottomSheet(this, index, weight);
        });

        TextValidator.InputView iv = view.findViewById(R.id.input);
        String name = getResources().getStringArray(R.array.exNames)[index].toLowerCase(locale);
        iv.layout.setHint(getString(R.string.maxWeight, name));
        validator.addChild(iv, TextValidator.inputForLocale(metric));
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
