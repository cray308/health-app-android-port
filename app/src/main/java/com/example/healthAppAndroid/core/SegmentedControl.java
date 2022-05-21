package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SegmentedControl extends LinearLayout {
    public interface Delegate {
        void selectedIndexChanged(int index);
    }

    private final MaterialButtonToggleGroup toggle;
    public Delegate delegate;
    private final int[] segmentIds = {R.id.buttonLeft, R.id.buttonMid, R.id.buttonRight};
    int selectedIndex;

    public SegmentedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.segmented_control, this);
        toggle = findViewById(R.id.toggle);
        TypedArray arr = context.getTheme().obtainStyledAttributes(
          attrs, R.styleable.SegmentedControl, 0, 0);
        try {
            String[] titles = {null, null, null};
            titles[0] = arr.getString(R.styleable.SegmentedControl_leftTitle);
            titles[1] = arr.getString(R.styleable.SegmentedControl_centerTitle);
            titles[2] = arr.getString(R.styleable.SegmentedControl_rightTitle);
            for (int i = 0; i < 3; ++i) {
                ((TextView)findViewById(segmentIds[i])).setText(titles[i]);
            }
        } finally {
            arr.recycle();
        }

        toggle.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
            if (!isChecked) return;
            for (int i = 0; i < 3; ++i) {
                if (checkedId == segmentIds[i]) {
                    selectedIndex = i;
                    break;
                }
            }
            if (delegate != null) delegate.selectedIndexChanged(selectedIndex);
        });
    }

    public void setSelectedIndex(int index) {
        toggle.clearChecked();
        toggle.check(segmentIds[index]);
    }
}
