package com.example.healthAppAndroid.core;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.healthAppAndroid.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public final class TextValidator {
    public static final class InputView extends LinearLayout implements TextWatcher {
        private static final InputFilter filter = new InputFilter() {
            private final Pattern pattern = Pattern.compile("([0-9]+)?(\\.[0-9]{0,2})?");

            public CharSequence filter(CharSequence seq, int i, int j, Spanned dest, int d1, int d2) {
                StringBuilder sb = new StringBuilder(dest);
                sb.replace(d1, d2, seq.subSequence(i, j).toString());
                if (pattern.matcher(sb.toString()).matches()) return null;
                return seq.length() == 0 ? dest.subSequence(d1, d2) : "";
            }
        };

        public TextInputLayout field;
        TextInputEditText textField;
        private TextValidator delegate;
        private int id;
        private int min;
        private int max;
        public float result = 0;
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

        private void setup(int minVal, int maxVal, int resId, int type, TextValidator validator) {
            min = minVal;
            max = maxVal;
            id = resId;
            emptyInputAllowed = resId == R.plurals.inputFieldErrorEmpty;
            delegate = validator;
            textField.addTextChangedListener(this);
            textField.setInputType(InputType.TYPE_CLASS_NUMBER | type);
            if (type == InputType.TYPE_NUMBER_FLAG_DECIMAL)
                textField.setFilters(new InputFilter[]{filter});
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        public void afterTextChanged(Editable s) {
            boolean isEmpty = s.length() == 0;
            if (isEmpty && !emptyInputAllowed) {
                showErrorMsg();
                return;
            }

            float res = -1;
            if (!isEmpty) {
                try {
                    res = Float.parseFloat(s.toString());
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

    public final InputView[] children = {null, null, null, null, null};
    private final Button button;
    private int count = 0;

    public TextValidator(Button button) { this.button = button; }

    public void enableButton() {
        button.setEnabled(true);
        button.setTextColor(AppColors.blue);
    }

    public void addChild(int min, int max, int id, int kb, InputView view) {
        view.setup(min, max, id, kb, this);
        children[count++] = view;
    }
}
