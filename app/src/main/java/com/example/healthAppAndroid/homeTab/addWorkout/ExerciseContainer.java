package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;

public final class ExerciseContainer extends LinearLayout {
    final HeaderView headerView;
    ExerciseView[] viewsArr;

    public ExerciseContainer(Context c) {
        super(c);
        inflate(c, R.layout.exercise_container, this);
        headerView = findViewById(R.id.headerView);
    }

    ExerciseContainer(Context c, Circuit g, int idx, View.OnClickListener action) {
        this(c);
        int size = g.exercises.length;
        viewsArr = new ExerciseView[size];
        String headerStr = g.headerStr.str.toString();
        headerView.headerLabel.setText(headerStr);
        if (headerStr.isEmpty())
            headerView.headerLabel.setVisibility(GONE);
        LinearLayout vStack = findViewById(R.id.mainStack);

        boolean addHint = size > 1;
        for (int i = 0; i < size; ++i) {
            int tag = (idx << 8) | (i + 1);
            ExerciseView v = new ExerciseView(c, g.exercises[i], tag, action);
            if (addHint)
                v.button.setHint(c.getString(R.string.exerciseProgressHint, i + 1, size));
            vStack.addView(v);
            viewsArr[i] = v;
        }
    }
}
