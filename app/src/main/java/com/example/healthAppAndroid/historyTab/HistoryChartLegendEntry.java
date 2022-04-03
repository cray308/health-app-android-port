package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public final class HistoryChartLegendEntry extends LinearLayout {
    final TextView label;

    public HistoryChartLegendEntry(Context c, AttributeSet attrs) {
        super(c, attrs);
        inflate(c, R.layout.history_chart_legend_entry, this);
        View form = findViewById(R.id.form);
        label = findViewById(R.id.label);
        int color;
        TypedArray a = c.getTheme().obtainStyledAttributes(
          attrs, R.styleable.HistoryChartLegendEntry, 0, 0);
        try {
            color = a.getColor(R.styleable.HistoryChartLegendEntry_formColor, 0);
        } finally {
            a.recycle();
        }
        form.setBackgroundColor(color);
    }
}
