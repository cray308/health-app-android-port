package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.homeTab.StatusView;

public final class ExerciseView extends StatusView {
    Exercise exercise;
    boolean userInteractionEnabled = true;

    private ExerciseView(Context context) { super(context); }

    ExerciseView(Context context, Exercise exercise, int tag, View.OnClickListener action) {
        this(context);
        this.exercise = exercise;
        button.setId(tag);
        button.setOnClickListener(action);
        header.setText(exercise.header.str);
        if (exercise.header.str.toString().isEmpty()) header.setVisibility(GONE);
        button.setText(exercise.title.str);
        enableButton(false);
        configure();
    }

    void configure() {
        Context context = getContext();
        switch (exercise.state) {
            case Exercise.State.disabled:
                box.setBackgroundColor(ContextCompat.getColor(context, R.color.systemGray));
                break;

            case Exercise.State.active:
                enableButton(true);
                box.setBackgroundColor(ContextCompat.getColor(context, R.color.systemOrange));
            case Exercise.State.activeCont:
                if (exercise.type == Exercise.Type.duration) {
                    userInteractionEnabled = false;
                    button.setEnabled(false);
                }
                if (exercise.completedSets != 0) {
                    button.setText(exercise.title.str);
                    header.setText(exercise.header.str);
                    updateAccessibility(exercise.header.str, exercise.title.str);
                }
                break;

            case Exercise.State.resting:
                button.setText(exercise.rest);
                if (exercise.sets > 1) updateAccessibility(exercise.header.str, exercise.rest);
                break;

            default:
                enableButton(false);
                if (exercise.rest != null) {
                    button.setText(exercise.title.str);
                    if (exercise.sets > 1)
                        updateAccessibility(exercise.header.str, exercise.title.str);
                }
                box.setBackgroundColor(ContextCompat.getColor(context, R.color.systemGreen));
        }
    }

    private void enableButton(boolean enabled) {
        button.setEnabled(enabled && userInteractionEnabled);
    }
}
