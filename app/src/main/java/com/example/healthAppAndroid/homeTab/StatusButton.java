package com.example.healthAppAndroid.homeTab;

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

    public StatusButton(Context c) {
        super(c);
        setup(c, null);
    }

    public StatusButton(Context c, AttributeSet attrs) {
        super(c, attrs);
        setup(c, attrs);
    }

    private void setup(Context c, AttributeSet attrs) {
        inflate(c, R.layout.status_button, this);
        button = findViewById(R.id.button);
        headerLabel = findViewById(R.id.headerLabel);
        checkbox = findViewById(R.id.checkbox);
        String buttonText = null;
        boolean hideBox = false;

        if (attrs != null) {
            TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.StatusButton, 0, 0);
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

    public void updateAccessibility() {
        StringBuilder builder = new StringBuilder(64);
        String header = headerLabel.getText().toString();
        if (!header.isEmpty()) {
            builder.append(header);
            builder.append(", ");
        }
        builder.append(button.getText());
        button.setContentDescription(builder.toString());
    }
}
