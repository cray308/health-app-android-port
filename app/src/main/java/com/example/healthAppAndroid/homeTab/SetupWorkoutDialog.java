package com.example.healthAppAndroid.homeTab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.healthAppAndroid.R;
import com.example.healthAppAndroid.core.TextValidator;
import com.example.healthAppAndroid.homeTab.addWorkout.ExerciseManager;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutParams;
import com.example.healthAppAndroid.homeTab.addWorkout.WorkoutType;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public final class SetupWorkoutDialog
  extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    private static final String namesKey = "SetupWorkout.Names";
    private static final String typeKey = "SetupWorkout.Type";

    private final WorkoutParams output = new WorkoutParams((byte)-1);
    private TextValidator validator;
    private String[] names;
    private byte type;

    static SetupWorkoutDialog init(Context c, byte type) {
        SetupWorkoutDialog fragment = new SetupWorkoutDialog();
        Bundle args = new Bundle();
        args.putStringArray(namesKey, ExerciseManager.getWorkoutNamesForType(c, type));
        args.putByte(typeKey, type);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            names = args.getStringArray(namesKey);
            type = args.getByte(typeKey);
            output.type = type;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.setup_workout_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.cancelBtn).setOnClickListener(view1 -> dismiss());

        Button submitButton = view.findViewById(R.id.submitBtn);
        submitButton.setOnClickListener(view2 -> {
            short[] results = validator.getResults();
            switch (output.type) {
                case WorkoutType.strength:
                    output.weight = results[2];
                case WorkoutType.SE:
                    output.sets = results[0];
                    output.reps = results[1];
                    break;

                case WorkoutType.endurance:
                    output.reps = results[0];
                default:
            }
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            HomeFragment home = null;
            for (Fragment fragment : fragments) {
                if ("1".equals(fragment.getTag()))
                    home = (HomeFragment)fragment;
            }

            if (home != null)
                home.navigateToAddWorkout(this, output);
        });

        validator = new TextValidator(submitButton);
        Spinner picker = view.findViewById(R.id.workoutPicker);
        LinearLayout inputViewStack = view.findViewById(R.id.textFieldStack);
        Context c = getContext();

        picker.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(c, android.R.layout.simple_spinner_item,
                                                          names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);
        picker.setSelection(0);

        short[] maxes = {5, 5, 100};
        short[] minArr = {1, 1, 1};
        String[] titles = {getString(R.string.sets), getString(R.string.reps), null};

        if (type == WorkoutType.strength) {
            titles[2] = getString(R.string.setupWorkoutMaxWeight);
        } else if (type == WorkoutType.SE) {
            maxes[0] = 3;
            maxes[1] = 50;
        } else if (type == WorkoutType.endurance) {
            titles[0] = null;
            titles[1] = getString(R.string.setupWorkoutDuration);
            maxes[1] = 180;
            minArr[1] = 15;
        } else {
            titles[0] = titles[1] = null;
            validator.enableButton();
        }

        for (int i = 0; i < 3; ++i) {
            if (titles[i] == null) continue;
            TextValidator.InputView v = new TextValidator.InputView(c);
            v.field.setHint(titles[i]);
            validator.addChild(minArr[i], maxes[i], R.plurals.inputFieldError, v);
            inputViewStack.addView(v);
        }
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (dialog != null)
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        output.index = i;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {}
}
