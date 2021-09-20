package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.healthAppAndroid.R;

public class HistoryChartLegendEntry extends LinearLayout {
    public View form;
    public TextView label;

    public HistoryChartLegendEntry(Context context) {
        super(context);
        setup(null);
    }

    public HistoryChartLegendEntry(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(@Nullable AttributeSet attrs) {
        inflate(getContext(), R.layout.history_chart_legend_entry, this);
        form = findViewById(R.id.form);
        label = findViewById(R.id.label);
        int color = 0;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.HistoryChartLegendEntry, 0, 0);
            try {
                color = a.getColor(R.styleable.HistoryChartLegendEntry_formColor, 0);
            } finally {
                a.recycle();
            }
        }
        form.setBackgroundColor(color);
    }
}
