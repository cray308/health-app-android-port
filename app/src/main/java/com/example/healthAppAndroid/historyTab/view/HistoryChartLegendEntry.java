package com.example.healthAppAndroid.historyTab.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public class HistoryChartLegendEntry extends LinearLayout {
    View form;
    TextView label;

    public HistoryChartLegendEntry(Context context) {
        super(context);
        setup(null);
    }

    public HistoryChartLegendEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
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
