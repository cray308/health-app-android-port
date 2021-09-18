package com.example.healthappandroid.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.shareddata.AppColors;

public class StatusButton extends ConstraintLayout {
    public final static byte StateDisabled = 0;
    public final static byte StateActive = 1;
    public final static byte StateFinished = 3;

    public View checkbox;
    public TextView headerLabel;
    public Button button;

    public StatusButton(Context context) {
        super(context);
        setup(null);
    }

    public StatusButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(@Nullable AttributeSet attrs) {
        inflate(getContext(), R.layout.status_button, this);
        button = findViewById(R.id.button);
        headerLabel = findViewById(R.id.headerLabel);
        checkbox = findViewById(R.id.checkbox);
        String labelText = null, buttonText = null;
        boolean disableBtn = false, hideBox = false, hideHeader = false;
        byte state = 0;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs, R.styleable.StatusButton, 0, 0);
            try {
                hideBox = a.getBoolean(R.styleable.StatusButton_hideCheckbox, false);
                hideHeader = a.getBoolean(R.styleable.StatusButton_hideHeader, false);
                disableBtn = a.getBoolean(R.styleable.StatusButton_disableButton, false);
                labelText = a.getString(R.styleable.StatusButton_headerText);
                buttonText = a.getString(R.styleable.StatusButton_buttonLabel);
                state = (byte) a.getInteger(R.styleable.StatusButton_checkboxState, 0);
            } finally {
                a.recycle();
            }
        }

        if (hideHeader)
            headerLabel.setVisibility(GONE);
        if (hideBox)
            checkbox.setVisibility(GONE);
        setProperties(labelText, buttonText, state, !disableBtn);
    }

    public void setProperties(String labelText, String buttonText, byte state, boolean enableButton) {
        headerLabel.setText(labelText);
        button.setText(buttonText);
        updateStateAndButton(state, enableButton);
    }

    public void updateStateAndButton(byte state, boolean enableButton) {
        button.setEnabled(enableButton);
        button.setTextColor(enableButton ? AppColors.labelNormal : AppColors.labelDisabled);
        switch (state) {
            case StateDisabled:
                checkbox.setBackgroundColor(AppColors.gray);
                break;
            case StateActive:
                checkbox.setBackgroundColor(AppColors.orange);
                break;
            default:
                checkbox.setBackgroundColor(AppColors.green);
                break;
        }
    }
}
