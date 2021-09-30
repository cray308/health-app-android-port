package com.example.healthAppAndroid.homeTab.view;

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

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.common.helpers.TextValidator;
import com.example.healthAppAndroid.common.shareddata.AppColors;
import com.example.healthAppAndroid.common.workouts.Workout;
import com.example.healthAppAndroid.homeTab.HomeTabCoordinator;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class HomeSetupWorkoutDialog extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    private static final String paramsKey = "HomeSetupWorkoutDialogParams";

    public static class Params implements Parcelable {
        public final byte type;
        public final String[] names;

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
            @Override public Params createFromParcel(Parcel parcel) { return new Params(parcel); }

            @Override public Params[] newArray(int i) { return new Params[i]; }
        };
    }

    private Params params;
    private final Workout.Params output = new Workout.Params((byte) -1);
    public HomeTabCoordinator delegate;
    private TextValidator validator;

    public static HomeSetupWorkoutDialog newInstance(Params params) {
        HomeSetupWorkoutDialog fragment = new HomeSetupWorkoutDialog();
        Bundle args = new Bundle();
        args.putParcelable(paramsKey, params);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            params = args.getParcelable(paramsKey);
            output.type = params.type;
        }
    }

    @Override public View onCreateView(LayoutInflater inflater,
                                       ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_setup_workout_modal, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button cancelButton = view.findViewById(R.id.homeSetupWorkoutModalCancel);
        cancelButton.setOnClickListener(view1 -> dismiss());

        Button submitButton = view.findViewById(R.id.homeSetupWorkoutModalSubmit);
        submitButton.setOnClickListener(view2 -> {
            short[] results = validator.getResults();
            switch (output.type) {
                case Workout.Type.strength:
                    output.weight = results[2];
                case Workout.Type.SE:
                    output.sets = results[0];
                    output.reps = results[1];
                    break;

                case Workout.Type.endurance:
                    output.reps = results[0];
                default:
            }
            delegate.finishedBottomSheet(HomeSetupWorkoutDialog.this, output);
        });

        validator = new TextValidator(submitButton, AppColors.blue);
        Spinner picker = view.findViewById(R.id.workoutPicker);
        LinearLayout inputViewStack = view.findViewById(R.id.textFieldStack);
        Context c = getContext();
        if (c == null) return;

        picker.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(c, android.R.layout.simple_spinner_item,
                                                          params.names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);
        picker.setSelection(0);

        short[] maxes = {5, 5, 100};
        String[] titles = {
            getString(R.string.setupWorkoutSets), getString(R.string.setupWorkoutReps), null
        };

        switch (params.type) {
            case Workout.Type.strength:
                titles[2] = getString(R.string.setupWorkoutMaxWeight);
                break;

            case Workout.Type.SE:
                maxes[0] = 3;
                maxes[1] = 50;
                break;

            case Workout.Type.endurance:
                titles[0] = null;
                titles[1] = getString(R.string.setupWorkoutDuration);
                maxes[1] = 180;
                break;

            default:
                titles[0] = titles[1] = null;
                validator.enableButton();
        }

        for (int i = 0; i < 3; ++i) {
            if (titles[i] == null) continue;
            TextValidator.InputView v = new TextValidator.InputView(c);
            v.field.setHint(titles[i]);
            validator.addChild(maxes[i], v);
            inputViewStack.addView(v);
            Space space = new Space(c);
            space.setMinimumHeight(20);
            inputViewStack.addView(space);
        }
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        output.index = i;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {}
}
