package com.example.healthAppAndroid.homeTab.addWorkout.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public final class HeaderView extends LinearLayout {
    public View divider;
    public TextView headerLabel;

    public HeaderView(Context context) {
        super(context);
        setup(null);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.header_view, this);
        divider = findViewById(R.id.divider);
        headerLabel = findViewById(R.id.headerLabel);
        boolean hideDivider = false;
        String headerText = null;
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
              attrs, R.styleable.HeaderView, 0, 0);
            try {
                hideDivider = a.getBoolean(R.styleable.HeaderView_hideSeparator, false);
                headerText = a.getString(R.styleable.HeaderView_headerText);
            } finally {
                a.recycle();
            }
        }

        if (hideDivider)
            divider.setVisibility(GONE);
        headerLabel.setText(headerText);
    }
}
