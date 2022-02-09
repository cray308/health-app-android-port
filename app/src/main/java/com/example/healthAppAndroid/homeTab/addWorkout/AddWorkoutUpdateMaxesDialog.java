package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
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

    static final class Params implements Parcelable {
        private final int index;

        private Params(int index) { this.index = index; }

        private Params(Parcel src) { index = src.readInt(); }

        public int describeContents() { return 0; }

        public void writeToParcel(Parcel parcel, int i) { parcel.writeInt(index); }

        public static final Creator<Params> CREATOR = new Creator<Params>() {
            public Params createFromParcel(Parcel parcel) { return new Params(parcel); }

            public Params[] newArray(int i) { return new Params[i]; }
        };
    }

    private TextValidator validator;
    private int index;
    private int value = 1;
    private TextView stepperLabel;

    static AddWorkoutUpdateMaxesDialog newInstance(int index) {
        AddWorkoutUpdateMaxesDialog dialog = new AddWorkoutUpdateMaxesDialog();
        Bundle args = new Bundle();
        args.putParcelable(paramsKey, new Params(index));
        dialog.setArguments(args);
        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            Params params = args.getParcelable(paramsKey);
            index = params.index;
        }
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_workout_update_maxes_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] keys = {
          R.string.maxWeightSquat, R.string.maxWeightPullUp,
          R.string.maxWeightBench, R.string.maxWeightDeadLift
        };
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
            short extra = (short) (index == LiftType.pullUp
                                   ? ExerciseManager.getBodyWeightToUse() : 0);
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
        validator.addChild((short) 999, input);
        updateStepperLabel();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void updateStepperLabel() {
        stepperLabel.setText(getString(R.string.stepperLabelFormat, value));
    }
}
