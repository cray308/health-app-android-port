package com.example.healthAppAndroid.common.helpers;

import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.common.shareddata.AppColors;

public class BaseTextValidator extends Fragment implements InputValidationDelegate {
    public final InputValidator[] children = {null, null, null, null};
    public final Button button;
    private final int enabledColor;

    public BaseTextValidator(Button button, int enabledColor) {
        this.button = button;
        this.enabledColor = enabledColor;
    }

    public void enableButton() {
        button.setEnabled(true);
        button.setTextColor(enabledColor);
    }

    public void disableButton() {
        button.setEnabled(false);
        button.setTextColor(AppColors.labelDisabled);
    }

    public void checkFields() {
        for (int i = 0; i < 4; ++i) {
            if (children[i] != null && !children[i].valid) {
                disableButton();
                return;
            }
        }
        enableButton();
    }
}
