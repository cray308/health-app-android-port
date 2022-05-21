package com.example.healthAppAndroid.historyTab;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public final class HistoryLegendEntry extends LinearLayout {
    final TextView label;

    public HistoryLegendEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.history_legend_entry, this);
        label = findViewById(R.id.label);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LegendEntry,
                                                                   0, 0);
        try {
            int color = arr.getColor(R.styleable.LegendEntry_formColor, 0);
            findViewById(R.id.form).setBackgroundColor(color);
        } finally {
            arr.recycle();
        }
    }
}
