package com.example.healthAppAndroid.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthAppAndroid.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InputView extends ConstraintLayout {
    public TextInputLayout field;
    public TextInputEditText textField;

    public InputView(Context context) {
        super(context);
        setup(null);
    }

    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.input_view, this);
        field = findViewById(R.id.field);
        textField = findViewById(R.id.fieldTextView);
        String hintText = null;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.InputView, 0, 0);
            try {
                hintText = a.getString(R.styleable.InputView_fieldHint);
            } finally {
                a.recycle();
            }
        }

        field.setHint(hintText);
    }
}
