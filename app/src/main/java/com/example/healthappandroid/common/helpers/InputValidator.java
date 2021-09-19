package com.example.healthappandroid.common.helpers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.views.InputView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InputValidator implements TextWatcher {
    private static final Set<Character> validChars = new HashSet<>(
            Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
    public final InputView view;
    private final short maxVal;
    private final short minVal;

    public short result = 0;
    public boolean valid = false;
    private boolean isInputNumeric = false;
    private final InputValidationDelegate delegate;

    public InputValidator(short maxVal, InputView view, InputValidationDelegate delegate) {
        this.minVal = 0;
        this.maxVal = maxVal;
        this.view = view;
        this.delegate = delegate;
        view.textField.addTextChangedListener(this);
    }

    public InputValidator(short min, short max, InputView view, InputValidationDelegate delegate) {
        this.minVal = min;
        this.maxVal = max;
        this.view = view;
        this.delegate = delegate;
        view.textField.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count == 0) {
            isInputNumeric = true;
            return;
        }
        for (int i = start; i < start + count; ++i) {
            if (!validChars.contains(s.charAt(i))) {
                isInputNumeric = false;
                return;
            }
        }
        isInputNumeric = true;
    }

    @Override
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

        if (res < minVal || res > maxVal) {
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
        String errorMessage = ViewHelper.format("Input must be between %d and %d", minVal, maxVal);
        view.field.setError(errorMessage);
        delegate.disableButton();
    }
}
