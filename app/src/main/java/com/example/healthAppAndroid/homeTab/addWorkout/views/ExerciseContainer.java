package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.workouts.ExerciseGroup;

public class ExerciseContainer extends LinearLayout {
    View divider;
    TextView headerLabel;
    ExerciseView[] viewsArr;

    public ExerciseContainer(Context context) {
        super(context);
        setup();
    }

    public ExerciseContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.exercise_container, this);
        divider = findViewById(R.id.topDivider);
        headerLabel = findViewById(R.id.headerLabel);
    }

    void setup(ExerciseGroup g, int idx, View.OnClickListener action) {
        int size = g.exercises.length;
        viewsArr = new ExerciseView[size];
        headerLabel.setText(g.createHeader(getContext()));
        LinearLayout vStack = findViewById(R.id.mainStack);

        for (int i = 0; i < size; ++i) {
            int tag = (idx << 8) | (i + 1);
            ExerciseView v = new ExerciseView(getContext());
            v.setup(g.exercises[i], tag, action);
            vStack.addView(v);
            viewsArr[i] = v;
        }
    }
}
