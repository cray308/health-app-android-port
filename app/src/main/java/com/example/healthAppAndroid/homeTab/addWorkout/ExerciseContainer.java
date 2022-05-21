package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;

public final class ExerciseContainer extends LinearLayout {
    final HeaderView headerView;
    ExerciseView[] views;

    private ExerciseContainer(Context context) {
        super(context);
        inflate(context, R.layout.exercise_container, this);
        headerView = findViewById(R.id.headerView);
    }

    ExerciseContainer(Context context, Circuit circuit, int index, View.OnClickListener action) {
        this(context);
        int size = circuit.exercises.length;
        views = new ExerciseView[size];
        headerView.header.setText(circuit.header.str);
        if (circuit.header.str.toString().isEmpty()) headerView.header.setVisibility(GONE);
        LinearLayout stack = findViewById(R.id.stack);

        boolean addHint = size > 1;
        for (int i = 0; i < size; ++i) {
            int tag = (index << 8) | (i + 1);
            ExerciseView view = new ExerciseView(context, circuit.exercises[i], tag, action);
            if (addHint)
                view.button.setHint(context.getString(R.string.exerciseProgress, i + 1, size));
            stack.addView(view);
            views[i] = view;
        }
    }
}
