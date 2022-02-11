package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.historyTab.HistoryFragment;

public final class SegmentedControl extends LinearLayout {
    private final Button[] buttons = {null, null, null};
    public HistoryFragment delegate;
    public byte selectedIndex = 0;

    public SegmentedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.segmented_control, this);
        buttons[0] = findViewById(R.id.buttonLeft);
        buttons[1] = findViewById(R.id.buttonMid);
        buttons[2] = findViewById(R.id.buttonRight);
        String[] titles = {null, null, null};
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
          attrs, R.styleable.SegmentedControl, 0, 0);
        try {
            titles[0] = a.getString(R.styleable.SegmentedControl_leftTitle);
            titles[1] = a.getString(R.styleable.SegmentedControl_midTitle);
            titles[2] = a.getString(R.styleable.SegmentedControl_rightTitle);
        } finally {
            a.recycle();
        }

        View.OnClickListener listener = view -> {
            byte selected = 0;
            int id = view.getId();
            if (id == R.id.buttonMid) {
                selected = 1;
            } else if (id == R.id.buttonRight) {
                selected = 2;
            }
            if (selected != selectedIndex)
                setSelectedIndex(selected);
        };

        for (int i = 0; i < 3; ++i) {
            buttons[i].setText(titles[i]);
            buttons[i].setOnClickListener(listener);
        }
    }

    public void setSelectedIndex(byte newIndex) {
        buttons[selectedIndex].setBackgroundColor(AppColors.gray5);
        buttons[newIndex].setBackgroundColor(AppColors.gray2);
        selectedIndex = newIndex;
        if (delegate != null)
            delegate.didSelectSegment(newIndex);
    }
}
