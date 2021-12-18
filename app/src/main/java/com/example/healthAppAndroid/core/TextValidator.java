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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class TextValidator {
    public static final class InputView extends LinearLayout implements TextWatcher {
        public TextInputLayout field;
        private TextInputEditText textField;
        private short min = 1;
        private short max = 999;
        private TextValidator delegate;
        private boolean valid = false;
        private boolean isInputNumeric = false;
        private short result = 0;

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
                if (!delegate.validChars.contains(s.charAt(i))) {
                    isInputNumeric = false;
                    return;
                }
            }
            isInputNumeric = true;
        }

        public void afterTextChanged(Editable s) {
            int len = s.length();
            if (!isInputNumeric || len == 0) {
                showErrorMsg();
                return;
            }

            short res = -1;
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

    private final Set<Character> validChars = new HashSet<>(
        Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
    private final InputView[] children = {null, null, null, null};
    private int count = 0;
    private final Button button;

    public TextValidator(Button button) { this.button = button; }

    public void enableButton() {
        button.setEnabled(true);
        button.setTextColor(AppColors.blue);
    }

    public void clearFocus() {
        for (int i = 0; i < count; ++i) children[i].textField.clearFocus();
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

    public void addChild(short max, InputView view) {
        view.setup(max, this);
        children[count++] = view;
    }

    public void reset(short[] values) {
        for (int i = 0; i < count; ++i) {
            children[i].result = values[i];
            children[i].valid = true;
            children[i].field.setError(null);
            children[i].textField.setText(String.format(Locale.US, "%d", values[i]));
        }
    }

    public short[] getResults() {
        short[] results = {0, 0, 0, 0};
        for (int i = 0; i < count; ++i)
            results[i] = children[i].result;
        return results;
    }
}
