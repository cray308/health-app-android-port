package com.example.healthappandroid.historytab.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthappandroid.R;

public class ChartSeparator extends ConstraintLayout {
    public ChartSeparator(@NonNull Context context) {
        super(context);
        setup(null);
    }

    public ChartSeparator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(@Nullable AttributeSet attrs) {
        inflate(getContext(), R.layout.chart_separator, this);
        TextView headerLabel = findViewById(R.id.sepHeaderLabel);
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
