package com.example.healthappandroid.hometab.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthappandroid.R;
import com.example.healthappandroid.common.helpers.InputValidationDelegate;
import com.example.healthappandroid.common.helpers.InputValidator;
import com.example.healthappandroid.common.shareddata.AppColors;
import com.example.healthappandroid.common.views.InputView;
import com.example.healthappandroid.common.workouts.Workout;
import com.example.healthappandroid.hometab.HomeTabCoordinator;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;

public class HomeSetupWorkoutDialog extends BottomSheetDialogFragment implements InputValidationDelegate, AdapterView.OnItemSelectedListener {
    private static final String paramsKey = "HomeSetupWorkoutDialogParams";

    public static class Params implements Parcelable {
        public byte type;
        public String[] names;

        public Params(byte type, String[] names) {
            this.type = type;
            this.names = names;
        }

        private Params(Parcel src) {
            names = src.createStringArray();
            type = src.readByte();
        }

        public int describeContents() { return 0; }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeStringArray(names);
            parcel.writeByte(type);
        }

        public static final Creator<Params> CREATOR = new Creator<Params>() {
            @Override
            public Params createFromParcel(Parcel parcel) { return new Params(parcel); }

            @Override
            public Params[] newArray(int i) { return new Params[i]; }
        };
    }

    private Params params;
    public HomeTabCoordinator delegate;
    private int index;
    private Button submitButton;
    private final InputValidator[] validators = {null, null, null};
    private final short[] inputs = {0, 0, 0};

    public static HomeSetupWorkoutDialog newInstance(Params params) {
        HomeSetupWorkoutDialog fragment = new HomeSetupWorkoutDialog();
        Bundle args = new Bundle();
        args.putParcelable(paramsKey, params);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            params = args.getParcelable(paramsKey);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_setup_workout_modal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button cancelButton = view.findViewById(R.id.homeSetupWorkoutModalCancel);
        cancelButton.setOnClickListener(cancelListener);
        submitButton = view.findViewById(R.id.homeSetupWorkoutModalSubmit);
        submitButton.setOnClickListener(finishListener);
        Spinner picker = view.findViewById(R.id.workoutPicker);
        LinearLayout inputViewStack = view.findViewById(R.id.textFieldStack);
        Context c = getContext();
        if (c == null) return;

        picker.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(c,
                android.R.layout.simple_spinner_item, params.names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);
        picker.setSelection(0);

        short[] maxes = {0, 0, 0};
        short minVal = 1;
        String[] titles = {null, null, null};

        switch (params.type) {
            case Workout.TypeStrength:
                titles[0] = "Sets";
                titles[1] = "Reps";
                titles[2] = "Max Weight Percentage";
                maxes[0] = maxes[1] = 5;
                maxes[2] = 100;
                disableButton();
                break;

            case Workout.TypeSE:
                titles[0] = "Sets";
                titles[1] = "Reps";
                inputs[2] = 1;
                maxes[0] = 3;
                maxes[1] = 50;
                disableButton();
                break;

            case Workout.TypeEndurance:
                titles[1] = "Duration (mins)";
                inputs[0] = inputs[2] = 1;
                maxes[1] = 180;
                disableButton();
                break;

            default:
                Arrays.fill(inputs, (short) 1);
                break;
        }

        for (int i = 0; i < 3; ++i) {
            if (titles[i] == null) continue;
            InputView v = new InputView(c);
            v.field.setHint(titles[i]);
            validators[i] = new InputValidator(minVal, maxes[i], v, this);
            inputViewStack.addView(v);
            Space space = new Space(c);
            space.setMinimumHeight(20);
            inputViewStack.addView(space);
        }
    }

    private final View.OnClickListener cancelListener = view -> dismiss();

    private final View.OnClickListener finishListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            for (int i = 0; i < 3; ++i) {
                if (validators[i] != null)
                    inputs[i] = validators[i].result;
            }
            delegate.finishedSettingUpCustomWorkout(
                    HomeSetupWorkoutDialog.this, params.type, index, inputs);
        }
    };

    public void disableButton() {
        submitButton.setEnabled(false);
        submitButton.setTextColor(AppColors.labelDisabled);
    }

    public void checkFields() {
        for (int i = 0; i < 3; ++i) {
            if (validators[i] != null && !validators[i].valid) {
                disableButton();
                return;
            }
        }
        submitButton.setEnabled(true);
        submitButton.setTextColor(AppColors.blue);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { index = i; }

    public void onNothingSelected(AdapterView<?> adapterView) {}
}
