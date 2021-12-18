package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;

public final class ExerciseContainer extends LinearLayout {
    HeaderView headerView;
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
        headerView = findViewById(R.id.headerView);
    }

    void setup(Circuit g, int idx, View.OnClickListener action) {
        int size = g.exercises.length;
        viewsArr = new ExerciseView[size];
        String headerStr = g.headerStr.toString();
        headerView.headerLabel.setText(headerStr);
        if (headerStr.isEmpty())
            headerView.headerLabel.setVisibility(GONE);
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
