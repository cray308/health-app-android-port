package com.example.healthAppAndroid.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.healthAppAndroid.R;

public class StatusButton extends LinearLayout {
    public View checkbox;
    public TextView headerLabel;
    public Button button;

    public StatusButton(Context context) {
        super(context);
        setup(null);
    }

    public StatusButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.status_button, this);
        button = findViewById(R.id.button);
        headerLabel = findViewById(R.id.headerLabel);
        checkbox = findViewById(R.id.checkbox);
        String buttonText = null;
        boolean hideBox = false;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.StatusButton, 0, 0);
            try {
                hideBox = a.getBoolean(R.styleable.StatusButton_hideCheckbox, false);
                buttonText = a.getString(R.styleable.StatusButton_buttonLabel);
            } finally {
                a.recycle();
            }
        }

        if (hideBox) {
            checkbox.setVisibility(GONE);
            headerLabel.setVisibility(GONE);
        }
        button.setText(buttonText);
    }
}
