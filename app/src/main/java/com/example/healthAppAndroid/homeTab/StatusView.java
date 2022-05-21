package com.example.healthAppAndroid.homeTab;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public class StatusView extends LinearLayout {
    protected View box;
    public TextView header;
    public Button button;

    public StatusView(Context context) {
        super(context);
        setup(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        inflate(context, R.layout.status_view, this);
        button = findViewById(R.id.button);
        header = findViewById(R.id.header);
        box = findViewById(R.id.checkbox);

        if (attrs != null) {
            TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StatusView,
                                                                       0, 0);
            try {
                if (arr.getBoolean(R.styleable.StatusView_hideCheckbox, false)) {
                    box.setVisibility(GONE);
                    header.setVisibility(GONE);
                }
                button.setText(arr.getString(R.styleable.StatusView_buttonText));
            } finally {
                arr.recycle();
            }
        }
    }

    public void updateAccessibility(CharSequence headerText, CharSequence title) {
        button.setContentDescription(getContext().getString(R.string.separator, headerText, title));
    }
}
