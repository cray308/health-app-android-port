package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.healthAppAndroid.R;

public class StartStopView extends LinearLayout {
    Button btn;

    public StartStopView(Context context) {
        super(context);
        setup();
    }

    public StartStopView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.workout_start_stop_view, this);
        btn = findViewById(R.id.btn);
    }
}
