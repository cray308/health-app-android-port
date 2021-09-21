package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.BaseTextValidator;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutCoordinator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddWorkoutUpdateMaxesDialog extends BottomSheetDialogFragment {
    private BaseTextValidator validator;
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

        int[] ids = {R.id.firstInput, R.id.secondInput, R.id.thirdInput, R.id.fourthInput};
        Button finishButton = view.findViewById(R.id.updateMaxSubmitBtn);
        finishButton.setOnClickListener(finishListener);
        validator = new BaseTextValidator(finishButton, AppColors.blue);
        for (int i = 0; i < 4; ++i)
            validator.addChild(i, (short) 1, (short) 999, view.findViewById(ids[i]));
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private final View.OnClickListener finishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            workout.newLifts = validator.getLiftData();
            delegate.completedWorkout(getActivity(), AddWorkoutUpdateMaxesDialog.this, false);
        }
    };
}
