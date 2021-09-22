package com.example.healthAppAndroid.common.helpers;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.views.InputView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TextValidator {
    private static class ChildValidator implements TextWatcher {
        private final InputView view;
        private final short min;
        private final short max;
        private final TextValidator delegate;
        private boolean valid = false;
        private boolean isInputNumeric = false;
        public short result = 0;

        private ChildValidator(short min, short max, InputView view, TextValidator delegate) {
            this.min = min;
            this.max = max;
            this.view = view;
            this.delegate = delegate;
            view.textField.addTextChangedListener(this);
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
            } catch (Exception e) {
                Log.e("checkInput", "Error while validating input", e);
            }

            if (res < min || res > max) {
                showErrorMsg();
                return;
            }

            view.field.setError(null);
            valid = true;
            result = res;
            delegate.checkFields();
        }

        private void showErrorMsg() {
            valid = false;
            view.field.setError(view.getContext().getString(R.string.inputFieldError, min, max));
            delegate.disableButton();
        }
    }

    private final Set<Character> validChars = new HashSet<>(
        Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
    private final ChildValidator[] children = {null, null, null, null};
    private int count = 0;
    private final Button button;
    private final int enabledColor;

    public TextValidator(Button button, int enabledColor) {
        this.button = button;
        this.enabledColor = enabledColor;
    }

    public void enableButton() {
        button.setEnabled(true);
        button.setTextColor(enabledColor);
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
        children[count++] = new ChildValidator(min, max, view, this);
    }

    public void reset(short[] values) {
        for (int i = 0; i < count; ++i) {
            children[i].result = values[i];
            children[i].valid = true;
            children[i].view.field.setError(null);
            children[i].view.textField.setText(ViewHelper.format("%d", values[i]));
        }
    }

    public short[] getResults() {
        short[] results = new short[4];
        for (int i = 0; i < count; ++i)
            results[i] = children[i].result;
        return results;
    }
}
