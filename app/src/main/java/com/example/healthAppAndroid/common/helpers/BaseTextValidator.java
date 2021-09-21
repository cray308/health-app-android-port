package com.example.healthAppAndroid.common.helpers;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.views.InputView;
import com.example.healthAppAndroid.common.workouts.Workout;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BaseTextValidator {
    public static class InputValidator implements TextWatcher {
        private final InputView view;
        private final short maxVal, minVal;
        private final BaseTextValidator delegate;
        private boolean valid = false, isInputNumeric = false;
        public short result = 0;

        private InputValidator(short min, short max, InputView view, BaseTextValidator delegate) {
            this.minVal = min;
            this.maxVal = max;
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
            view.field.setError(view.getContext().getString(R.string.inputFieldError,
                                                            minVal, maxVal));
            delegate.disableButton();
        }
    }

    private final Set<Character> validChars = new HashSet<>(
        Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
    public final InputValidator[] children = {null, null, null, null};
    private final Button button;
    private final int enabledColor;

    public BaseTextValidator(Button button, int enabledColor) {
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
        for (int i = 0; i < 4; ++i) {
            if (children[i] != null && !children[i].valid) {
                disableButton();
                return;
            }
        }
        enableButton();
    }

    public void addChild(int index, short min, short max, InputView view) {
        children[index] = new InputValidator(min, max, view, this);
    }

    public void reset(int index, short value) {
        children[index].result = value;
        children[index].valid = true;
        String text = ViewHelper.format("%d", value);
        children[index].view.field.setError(null);
        children[index].view.textField.setText(text);
    }

    public Workout.LiftData getLiftData() {
        return new Workout.LiftData(children[0].result, children[1].result,
                                    children[2].result, children[3].result);
    }
}
