package com.example.healthAppAndroid.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;

public class SegmentedControl extends LinearLayout {
    public interface Delegate {
        void didSelectSegment(byte index);
    }

    public byte selectedIndex;
    private final Button[] buttons = {null, null, null};
    public Delegate delegate;

    private final View.OnClickListener listener = view -> {
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

    public SegmentedControl(Context context) {
        super(context);
        setup(null);
    }

    public SegmentedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.segmented_control, this);
        buttons[0] = findViewById(R.id.buttonLeft);
        buttons[1] = findViewById(R.id.buttonMid);
        buttons[2] = findViewById(R.id.buttonRight);
        String[] titles = {null, null, null};
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.SegmentedControl, 0, 0);
            try {
                titles[0] = a.getString(R.styleable.SegmentedControl_leftTitle);
                titles[1] = a.getString(R.styleable.SegmentedControl_midTitle);
                titles[2] = a.getString(R.styleable.SegmentedControl_rightTitle);
            } finally {
                a.recycle();
            }
        }

        for (int i = 0; i < 3; ++i) {
            buttons[i].setText(titles[i]);
            buttons[i].setOnClickListener(listener);
        }
    }

    public void setSelectedIndex(byte newIndex) {
        selectedIndex = newIndex;
        for (byte i = 0; i < 3; ++i)
            buttons[i].setBackgroundColor(i == selectedIndex ? AppColors.gray2 : AppColors.gray5);
        if (delegate != null)
            delegate.didSelectSegment(newIndex);
    }
}
