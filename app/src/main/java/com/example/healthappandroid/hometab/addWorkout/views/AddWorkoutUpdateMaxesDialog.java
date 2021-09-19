package com.example.healthappandroid.hometab.addWorkout.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.helpers.InputValidationDelegate;
import com.example.healthappandroid.common.helpers.InputValidator;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.common.workouts.Workout;
import com.example.healthappandroid.hometab.addWorkout.WorkoutCoordinator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddWorkoutUpdateMaxesDialog extends BottomSheetDialogFragment implements InputValidationDelegate {
    private final InputValidator[] validators = {null, null, null, null};
    private Button finishButton;

    public WorkoutCoordinator delegate;
    public Workout workout;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_workout_update_maxes_modal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] ids = {R.id.updateMaxSquat, R.id.updateMaxPullUp,
            R.id.updateMaxBench, R.id.updateMaxDeadlift};
        finishButton = view.findViewById(R.id.updateMaxSubmitBtn);
        finishButton.setOnClickListener(finishListener);
        for (int i = 0; i < 4; ++i)
            validators[i] = new InputValidator((short) 999, view.findViewById(ids[i]), this);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private final View.OnClickListener finishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            workout.newLifts = new short[4];
            for (int i = 0; i < 4; ++i)
                workout.newLifts[i] = validators[i].result;
            delegate.completedWorkout(getActivity(), AddWorkoutUpdateMaxesDialog.this, false);
        }
    };

    public void disableButton() {
        finishButton.setEnabled(false);
        finishButton.setTextColor(AppColors.labelDisabled);
    }

    public void checkFields() {
        for (int i = 0; i < 4; ++i) {
            if (!validators[i].valid) return;
        }
        finishButton.setEnabled(true);
        finishButton.setTextColor(AppColors.blue);
    }
}
