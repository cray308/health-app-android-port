package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthAppAndroid.R;

public class ChartSeparator extends ConstraintLayout {
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
        String labelText = null;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChartSeparator, 0, 0);
            try {
                labelText = a.getString(R.styleable.ChartSeparator_sepHeader);
            } finally {
                a.recycle();
            }
        }
        headerLabel.setText(labelText);
    }
}
