package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.view.View;

import com.example.healthAppAndroid.core.AppColors;
import com.example.healthAppAndroid.core.StatusButton;

public final class ExerciseView extends StatusButton {
    boolean userInteractionEnabled = true;
    ExerciseEntry entry;

    public ExerciseView(Context c) { super(c); }

    ExerciseView(Context c, ExerciseEntry e, int tag, View.OnClickListener action) {
        super(c);
        entry = e;
        button.setId(tag);
        button.setOnClickListener(action);
        if (e.headerStr.str.toString().isEmpty())
            headerLabel.setVisibility(GONE);
        configure();
    }

    void configure() {
        headerLabel.setText(entry.headerStr.str);
        if (entry.state == ExerciseEntry.State.resting) {
            button.setText(entry.restStr);
        } else {
            button.setText(entry.titleStr.str);
        }
        updateAccessibility();

        switch (entry.state) {
            case ExerciseEntry.State.disabled:
                checkbox.setBackgroundColor(AppColors.gray);
                enableButton(false);
                break;

            case ExerciseEntry.State.active:
                if (entry.type == ExerciseEntry.Type.duration)
                    userInteractionEnabled = false;
            case ExerciseEntry.State.resting:
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
