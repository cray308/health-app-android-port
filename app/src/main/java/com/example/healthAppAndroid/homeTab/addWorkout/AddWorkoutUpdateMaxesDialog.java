package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.TextValidator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class AddWorkoutUpdateMaxesDialog extends BottomSheetDialogFragment {
    private static final String paramsKey = "AddWorkoutUpdateMaxesDialogParams";

    private TextValidator validator;
    private TextView stepperLabel;
    private int index;
    private byte value = 1;

    static AddWorkoutUpdateMaxesDialog newInstance(int index) {
        AddWorkoutUpdateMaxesDialog dialog = new AddWorkoutUpdateMaxesDialog();
        Bundle args = new Bundle();
        args.putInt(paramsKey, index);
        dialog.setArguments(args);
        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            index = args.getInt(paramsKey, 0);
        }
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_workout_update_maxes_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] keys = {R.string.maxWeightSquat, R.string.maxWeightPullUp,
                      R.string.maxWeightBench, R.string.maxWeightDeadLift};
        stepperLabel = view.findViewById(R.id.stepperLabel);
        view.findViewById(R.id.minusButton).setOnClickListener(btn -> {
            if (value > 1)
                value -= 1;
            updateStepperLabel();
        });
        view.findViewById(R.id.plusButton).setOnClickListener(btn -> {
            if (value < 10)
                value += 1;
            updateStepperLabel();
        });

        Button finishButton = view.findViewById(R.id.updateMaxSubmitBtn);
        finishButton.setOnClickListener(view1 -> {
            int extra = index == LiftType.pullUp ? ExerciseManager.getBodyWeightToUse() : 0;
            int initWeight = (validator.getResults()[0] + extra) * 36;
            float reps = 37f - value;
            short weight = (short) (((initWeight / reps) + 0.5f) - extra);
            WorkoutActivity activity = (WorkoutActivity) getActivity();
            if (activity != null)
                activity.finishedBottomSheet(this, index, weight);
        });
        validator = new TextValidator(finishButton);

        TextValidator.InputView input = view.findViewById(R.id.input);
        input.field.setHint(getString(keys[index]));
        validator.addChild((short) 0, (short) 999, input);
        updateStepperLabel();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void updateStepperLabel() {
        stepperLabel.setText(getString(R.string.stepperLabelFormat, value));
    }
}
