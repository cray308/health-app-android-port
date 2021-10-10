package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public class ChartSeparator extends LinearLayout {
    public ChartSeparator(Context context) {
        super(context);
        setup(null);
    }

    public ChartSeparator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.chart_separator, this);
        TextView headerLabel = findViewById(R.id.headerLabel);
        View divider = findViewById(R.id.divider);
        String labelText = null;
        boolean hideDivider = false;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChartSeparator, 0, 0);
            try {
                labelText = a.getString(R.styleable.ChartSeparator_sepHeader);
                hideDivider = a.getBoolean(R.styleable.ChartSeparator_hideDivider, false);
            } finally {
                a.recycle();
            }
        }
        headerLabel.setText(labelText);
        if (hideDivider)
            divider.setVisibility(GONE);
    }
}
