package com.example.healthAppAndroid.homeTab.addWorkout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public final class HeaderView extends LinearLayout {
    public final View divider;
    public final TextView headerLabel;

    public HeaderView(Context c, AttributeSet attrs) {
        super(c, attrs);
        inflate(c, R.layout.header_view, this);
        divider = findViewById(R.id.divider);
        headerLabel = findViewById(R.id.headerLabel);
        boolean hideDivider;
        String headerText;
        TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.HeaderView, 0, 0);
        try {
            hideDivider = a.getBoolean(R.styleable.HeaderView_hideSeparator, false);
            headerText = a.getString(R.styleable.HeaderView_headerText);
        } finally {
            a.recycle();
        }

        if (hideDivider) divider.setVisibility(GONE);
        headerLabel.setText(headerText);
    }
}
