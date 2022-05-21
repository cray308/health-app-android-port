package com.example.healthAppAndroid.core;

import android.content.Context;
import android.content.res.TypedArray;
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

import java.util.Locale;
import java.util.regex.Pattern;

public final class TextValidator {
    public static final class InputView extends LinearLayout implements TextWatcher {
        private static final InputFilter filter = new InputFilter() {
            private final Pattern pattern = Pattern.compile("([0-9]+)?(\\.[0-9]{0,2})?");

            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dStart, int dEnd) {
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dStart, dEnd, source.subSequence(start, end).toString());
                if (pattern.matcher(builder.toString()).matches()) return null;
                return source.length() == 0 ? dest.subSequence(dStart, dEnd) : "";
            }
        };

        public TextInputLayout layout;
        TextInputEditText field;
        private TextValidator delegate;
        private int min;
        private int max = 999;
        private int errorId = R.plurals.inputFieldError;
        public float result;
        boolean valid;

        public InputView(Context context) {
            super(context);
            setup(context, null);
        }

        public InputView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setup(context, attrs);
        }

        private void setup(Context context, AttributeSet attrs) {
            inflate(context, R.layout.input_view, this);
            layout = findViewById(R.id.layout);
            field = findViewById(R.id.field);
            field.addTextChangedListener(this);

            if (attrs != null) {
                TypedArray arr = context.getTheme().obtainStyledAttributes(
                  attrs, R.styleable.InputView, 0, 0);
                try {
                    min = arr.getInt(R.styleable.InputView_min, 0);
                    errorId = arr.getResourceId(R.styleable.InputView_errorId,
                                                R.plurals.inputFieldError);
                    int index = arr.getInt(R.styleable.InputView_index, 0);
                    if (index >= 0) {
                        String liftName = getResources().getStringArray(R.array.exNames)[index];
                        String adjustedName = liftName.toLowerCase(Locale.getDefault());
                        layout.setHint(getContext().getString(R.string.maxWeight, adjustedName));
                    } else {
                        valid = true;
                        layout.setError(null);
                        layout.setHint(getContext().getString(R.string.bodyWeight));
                    }
                } finally {
                    arr.recycle();
                }
            }
        }

        private void setup(TextValidator validator, int inputType) {
            delegate = validator;
            field.setInputType(InputType.TYPE_CLASS_NUMBER | inputType);
            if (inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL)
                field.setFilters(new InputFilter[]{filter});
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        public void afterTextChanged(Editable s) {
            boolean isEmpty = s.length() == 0;
            if (isEmpty && errorId != R.plurals.inputFieldErrorEmpty) {
                showErrorMessage();
                return;
            }

            float value = -1;
            if (!isEmpty) {
                try {
                    value = Float.parseFloat(s.toString());
                } finally {
                    if (value < min || value > max) {
                        showErrorMessage();
                        return;
                    }
                }
            }

            reset(value);
            for (int i = 0; i < delegate.count; ++i) {
                if (!delegate.children[i].valid) {
                    delegate.button.setEnabled(false);
                    return;
                }
            }
            delegate.button.setEnabled(true);
        }

        private void showErrorMessage() {
            valid = false;
            layout.setError(getResources().getQuantityString(errorId, 1, min, max));
            delegate.button.setEnabled(false);
        }

        void reset(float value) {
            valid = true;
            layout.setError(null);
            result = value;
        }
    }

    public static int inputForLocale(boolean metric) {
        return metric ? InputType.TYPE_NUMBER_FLAG_DECIMAL : InputType.TYPE_NUMBER_VARIATION_NORMAL;
    }

    public final InputView[] children = {null, null, null, null, null};
    final Button button;
    private int count;

    public TextValidator(Button button) { this.button = button; }

    public void addChild(InputView view, int inputType) {
        view.setup(this, inputType);
        children[count++] = view;
    }

    public void addChild(InputView view, CharSequence hint, int min, int max) {
        view.min = min;
        view.max = max;
        view.layout.setHint(hint);
        addChild(view, InputType.TYPE_NUMBER_VARIATION_NORMAL);
    }
}
