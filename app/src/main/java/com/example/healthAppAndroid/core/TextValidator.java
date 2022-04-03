package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
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
        private String errorSuffix = "";
        private int min;
        private int max;
        short result = 0;
        boolean valid = false;
        private boolean isInputNumeric = false;
        private boolean emptyInputAllowed = false;

        public InputView(Context c) {
            super(c);
            setup(c, null);
        }

        public InputView(Context c, AttributeSet attrs) {
            super(c, attrs);
            setup(c, attrs);
        }

        private void setup(Context c, AttributeSet attrs) {
            inflate(c, R.layout.input_view, this);
            field = findViewById(R.id.field);
            textField = findViewById(R.id.fieldTextView);
            String hintText = null, suffix = null;

            if (attrs != null) {
                TypedArray a = c.getTheme().obtainStyledAttributes(
                  attrs, R.styleable.InputView, 0, 0);
                try {
                    hintText = a.getString(R.styleable.InputView_fieldHint);
                    suffix = a.getString(R.styleable.InputView_extraError);
                    emptyInputAllowed = a.getBoolean(R.styleable.InputView_emptyInputAllowed, false);
                } finally {
                    a.recycle();
                }
            }
            field.setHint(hintText);
            if (suffix != null)
                errorSuffix = suffix;
        }

        private void setup(short minVal, short maxVal, TextValidator validator) {
            min = minVal;
            max = maxVal;
            delegate = validator;
            textField.addTextChangedListener(this);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count == 0) {
                isInputNumeric = true;
                return;
            }
            for (int i = start; i < start + count; ++i) {
                char c = s.charAt(i);
                if (c < 48 || c > 57) {
                    isInputNumeric = false;
                    return;
                }
            }
            isInputNumeric = true;
        }

        public void afterTextChanged(Editable s) {
            boolean isEmpty = s.length() == 0;
            if (!isInputNumeric || (isEmpty && !emptyInputAllowed)) {
                showErrorMsg();
                return;
            }

            int res = -1;
            if (!isEmpty) {
                try {
                    res = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    Log.e("checkInput", "Error while validating input", e);
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
            delegate.checkFields();
        }

        private void showErrorMsg() {
            valid = false;
            String msg = getContext().getResources().getQuantityString(
              R.plurals.inputFieldError, 1, min, max) + errorSuffix;
            field.setError(msg);
            delegate.disableButton();
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

    private void disableButton() {
        button.setEnabled(false);
        button.setTextColor(AppColors.labelDisabled);
    }

    private void checkFields() {
        for (int i = 0; i < count; ++i) {
            if (!children[i].valid) {
                disableButton();
                return;
            }
        }
        enableButton();
    }

    public void addChild(short min, short max, InputView view) {
        view.setup(min, max, this);
        children[count++] = view;
    }

    public short[] getResults() {
        short[] results = {0, 0, 0, 0, 0};
        for (int i = 0; i < count; ++i)
            results[i] = children[i].result;
        return results;
    }
}
