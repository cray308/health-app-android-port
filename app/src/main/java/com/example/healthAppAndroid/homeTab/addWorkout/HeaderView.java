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
    final TextView header;

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.header_view, this);
        divider = findViewById(R.id.divider);
        header = findViewById(R.id.header);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HeaderView,
                                                                   0, 0);
        try {
            if (arr.getBoolean(R.styleable.HeaderView_hideSeparator, false))
                divider.setVisibility(GONE);
            header.setText(arr.getString(R.styleable.HeaderView_headerText));
        } finally {
            arr.recycle();
        }
    }
}
