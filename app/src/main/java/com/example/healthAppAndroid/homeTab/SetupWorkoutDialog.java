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
    private static final String key = "SetupWorkoutKey";

    private final WorkoutParams output = new WorkoutParams((byte)-1);
    private TextValidator validator;

    static SetupWorkoutDialog init(byte type) {
        SetupWorkoutDialog fragment = new SetupWorkoutDialog();
        Bundle args = new Bundle();
        args.putByte(key, type);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) output.type = args.getByte(key);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.setup_workout_modal, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context c = getContext();
        if (c == null) return;
        view.findViewById(R.id.cancelBtn).setOnClickListener(view1 -> dismiss());

        Button submitButton = view.findViewById(R.id.submitBtn);
        submitButton.setOnClickListener(view2 -> {
            switch (output.type) {
                case WorkoutType.strength:
                    output.weight = (short)validator.children[2].result;
                case WorkoutType.SE:
                    output.sets = (short)validator.children[0].result;
                    output.reps = (short)validator.children[1].result;
                    break;

                case WorkoutType.endurance:
                    output.reps = (short)validator.children[0].result;
                default:
            }
            List<Fragment> fragments = getParentFragmentManager().getFragments();
            HomeFragment home = null;
            for (Fragment fragment : fragments) {
                if ("1".equals(fragment.getTag())) home = (HomeFragment)fragment;
            }

            if (home != null) home.navigateToAddWorkout(this, output);
        });

        validator = new TextValidator(submitButton);
        Spinner picker = view.findViewById(R.id.workoutPicker);
        picker.setOnItemSelectedListener(this);
        String[] names = ExerciseManager.getWorkoutNamesForType(c, output.type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(c, android.R.layout.simple_spinner_item,
                                                          names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);
        picker.setSelection(0);

        int[] maxes = {5, 5, 100}, minArr = {1, 1, 1}, titleKeys = {R.string.sets, R.string.reps, -1};

        if (output.type == WorkoutType.strength) {
            titleKeys[2] = R.string.setupWorkoutMaxWeight;
        } else if (output.type == WorkoutType.SE) {
            maxes[0] = 3;
            maxes[1] = 50;
        } else if (output.type == WorkoutType.endurance) {
            titleKeys[0] = -1;
            titleKeys[1] = R.string.setupWorkoutDuration;
            maxes[1] = 180;
            minArr[1] = 15;
        } else {
            titleKeys[0] = titleKeys[1] = -1;
            validator.enableButton();
        }

        LinearLayout inputViewStack = view.findViewById(R.id.textFieldStack);
        for (int i = 0; i < 3; ++i) {
            if (titleKeys[i] == -1) continue;
            TextValidator.InputView v = new TextValidator.InputView(c);
            v.field.setHint(getString(titleKeys[i]));
            validator.addChild(minArr[i], maxes[i], R.plurals.inputFieldError, 0, v);
            inputViewStack.addView(v);
        }
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (dialog != null) dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        output.index = i;
    }

    public void onNothingSelected(AdapterView<?> adapterView) {}
}
