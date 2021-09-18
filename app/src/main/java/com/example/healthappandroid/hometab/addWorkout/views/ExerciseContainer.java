package com.example.healthappandroid.hometab.addWorkout.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.workouts.ExerciseGroup;

public class ExerciseContainer extends ConstraintLayout {
    public LinearLayout divider;
    public TextView headerLabel;
    public ExerciseView[] viewsArr;

    public ExerciseContainer(@NonNull Context context) {
        super(context);
        setup();
    }

    public ExerciseContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.exercise_container, this);
        divider = findViewById(R.id.exerciseContainerDivider);
        headerLabel = findViewById(R.id.exerciseContainerHeader);
    }

    public void setup(ExerciseGroup g, int idx, View.OnClickListener action) {
        int size = g.exercises.length;
        viewsArr = new ExerciseView[size];
        headerLabel.setText(g.createHeader());
        Context context = getContext();
        LinearLayout vStack = findViewById(R.id.exerciseContainerMainStack);

        for (int i = 0; i < size; ++i) {
            int tag = (idx << 8) | (i + 1);
            ExerciseView v = new ExerciseView(context);
            v.setup(g.exercises[0], tag, action);
            vStack.addView(v);
            viewsArr[i] = v;
        }
    }
}
