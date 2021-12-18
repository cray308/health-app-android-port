package com.example.healthAppAndroid.homeTab.addWorkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.TextValidator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class AddWorkoutUpdateMaxesDialog extends BottomSheetDialogFragment {
    private TextValidator validator;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_workout_update_maxes_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button finishButton = view.findViewById(R.id.updateMaxSubmitBtn);
        finishButton.setOnClickListener(view1 -> {
            short[] lifts = validator.getResults();
            WorkoutActivity activity = (WorkoutActivity) getActivity();
            if (activity != null)
                activity.handleFinishedWorkout(this, lifts, true);
        });
        validator = new TextValidator(finishButton);

        int[] ids = {R.id.firstInput, R.id.secondInput, R.id.thirdInput, R.id.fourthInput};
        for (int i = 0; i < 4; ++i)
            validator.addChild((short) 999, view.findViewById(ids[i]));
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
