package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.workouts.ExerciseGroup;

public class ExerciseContainer extends ConstraintLayout {
    public View divider;
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
        divider = findViewById(R.id.topDivider);
        headerLabel = findViewById(R.id.headerLabel);
    }

    public void setup(ExerciseGroup g, int idx, View.OnClickListener action) {
        int size = g.exercises.length;
        viewsArr = new ExerciseView[size];
        Context context = getContext();
        headerLabel.setText(g.createHeader(context));
        LinearLayout vStack = findViewById(R.id.mainStack);

        for (int i = 0; i < size; ++i) {
            int tag = (idx << 8) | (i + 1);
            ExerciseView v = new ExerciseView(context);
            v.setup(g.exercises[0], tag, action);
            vStack.addView(v);
            viewsArr[i] = v;
        }
    }
}
