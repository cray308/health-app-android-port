package com.example.healthAppAndroid.core;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public final class TextValidator {
    public static final class InputView extends LinearLayout implements TextWatcher {
        public TextInputLayout field;
        TextInputEditText textField;
        private TextValidator delegate;
        private int id;
        private int min;
        private int max;
        short result = 0;
        boolean valid = false;
        private boolean emptyInputAllowed = false;

        public InputView(Context c) {
            super(c);
            setup(c);
        }

        public InputView(Context c, AttributeSet attrs) {
            super(c, attrs);
            setup(c);
        }

        private void setup(Context c) {
            inflate(c, R.layout.input_view, this);
            field = findViewById(R.id.field);
            textField = findViewById(R.id.fieldTextView);
        }

        private void setup(short minVal, short maxVal, int resId, TextValidator validator) {
            min = minVal;
            max = maxVal;
            id = resId;
            emptyInputAllowed = resId == R.plurals.inputFieldErrorEmpty;
            delegate = validator;
            textField.addTextChangedListener(this);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        public void afterTextChanged(Editable s) {
            boolean isEmpty = s.length() == 0;
            if (isEmpty && !emptyInputAllowed) {
                showErrorMsg();
                return;
            }

            int res = -1;
            if (!isEmpty) {
                try {
                    res = Integer.parseInt(s.toString());
                } finally {
                    if (res < min || res > max) {
                        showErrorMsg();
                        return;
                    }
                }
            }

            field.setError(null);
            valid = true;
            result = (short)res;
            for (int i = 0; i < delegate.count; ++i) {
                if (!delegate.children[i].valid) {
                    disableButton();
                    return;
                }
            }
            delegate.enableButton();
        }

        private void showErrorMsg() {
            valid = false;
            field.setError(getContext().getResources().getQuantityString(id, 1, min, max));
            disableButton();
        }

        private void disableButton() {
            delegate.button.setEnabled(false);
            delegate.button.setTextColor(AppColors.labelDisabled);
        }
    }

    final InputView[] children = {null, null, null, null, null};
    private final Button button;
    private int count = 0;

    public TextValidator(Button button) { this.button = button; }

    public void enableButton() {
        button.setEnabled(true);
        button.setTextColor(AppColors.blue);
    }

    public void addChild(short min, short max, int id, InputView view) {
        view.setup(min, max, id, this);
        children[count++] = view;
    }

    public short[] getResults() {
        short[] results = {0, 0, 0, 0, 0};
        for (int i = 0; i < count; ++i)
            results[i] = children[i].result;
        return results;
    }
}
