package com.example.healthappandroid.common.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healthappandroid.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InputView extends ConstraintLayout {
    public TextInputLayout field;
    public TextInputEditText textField;

    public InputView(Context context) {
        super(context);
        setup(null);
    }

    public InputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    private void setup(@Nullable AttributeSet attrs) {
        inflate(getContext(), R.layout.input_view, this);
        field = findViewById(R.id.field);
        textField = findViewById(R.id.fieldTextView);
        String initialText = null, hintText = null;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs, R.styleable.InputView, 0, 0);
            try {
                initialText = a.getString(R.styleable.InputView_initialText);
                hintText = a.getString(R.styleable.InputView_fieldHint);
            } finally {
                a.recycle();
            }
        }

        textField.setText(initialText);
        field.setHint(hintText);
    }
}
