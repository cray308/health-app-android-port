package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.example.healthAppAndroid.common.helpers.ControlState;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.views.StatusButton;
import com.example.healthAppAndroid.common.workouts.ExerciseEntry;

public final class ExerciseView extends StatusButton {
    public boolean userInteractionEnabled = true;

    public ExerciseView(Context context) { super(context); }

    public ExerciseView(Context context, AttributeSet attrs) { super(context, attrs); }

    void setup(ExerciseEntry e, int tag, View.OnClickListener action) {
        button.setId(tag);
        button.setOnClickListener(action);
        configure(e);
    }

    public void configure(ExerciseEntry e) {
        Context context = getContext();
        button.setText(e.createTitle(context));
        headerLabel.setText(e.createSetsTitle(context));

        switch (e.state) {
            case ControlState.disabled:
                checkbox.setBackgroundColor(AppColors.gray);
                enableButton(false);
                break;

            case ControlState.active:
                if (e.type == ExerciseEntry.Type.duration)
                    userInteractionEnabled = false;
            case ControlState.resting:
                enableButton(true);
                checkbox.setBackgroundColor(AppColors.orange);
                break;

            default:
                enableButton(false);
                checkbox.setBackgroundColor(AppColors.green);
        }
    }

    private void enableButton(boolean enabled) {
        if (userInteractionEnabled)
            button.setEnabled(enabled);
        button.setTextColor(enabled ? AppColors.labelNormal : AppColors.labelDisabled);
    }
}
