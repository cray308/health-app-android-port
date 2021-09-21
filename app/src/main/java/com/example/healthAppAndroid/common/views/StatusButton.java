package com.example.healthAppAndroid.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;

public class StatusButton extends ConstraintLayout {
    public static abstract class State {
        public final static byte Disabled = 0;
        public final static byte Active = 1;
        public final static byte Finished = 3;
    }

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
        String buttonText = null;
        boolean hideBox = false, hideHeader = false;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.StatusButton, 0, 0);
            try {
                hideBox = a.getBoolean(R.styleable.StatusButton_hideCheckbox, false);
                hideHeader = a.getBoolean(R.styleable.StatusButton_hideHeader, false);
                buttonText = a.getString(R.styleable.StatusButton_buttonLabel);
            } finally {
                a.recycle();
            }
        }

        if (hideHeader)
            headerLabel.setVisibility(GONE);
        if (hideBox)
            checkbox.setVisibility(GONE);
        setProperties(null, buttonText, (byte) 0, true);
    }

    public void setProperties(String labelText, String buttonText,
                              byte state, boolean enableButton) {
        headerLabel.setText(labelText);
        button.setText(buttonText);
        updateStateAndButton(state, enableButton);
    }

    public void updateStateAndButton(byte state, boolean enableButton) {
        button.setEnabled(enableButton);
        button.setTextColor(enableButton ? AppColors.labelNormal : AppColors.labelDisabled);
        switch (state) {
            case State.Disabled:
                checkbox.setBackgroundColor(AppColors.gray);
                break;
            case State.Active:
                checkbox.setBackgroundColor(AppColors.orange);
                break;
            default:
                checkbox.setBackgroundColor(AppColors.green);
                break;
        }
    }
}
