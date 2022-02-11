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
        private short min = 1;
        private short max = 999;
        short result = 0;
        boolean valid = false;
        private boolean isInputNumeric = false;
        private boolean emptyInputAllowed = false;

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
            boolean zeroMin = false;

            if (attrs != null) {
                TypedArray a = getContext().getTheme().obtainStyledAttributes(
                  attrs, R.styleable.InputView, 0, 0);
                try {
                    hintText = a.getString(R.styleable.InputView_fieldHint);
                    zeroMin = a.getBoolean(R.styleable.InputView_zeroMin, false);
                    emptyInputAllowed = a.getBoolean(R.styleable.InputView_emptyInputAllowed, false);
                } finally {
                    a.recycle();
                }
            }
            field.setHint(hintText);
            if (zeroMin)
                min = 0;
        }

        private void setup(short maxVal, TextValidator validator) {
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

            short res = -1;
            if (!isEmpty) {
                try {
                    res = (short) Integer.parseInt(s.toString());
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
            result = res;
            delegate.checkFields();
        }

        private void showErrorMsg() {
            valid = false;
            field.setError(getContext().getResources().getQuantityString(
              R.plurals.inputFieldError, 1, min, max));
            delegate.disableButton();
        }
    }

    final InputView[] children = {null, null, null, null, null};
    private final Button button;
    private byte count = 0;

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
        for (byte i = 0; i < count; ++i) {
            if (!children[i].valid) {
                disableButton();
                return;
            }
        }
        enableButton();
    }

    public void addChild(short max, InputView view) {
        view.setup(max, this);
        children[count++] = view;
    }

    public short[] getResults() {
        short[] results = {0, 0, 0, 0, 0};
        for (byte i = 0; i < count; ++i)
            results[i] = children[i].result;
        return results;
    }
}
